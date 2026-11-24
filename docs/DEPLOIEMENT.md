# Deploiement (offres gratuites)

## 1. Base PostgreSQL
Neon, Supabase ou Render Postgres. Recuperer l'URL **JDBC** : `jdbc:postgresql://HOTE:5432/BASE?sslmode=require`, l'utilisateur et le mot de passe.

## 2. Back-end sur Render (Docker)
1. Pousser le depot sur GitHub. Render > New > Web Service > choisir le depot, **Root Directory : `backend`**, Runtime **Docker**.
2. Variables : `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (aleatoire, 32+ car.), `PV_SECRET`, `SEED_PASSWORD`, `SEED_ENABLED=true`, `CORS_ALLOWED_ORIGINS=https://VOTRE-FRONT.netlify.app`.
3. Health check : `/actuator/health`. Verifier `https://VOTRE-API.onrender.com/swagger-ui.html`.
Note : le plan gratuit se met en veille ; ouvrir l'API une minute avant la demonstration. La tache quotidienne ne tourne pas pendant la veille : utiliser les boutons "Generer les echeances" / "Rappels J-3" de l'espace comptable pour la demo.

## 3. Front-end sur Netlify ou Vercel
1. Editer `frontend/src/environments/environment.ts` : `apiUrl: 'https://VOTRE-API.onrender.com/api'`.
2. Netlify : depot GitHub, base `frontend` (le fichier `netlify.toml` configure build et redirection SPA). Vercel : Root Directory `frontend`, preset Angular (`vercel.json` fournit la redirection).
3. Mettre l'URL finale du front dans `CORS_ALLOWED_ORIGINS` cote back-end, puis redeployer.
