# BetForFriend — Tests de charge

Ce projet contient des scripts **k6** pour tester les performances de l’API BetForFriend.

## 📦 Installation

1. Installer [k6](https://k6.io/docs/getting-started/installation/).
   ```bash
   # Linux / MacOS
   brew install k6

   # Windows (choco)
   choco install k6
   ```

2. Cloner ce repo et installer les dépendances Node (uniquement pour générer les rapports HTML/JSON).
   ```bash
   npm install
   ```

---

## 🚀 Scripts disponibles

### 1. Test de charge simple
Lance le scénario par défaut défini dans `load-test.js` :
```bash
npm run load
```

### 2. Export JSON
Sauvegarde les résultats dans `reports/<date>.json` :
```bash
npm run load:json
```

### 3. Export HTML
Génère un rapport visuel (`reports/result.html`) :
```bash
npm run load:html
```

> ⚠️ Nécessite `k6-to-html` installé globalement :
```bash
npm install -g k6-to-html
```

### 4. Scénario complet utilisateur
Exécute un **user journey** (création d’event → ajout pari → invite → pronostic) :
```bash
npm run user
```

---

## 📁 Structure

- `load-test.js` → test de charge multi-scénarios (host + invites)
- `user-journey.js` → parcours utilisateur complet
- `reports/` → dossiers où seront stockés les exports JSON/HTML
