import fs from "fs";
import { ObjectId } from "bson";

function randomId(len = 12) {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
  return Array.from({ length: len }, () => chars[Math.floor(Math.random() * chars.length)]).join("");
}

function randomName() {
  const names = ["Léo", "Sofia", "Karim", "Emma", "Paul", "Hugo", "Clara", "Maxime", "Julie", "Antoine", "Sarah", "Romain", "Nina", "Lucas"];
  return names[Math.floor(Math.random() * names.length)];
}

function randomText() {
  const bets = [
    "Mbappé marque avant la 20e minute",
    "Quelqu’un oublie sa crème solaire",
    "Tom chante au karaoké",
    "Durée > 35 minutes",
    "Qui commande le plus de pintes ?",
    "Pizza mangée en moins de 30s",
    "Fnatic fait un ace",
    "G2 gagne la première manche"
  ];
  return bets[Math.floor(Math.random() * bets.length)];
}

function generateEvent(i) {
  const invites = [];
  for (let j = 0; j < 20; j++) {
    invites.push({
      _id: randomId(),
      name: randomName() + "_" + j,
      revoked: Math.random() < 0.05
    });
  }

  const bets = [];
  for (let k = 0; k < 50; k++) {
    const predictions = [];
    for (let p = 0; p < 200; p++) {
      const invite = invites[Math.floor(Math.random() * invites.length)];
      predictions.push({
        at: new Date().toISOString(),
        choice: Math.random() < 0.5 ? "YES" : "NO",
        inviteId: invite._id
      });
    }

    bets.push({
      _id: randomId(),
      text: randomText(),
      status: ["open", "true", "false"][Math.floor(Math.random() * 3)],
      predictions
    });
  }

  return {
    _id: { $oid: new ObjectId().toString() },
    title: "Event_" + i,
    description: "Generated event " + i,
    place: "Place_" + i,
    startAt: new Date().toISOString(),
    endAt: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
    hostSecret: randomId(16),
    closed: false,
    invites,
    bets
  };
}

const events = [];
for (let i = 0; i < 10; i++) {
  events.push(generateEvent(i));
}

fs.writeFileSync("events.json", JSON.stringify(events, null, 2));
console.log("✅ 10 events générés dans events.json");
