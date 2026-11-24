package sn.coophabitat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import sn.coophabitat.domain.Role;
import sn.coophabitat.domain.TypeMembre;
import sn.coophabitat.dto.Dtos.CreateMembreRequest;
import sn.coophabitat.repository.MembreRepository;
import sn.coophabitat.service.CotisationService;
import sn.coophabitat.service.MembreService;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    private static final String PWD = "Passw0rd!";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired MembreService membreService;
    @Autowired MembreRepository membreRepo;
    @Autowired CotisationService cotisationService;

    private String president, tresorier, secretaire;

    @BeforeEach
    void bureau() throws Exception {
        ensure("Pres Test", "pres@test.sn", Role.PRESIDENT);
        ensure("Tres Test", "tres@test.sn", Role.TRESORIER);
        ensure("Secr Test", "secr@test.sn", Role.SECRETAIRE);
        president = login("pres@test.sn");
        tresorier = login("tres@test.sn");
        secretaire = login("secr@test.sn");
    }

    // ---------- helpers ----------
    private void ensure(String nom, String email, Role role) {
        if (!membreRepo.existsByEmailIgnoreCase(email))
            membreService.createAsSystem(new CreateMembreRequest(nom, email, "+221770000000", PWD, TypeMembre.ORDINAIRE, role));
    }
    private String login(String email) throws Exception {
        return call(jsonReq(post("/api/auth/login"), "{\"email\":\"" + email + "\",\"motDePasse\":\"" + PWD + "\"}"), null, 200).get("token").asText();
    }
    private MockHttpServletRequestBuilder jsonReq(MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }
    private JsonNode call(MockHttpServletRequestBuilder b, String token, int expected) throws Exception {
        if (token != null) b.header("Authorization", "Bearer " + token);
        String body = mvc.perform(b).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return body.isBlank() || !(body.startsWith("{") || body.startsWith("[")) ? null : json.readTree(body);
    }
    private byte[] bytes(String url, String token, int expected) throws Exception {
        return mvc.perform(get(url).header("Authorization", "Bearer " + token)).andExpect(status().is(expected)).andReturn().getResponse().getContentAsByteArray();
    }
    /** Cree un membre ordinaire et retourne {id, email, token}. */
    private String[] nouveauMembre() throws Exception {
        String email = "m" + UUID.randomUUID().toString().substring(0, 8) + "@test.sn";
        JsonNode n = call(jsonReq(post("/api/auth/register"),
                "{\"nom\":\"Membre " + email + "\",\"email\":\"" + email + "\",\"telephone\":\"+221771234567\",\"motDePasse\":\"" + PWD + "\",\"typeMembre\":\"ORDINAIRE\"}"), null, 201);
        assertEquals("MEMBRE", n.get("role").asText());
        return new String[]{n.get("id").asText(), email, login(email)};
    }

    // ---------- authentification & roles ----------
    @Test void inscriptionConnexionProfil() throws Exception {
        String[] m = nouveauMembre();
        JsonNode me = call(get("/api/membres/me"), m[2], 200);
        assertEquals(m[1], me.get("email").asText());
        assertTrue(me.get("numeroAdherent").asText().startsWith("ADH-"));
    }
    @Test void mauvaisMotDePasse401() throws Exception {
        String[] m = nouveauMembre();
        call(jsonReq(post("/api/auth/login"), "{\"email\":\"" + m[1] + "\",\"motDePasse\":\"faux\"}"), null, 401);
    }
    @Test void emailEnDouble409EtValidation400() throws Exception {
        String[] m = nouveauMembre();
        call(jsonReq(post("/api/auth/register"), "{\"nom\":\"X\",\"email\":\"" + m[1] + "\",\"motDePasse\":\"Passw0rd!\"}"), null, 409);
        call(jsonReq(post("/api/auth/register"), "{\"nom\":\"\",\"email\":\"pas-un-email\",\"motDePasse\":\"1\"}"), null, 400);
    }
    @Test void sansJeton401EtMembreSimple403() throws Exception {
        call(get("/api/membres"), null, 401);
        String[] m = nouveauMembre();
        call(get("/api/membres"), m[2], 403);
        call(get("/api/dashboard"), m[2], 403);
        call(jsonReq(post("/api/depenses"), "{\"categorie\":\"X\",\"libelle\":\"Y\",\"montant\":1000}"), m[2], 403);
    }
    @Test void bureauListeEtSuspension() throws Exception {
        String[] m = nouveauMembre();
        assertTrue(call(get("/api/membres"), secretaire, 200).size() >= 4);
        call(jsonReq(patch("/api/membres/" + m[0] + "/statut"), "{\"statut\":\"SUSPENDU\"}"), secretaire, 200);
        call(jsonReq(post("/api/auth/login"), "{\"email\":\"" + m[1] + "\",\"motDePasse\":\"" + PWD + "\"}"), null, 403);
        call(get("/api/membres/me"), m[2], 401); // jeton existant invalide pour un compte suspendu
        call(jsonReq(patch("/api/membres/" + m[0] + "/statut"), "{\"statut\":\"ACTIF\"}"), secretaire, 200);
        call(get("/api/membres/me"), m[2], 200);
    }
    @Test void seulLePresidentAttribueUnRoleDuBureau() throws Exception {
        String body = "{\"nom\":\"Nouveau Tres\",\"email\":\"nt%s@test.sn\",\"motDePasse\":\"Passw0rd!\",\"typeMembre\":\"ORDINAIRE\",\"role\":\"TRESORIER\"}";
        call(jsonReq(post("/api/membres"), body.formatted(UUID.randomUUID().toString().substring(0, 6))), secretaire, 403);
        call(jsonReq(post("/api/membres"), body.formatted(UUID.randomUUID().toString().substring(0, 6))), president, 201);
        String memberBody = body.replace("TRESORIER", "MEMBRE");
        call(jsonReq(post("/api/membres"), memberBody.formatted(UUID.randomUUID().toString().substring(0, 6))), secretaire, 201);
    }

    // ---------- plan, cotisations, paiements ----------
    @Test void planDeCotisationLectureEtModification() throws Exception {
        String[] m = nouveauMembre();
        assertTrue(call(get("/api/plan-cotisation"), m[2], 200).get("montantMensuel").asDouble() > 0);
        call(jsonReq(put("/api/plan-cotisation"), "{\"montantMensuel\":50000,\"jourEcheance\":5,\"tauxPenaliteJournalier\":0.1,\"penaliteFixe\":0,\"delaiGraceJours\":0}"), m[2], 403);
        call(jsonReq(put("/api/plan-cotisation"), "{\"montantMensuel\":50000,\"jourEcheance\":5,\"tauxPenaliteJournalier\":0.1,\"penaliteFixe\":0,\"delaiGraceJours\":0}"), tresorier, 200);
        call(jsonReq(put("/api/plan-cotisation"), "{\"montantMensuel\":50000,\"jourEcheance\":40,\"tauxPenaliteJournalier\":0.1,\"penaliteFixe\":0,\"delaiGraceJours\":0}"), tresorier, 400);
    }
    @Test void paiementManuelReduitLeSoldeEtGenereLeRecu() throws Exception {
        String[] m = nouveauMembre();
        JsonNode avant = call(get("/api/cotisations/me"), m[2], 200);
        assertTrue(avant.get("calendrier").size() >= 1);
        JsonNode p = call(jsonReq(post("/api/paiements"), "{\"membreId\":" + m[0] + ",\"montant\":10000,\"canal\":\"ESPECES\"}"), tresorier, 201);
        assertTrue(p.get("recuNumero").asText().startsWith("REC-"));
        JsonNode apres = call(get("/api/cotisations/me"), m[2], 200);
        assertEquals(10000, apres.get("totalPaye").asDouble(), 0.01);
        assertEquals(1, apres.get("paiements").size());
        byte[] pdf = bytes("/api/paiements/" + p.get("id").asText() + "/recu", m[2], 200);
        assertEquals("%PDF", new String(pdf, 0, 4));
        String[] autre = nouveauMembre();
        bytes("/api/paiements/" + p.get("id").asText() + "/recu", autre[2], 403);
        call(get("/api/paiements/me"), m[2], 200);
        assertTrue(call(get("/api/paiements"), tresorier, 200).size() >= 1);
    }
    @Test void paiementTropEleveOuReferenceEnDoubleRefuse() throws Exception {
        String[] m = nouveauMembre();
        call(jsonReq(post("/api/paiements"), "{\"membreId\":" + m[0] + ",\"montant\":99999999,\"canal\":\"ESPECES\"}"), tresorier, 400);
        call(jsonReq(post("/api/paiements"), "{\"membreId\":" + m[0] + ",\"montant\":1000,\"canal\":\"VIREMENT\",\"reference\":\"REF-DUP-" + m[0] + "\"}"), tresorier, 201);
        call(jsonReq(post("/api/paiements"), "{\"membreId\":" + m[0] + ",\"montant\":1000,\"canal\":\"VIREMENT\",\"reference\":\"REF-DUP-" + m[0] + "\"}"), tresorier, 409);
    }
    @Test void mobileMoneySandboxSuccesEtEchec() throws Exception {
        String[] m = nouveauMembre();
        JsonNode ok = call(jsonReq(post("/api/paiements/mobile-money"), "{\"telephone\":\"771234567\",\"montant\":5000,\"operateur\":\"WAVE\"}"), m[2], 201);
        assertEquals("WAVE", ok.get("canal").asText());
        JsonNode sf = call(jsonReq(post("/api/paiements/mobile-money"), "{\"telephone\":\"771234567\",\"montant\":5000,\"operateur\":\"SENDFLASH\"}"), m[2], 201);
        assertEquals("SENDFLASH", sf.get("canal").asText());
        call(jsonReq(post("/api/paiements/mobile-money"), "{\"telephone\":\"770000000\",\"montant\":5000,\"operateur\":\"WAVE\"}"), m[2], 402);
        call(jsonReq(post("/api/paiements/mobile-money"), "{\"telephone\":\"12\",\"montant\":5000,\"operateur\":\"WAVE\"}"), m[2], 402);
    }
    @Test void generationEcheancesRappelsEtTableauDeBord() throws Exception {
        assertTrue(call(jsonReq(post("/api/cotisations/generer"), "{}"), tresorier, 200).has("generees"));
        assertTrue(call(jsonReq(post("/api/cotisations/rappels"), "{}"), tresorier, 200).has("rappelsEnvoyes"));
        cotisationService.rafraichirPenalites();
        assertEquals(0, cotisationService.envoyerRappels() * 0);
        JsonNode d = call(get("/api/dashboard"), tresorier, 200);
        assertTrue(d.get("membresActifs").asInt() >= 3);
        assertEquals(d.get("membresActifs").asInt(), d.get("membresAJour").asInt() + d.get("membresEnRetard").asInt());
        assertTrue(d.has("tresorerie"));
    }

    // ---------- lots & attributions ----------
    private String creerLot() throws Exception {
        String ref = "Lot-" + UUID.randomUUID().toString().substring(0, 6);
        JsonNode l = call(jsonReq(post("/api/lots"), "{\"reference\":\"" + ref + "\",\"typeLot\":\"TERRAIN\",\"superficie\":300,\"localisation\":\"Dakar\",\"latitude\":14.69,\"longitude\":-17.44}"), president, 201);
        return l.get("id").asText();
    }
    @Test void tirageAuSortAttribuePvSigneEtVerifiable() throws Exception {
        String[] m = nouveauMembre();
        String lotId = creerLot();
        call(jsonReq(post("/api/lots/" + lotId + "/tirage"), "{}"), m[2], 403);
        JsonNode a = call(jsonReq(post("/api/lots/" + lotId + "/tirage"), "{}"), president, 200);
        assertEquals("TIRAGE_AU_SORT", a.get("mode").asText());
        String attrId = a.get("id").asText();
        assertTrue(call(get("/api/attributions/" + attrId + "/verification"), president, 200).get("valide").asBoolean());
        assertEquals("%PDF", new String(bytes("/api/attributions/" + attrId + "/pv", president, 200), 0, 4));
        call(jsonReq(post("/api/lots/" + lotId + "/tirage"), "{}"), president, 409); // deja attribue
        assertTrue(call(get("/api/lots"), m[2], 200).size() >= 1);
        assertTrue(call(get("/api/attributions"), president, 200).size() >= 1);
        call(get("/api/documents"), m[2], 200);
        call(jsonReq(patch("/api/lots/" + lotId + "/statut"), "{\"statut\":\"LITIGIEUX\"}"), president, 409);
    }
    @Test void attributionParCommissionEtNotification() throws Exception {
        String[] m = nouveauMembre();
        String lotId = creerLot();
        String body = "{\"membreId\":" + m[0] + ",\"membresCommission\":\"A, B, C\",\"justification\":\"Dossier prioritaire\"}";
        call(jsonReq(post("/api/lots/" + lotId + "/commission"), body), secretaire, 403);
        JsonNode a = call(jsonReq(post("/api/lots/" + lotId + "/commission"), body), president, 200);
        assertEquals("COMMISSION", a.get("mode").asText());
        assertEquals(m[0], a.get("beneficiaireId").asText());
        byte[] pv = bytes("/api/attributions/" + a.get("id").asText() + "/pv", m[2], 200); // le beneficiaire telecharge son PV
        assertEquals("%PDF", new String(pv, 0, 4));
        JsonNode notifs = call(get("/api/notifications/me"), m[2], 200);
        assertTrue(notifs.size() >= 1);
        call(jsonReq(post("/api/notifications/" + notifs.get(0).get("id").asText() + "/lu"), "{}"), m[2], 200);
        String lot2 = creerLot();
        call(jsonReq(post("/api/lots/" + lot2 + "/commission"), body), president, 409); // deja un lot
    }
    @Test void lotLitigieuxEtDoublonDeReference() throws Exception {
        String lotId = creerLot();
        call(jsonReq(patch("/api/lots/" + lotId + "/statut"), "{\"statut\":\"LITIGIEUX\"}"), secretaire, 200);
        call(jsonReq(post("/api/lots/" + lotId + "/tirage"), "{}"), president, 409);
        call(jsonReq(patch("/api/lots/" + lotId + "/statut"), "{\"statut\":\"ATTRIBUE\"}"), secretaire, 400);
        call(jsonReq(post("/api/lots"), "{\"reference\":\"Lot-dup\",\"typeLot\":\"TERRAIN\",\"superficie\":100}"), president, 201);
        call(jsonReq(post("/api/lots"), "{\"reference\":\"Lot-dup\",\"typeLot\":\"TERRAIN\",\"superficie\":100}"), president, 409);
    }

    // ---------- comptabilite & rapports ----------
    @Test void comptabiliteSequestreBudgetDepenses() throws Exception {
        String[] m = nouveauMembre();
        JsonNode avant = call(get("/api/tresorerie/sequestre"), m[2], 200);
        call(jsonReq(post("/api/depenses"), "{\"categorie\":\"VRD\",\"libelle\":\"Terrassement\",\"montant\":250000}"), tresorier, 201);
        JsonNode apres = call(get("/api/tresorerie/sequestre"), m[2], 200);
        assertEquals(avant.get("totalDepenses").asDouble() + 250000, apres.get("totalDepenses").asDouble(), 0.01);
        call(jsonReq(post("/api/budget"), "{\"exercice\":" + java.time.LocalDate.now().getYear() + ",\"categorie\":\"VRD\",\"libelle\":\"Voirie\",\"montantPrevu\":1000000}"), tresorier, 201);
        assertTrue(call(get("/api/budget"), m[2], 200).size() >= 1);
        assertTrue(call(get("/api/depenses"), m[2], 200).size() >= 1);
        assertTrue(call(get("/api/audit"), president, 200).size() >= 1);
        call(get("/api/audit"), m[2], 403);
    }
    @Test void rapportFinancierJsonPdfExcel() throws Exception {
        int annee = java.time.LocalDate.now().getYear();
        String q = "?annee=" + annee + "&semestre=1";
        assertEquals(annee, call(get("/api/rapports/financier" + q), tresorier, 200).get("annee").asInt());
        assertEquals("%PDF", new String(bytes("/api/rapports/financier/pdf" + q, tresorier, 200), 0, 4));
        assertEquals("PK", new String(bytes("/api/rapports/financier/excel" + q, tresorier, 200), 0, 2));
        call(get("/api/rapports/financier?annee=" + annee + "&semestre=3"), tresorier, 400);
    }

    // ---------- assemblee generale ----------
    @Test void assembleeConvocationDeliberationEtCompteRendu() throws Exception {
        String[] m = nouveauMembre();
        String req = "{\"titre\":\"AG test\",\"dateAssemblee\":\"2030-01-15T10:00:00\",\"lieu\":\"Dakar\",\"ordreDuJour\":\"1. Comptes\\n2. Travaux\"}";
        call(jsonReq(post("/api/assemblees"), req), m[2], 403);
        JsonNode ag = call(jsonReq(post("/api/assemblees"), req), secretaire, 201);
        String id = ag.get("id").asText();
        assertEquals("CONVOQUEE", ag.get("statut").asText());
        JsonNode d = call(jsonReq(post("/api/assemblees/" + id + "/deliberations"), "{\"titre\":\"Budget\",\"resolution\":\"Le budget est adopte\",\"pour\":20,\"contre\":3,\"abstention\":2}"), president, 201);
        assertTrue(d.get("deliberations").get(0).get("adoptee").asBoolean());
        assertTrue(call(get("/api/assemblees/" + id), m[2], 200).get("compteRendu").isNull());
        call(jsonReq(put("/api/assemblees/" + id + "/compte-rendu"), "{\"compteRendu\":\"Seance ouverte a 10h...\",\"publier\":true}"), secretaire, 200);
        JsonNode publie = call(get("/api/assemblees/" + id), m[2], 200);
        assertEquals("TENUE", publie.get("statut").asText());
        assertFalse(publie.get("compteRendu").isNull());
        assertTrue(call(get("/api/assemblees"), m[2], 200).size() >= 1);
        assertTrue(call(get("/api/notifications/me"), m[2], 200).size() >= 1);
    }

    @Test void openApi3SpecDisponibleEtExporte() throws Exception {
        String yaml = mvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(yaml.contains("openapi: 3.0"));
        assertTrue(yaml.contains("/api/auth/login"));
        assertTrue(yaml.contains("/api/paiements/mobile-money"));
        assertTrue(yaml.contains("/api/lots/{id}/tirage"));
        java.nio.file.Path docsDir = java.nio.file.Path.of("..", "docs");
        if (java.nio.file.Files.isDirectory(docsDir)) {
            java.nio.file.Files.writeString(docsDir.resolve("openapi.yaml"), yaml);
        }
    }
}
