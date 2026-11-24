package sn.coophabitat.service;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import sn.coophabitat.dto.Dtos.BudgetLigneDto;
import sn.coophabitat.dto.Dtos.RapportFinancier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class ExcelService {
    public byte[] rapport(RapportFinancier r) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle bold = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            bold.setFont(f);
            Sheet s = wb.createSheet("Rapport S" + r.semestre() + " " + r.annee());
            int i = 0;
            Row t = s.createRow(i++);
            t.createCell(0).setCellValue("Rapport financier - semestre " + r.semestre() + " " + r.annee() + " (" + r.debut() + " au " + r.fin() + ")");
            t.getCell(0).setCellStyle(bold);
            i++;
            i = kv(s, i, "Budget previsionnel (exercice)", r.budgetPrevisionnel());
            i = kv(s, i, "Encaissements du semestre", r.encaissements());
            i = kv(s, i, "Depenses reelles du semestre", r.depenses());
            i = kv(s, i, "Tresorerie disponible (sequestre)", r.tresorerieDisponible());
            Row m = s.createRow(i++);
            m.createCell(0).setCellValue("Membres a jour / en retard");
            m.createCell(1).setCellValue(r.membresAJour() + " / " + r.membresEnRetard());
            i++;
            Row h = s.createRow(i++);
            h.createCell(0).setCellValue("Categorie"); h.createCell(1).setCellValue("Libelle"); h.createCell(2).setCellValue("Prevu (FCFA)");
            for (int c = 0; c < 3; c++) h.getCell(c).setCellStyle(bold);
            for (BudgetLigneDto l : r.lignesBudget()) {
                Row row = s.createRow(i++);
                row.createCell(0).setCellValue(l.categorie());
                row.createCell(1).setCellValue(l.libelle());
                row.createCell(2).setCellValue(l.montantPrevu().doubleValue());
            }
            i++;
            Row h2 = s.createRow(i++);
            h2.createCell(0).setCellValue("Depenses par categorie"); h2.createCell(1).setCellValue("Reel (FCFA)");
            h2.getCell(0).setCellStyle(bold); h2.getCell(1).setCellStyle(bold);
            for (Map.Entry<String, BigDecimal> e : r.depensesParCategorie().entrySet()) {
                Row row = s.createRow(i++);
                row.createCell(0).setCellValue(e.getKey());
                row.createCell(1).setCellValue(e.getValue().doubleValue());
            }
            for (int c = 0; c < 3; c++) s.setColumnWidth(c, 9000);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erreur de generation Excel", e);
        }
    }

    private int kv(Sheet s, int i, String k, BigDecimal v) {
        Row row = s.createRow(i);
        row.createCell(0).setCellValue(k);
        row.createCell(1).setCellValue(v.doubleValue());
        return i + 1;
    }
}
