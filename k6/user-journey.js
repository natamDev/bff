import http from "k6/http";
import { check, sleep } from "k6";

export let options = {
  vus: 1,
  iterations: 1,
};

const BASE = "https://calm-genevieve-bfftest-2a43b006.koyeb.app";

// Dataset complet (12 scénarios réalistes)
const EVENTS = [
  {
    title: "Match PSG - OM",
    description: "Classico au Vélodrome",
    place: "Paris",
    invites: ["Léo", "Sofia", "Karim"],
    betText: "Mbappé marque avant la 20e minute",
  },
  {
    title: "Anniversaire de Lola",
    description: "Soirée karaoké et défis",
    place: "Marseille",
    invites: ["Lola", "Nina", "Tom"],
    betText: "Tom chante au karaoké avant minuit",
  },
  {
    title: "Tournoi e-sport LoL",
    description: "Finale entre G2 et Fnatic",
    place: "Online",
    invites: ["Clara", "Maxime"],
    betText: "G2 gagne la première manche",
  },
  {
    title: "Afterwork du vendredi",
    description: "Bière et détente 🍻",
    place: "Lyon",
    invites: ["Julie", "Marc", "Antoine"],
    betText: "Qui commande le plus de pintes ?",
  },
  {
    title: "Vacances à Barcelone",
    description: "Trip soleil & tapas",
    place: "Barcelone",
    invites: ["Paul", "Emma", "Hugo"],
    betText: "Paul oublie sa crème solaire",
  },
  {
    title: "Tournoi FIFA",
    description: "Session gaming entre potes 🎮",
    place: "Chez Alex",
    invites: ["Alex", "Max", "Julien"],
    betText: "Max gagne au moins une finale",
  },
  {
    title: "Karaoké Night",
    description: "Nuit de chants et de défis musicaux",
    place: "Toulouse",
    invites: ["Marie", "Lucas", "Léa"],
    betText: "Marie chante du Céline Dion",
  },
  {
    title: "Défi Vélo",
    description: "Sortie sportive entre amis 🚲",
    place: "Bordeaux",
    invites: ["Pierre", "Nora", "Romain"],
    betText: "Qui arrive le dernier au sommet ?",
  },
  {
    title: "Soirée Pizza",
    description: "Bouffe et débats sans fin 🍕",
    place: "Nantes",
    invites: ["Camille", "Thomas", "Sarah"],
    betText: "Qui mange le plus de parts ?",
  },
  {
    title: "Soirée Jeux de société",
    description: "Compétition amicale 🎲",
    place: "Lille",
    invites: ["Clara", "David", "Sophie"],
    betText: "Clara gagne au moins 2 parties",
  },
  {
    title: "Tournoi Volley sur la plage",
    description: "Plage, sable et smashs 🏐",
    place: "Biarritz",
    invites: ["Hugo", "Anaïs", "Mathieu"],
    betText: "L’équipe 2 gagne un set",
  },
  {
    title: "Road Trip",
    description: "Escapade sur la Côte d’Azur 🚗",
    place: "Côte d’Azur",
    invites: ["Adrien", "Clara", "Yassine"],
    betText: "Qui s’endort le premier dans la voiture ?",
  },
];

// Choisir un scénario au hasard
function pickEvent() {
  return EVENTS[Math.floor(Math.random() * EVENTS.length)];
}

export default function () {
  const chosen = pickEvent();

  // 1. Créer un event
  const payload = {
    title: `${chosen.title} #${Math.floor(Math.random() * 10000)}`, // titre unique
    description: chosen.description,
    place: chosen.place,
    startAt: new Date().toISOString(),
    endAt: new Date(Date.now() + 3600 * 1000).toISOString(),
    invites: chosen.invites,
  };

  let createRes = http.post(`${BASE}/events`, JSON.stringify(payload), {
    headers: { "Content-Type": "application/json" },
  });

  check(createRes, { "create event 200": (r) => r.status === 200 });
  let body = createRes.json();
  let eventId = body.eventId;
  let hostSig = body.hostLink.split("sig=")[1];

  // 2. GET Event
  let getEvent = http.get(`${BASE}/events/${eventId}`);
  check(getEvent, { "get event 200": (r) => r.status === 200 });

  // 3. GET Invites
  let getInvites = http.get(
    `${BASE}/events/${eventId}/invites/links?sig=${hostSig}`
  );
  check(getInvites, { "get invites 200": (r) => r.status === 200 });

  // 4. GET Matrix
  let getMatrix = http.get(
    `${BASE}/events/${eventId}/predictions/matrix?sig=${hostSig}`
  );
  check(getMatrix, { "get matrix 200": (r) => r.status === 200 });

  // 5. Ajouter un pari
  let betRes = http.post(
    `${BASE}/events/${eventId}/bets?sig=${hostSig}`,
    JSON.stringify({ text: chosen.betText }),
    { headers: { "Content-Type": "application/json" } }
  );
  check(betRes, { "add bet 200": (r) => r.status === 200 });
  let betId = betRes.json().bets[0].id;

  // 6. Charger une page invité
  let inviteLinks = body.inviteLinks;
  let firstInviteName = Object.keys(inviteLinks)[0];
  let inviteUrl = inviteLinks[firstInviteName];
  let parts = inviteUrl.split("/");
  let inviteId = parts[parts.length - 1].split("?")[0];
  let inviteSig = inviteUrl.split("sig=")[1];

  console.log(`Étape 6 → inviteId: ${inviteId}, inviteSig: ${inviteSig}`);

  let inviteRes = http.get(
    `${BASE}/events/${eventId}/as-invite?inviteId=${inviteId}&sig=${inviteSig}`
  );
  check(inviteRes, { "invite page 200": (r) => r.status === 200 });

  // 7. Faire un pronostic
  console.log(`Étape 7 → eventId: ${eventId}, betId: ${betId}`);
  let predictRes = http.post(
    `${BASE}/events/${eventId}/bets/${betId}/predict?inviteId=${inviteId}&sig=${inviteSig}`,
    JSON.stringify({ choice: "YES" }),
    { headers: { "Content-Type": "application/json" } }
  );
  check(predictRes, { "predict 200": (r) => r.status === 200 });

  sleep(1);
}
