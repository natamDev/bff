<template>
  <div class="card" v-if="event">
    <div
      class="row"
      style="align-items: center; justify-content: space-between"
    >
      <h2 style="margin: 0">Admin — {{ event.title }}</h2>
    </div>
    <p class="muted">{{ event.description }}</p>
    <ul
      class="muted"
      style="margin: 8px 0 16px; padding-left: 16px; line-height: 1.4"
    >
      <li><b>Lieu :</b> {{ event.place }}</li>
      <li><b>Début :</b> {{ new Date(event.startAt).toLocaleString() }}</li>
      <li><b>Fin :</b> {{ new Date(event.endAt).toLocaleString() }}</li>
    </ul>
    <div class="sep"></div>
    <div
      class="row"
      style="align-items: center; justify-content: space-between"
    >
      <div
        v-if="event.closed"
        class="muted"
        style="margin: 12px 0; font-weight: 500; color: #f87171"
      >
        Événement clôturé — pronostics verrouillés
      </div>
      <div
        v-else
        class="muted"
        style="margin: 12px 0; font-weight: 500; color: #22c55e"
      >
        Événement ouvert — vous pouvez pronostiquer <br />
      </div>
      <span class="muted">
        Si l'evenement est cloturé, tous les pronostics sont vérouillés
      </span>
      <div class="row" style="gap: 8px">
        <button v-if="!event.closed" class="danger" @click="closeEvent">
          Clôturer
        </button>
        <button v-else @click="openEvent">Ouvrir</button>
      </div>
    </div>
    <div class="sep"></div>
    <div
      class="row"
      style="align-items: center; justify-content: space-between"
    >
      <label
        >Nouveau pari<br /><input
          v-model="newBet"
          placeholder="A1 marque avant 10:00"
      /></label>
      <div class="row" style="gap: 8px">
        <button @click="addBet" :disabled="!newBet || event.closed">
          Ajouter
        </button>
      </div>
    </div>
    <div class="sep"></div>
    <h3>Paris</h3>
    <div class="muted">Si un pari est fermé, le pronostic est vérouillé</div>
    <div class="list" v-if="event.bets?.length">
      <div
        class="card"
        v-for="b in event.bets"
        :key="b.id"
        style="padding: 12px"
      >
        <div
          style="
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
          "
        >
          <div>
            <div>
              <b>{{ b.text }}</b>
            </div>
            <div class="muted">
              <span class="pill">{{ statusLabel(b.status) }}</span>
            </div>
          </div>
          <div class="row">
            <button
              :class="{ ghost: b.status === 'open' }"
              :disabled="event.closed"
              @click="setStatus(b, 'open')"
            >
              {{ b.status === "open" ? "En cours" : "Ouvrir" }}
            </button>
            <button
              :class="{ ghost: b.status !== 'true' }"
              :disabled="event.closed || b.status !== 'open'"
              @click="setStatus(b, 'true')"
            >
              Oui
            </button>
            <button
              :class="{ ghost: b.status !== 'false' }"
              :disabled="event.closed || b.status !== 'open'"
              @click="setStatus(b, 'false')"
            >
              Non
            </button>
            <button
              :class="{ danger: true }"
              @click="deleteBet(b)"
              :disabled="event.closed"
            >
              🗑 Supprimer
            </button>
          </div>
        </div>
      </div>
    </div>
    <p v-else class="muted">Aucun pari pour l’instant.</p>

    <div class="sep"></div>
    <div
      class="row"
      style="align-items: center; justify-content: space-between"
    >
      <label>
        Nouvel invité<br />
        <input v-model="newInvite" placeholder="Nom de l’invité" />
      </label>

      <div class="row" style="gap: 8px">
        <button @click="addInvite" :disabled="!newInvite">Ajouter</button>
      </div>
    </div>

    <div class="sep"></div>
    <div class="card">
      <h3>Liens invités</h3>
      <div class="list">
        <div
          v-for="i in inviteLinks"
          :key="i.id"
          class="row"
          style="align-items: center; justify-content: space-between"
        >
          <b>{{ i.name }}</b>
          <button
            :class="{ ghost: i.revoked }"
            :disabled="i.revoked"
            @click="shareInvite(i)"
          >
            🔗 Partager
          </button>

          <button v-if="!i.revoked" class="danger" @click="revokeInvite(i)">
            🚫 Révoquer
          </button>
          <button v-else @click="approveInvite(i)">✅ Approuver</button>
        </div>
      </div>
    </div>
    <div class="sep"></div>

    <div class="card">
      <h3>
        Matrix des pronostics
        <button class="ghost" @click="refreshMatrix">Rafraîchir</button>
      </h3>
      <div v-if="matrix.length" class="list">
        <div
          class="card"
          v-for="r in matrix"
          :key="r.betId"
          style="padding: 12px"
        >
          <div style="margin-bottom: 6px">
            <b>{{ r.text }}</b> —
            <span class="pill">{{ statusLabel(r.status) }}</span>
          </div>
          <div class="list">
            <div
              v-for="(choice, name) in r.answers"
              :key="name"
              style="display: flex; justify-content: space-between"
            >
              <div>{{ name }}</div>
              <div>{{ choiceLabel(choice) }}</div>
              <div>{{ resultLabel(choice, r, event) }}</div>
            </div>
          </div>
        </div>
      </div>
      <p v-else class="muted">Aucun pronostic pour l’instant.</p>
    </div>

    <div class="card">
      <h3>
        Classement des invités
        <button class="ghost" @click="refreshScores">Rafraîchir</button>
      </h3>
      <div v-if="scores.length" class="list">
        <div
          v-for="s in scores"
          :key="s.inviteId"
          class="row"
          style="justify-content: space-between; padding: 8px 0"
        >
          <b>{{ s.name }}</b>
          <span>{{ s.score }} point{{ s.score > 1 ? "s" : "" }}</span>
        </div>
      </div>
      <p v-else class="muted">Aucun score calculé pour l’instant.</p>
    </div>

    <p v-if="error" style="color: #fca5a5">{{ error }}</p>
  </div>
  <div v-else class="card">Chargement…</div>
</template>
<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { EventApi } from "../api";

const route = useRoute();
const eventId = route.params.eventId as string;
const sig = (route.query.sig as string) || "";

const event = ref<any>(null);
const inviteLinks = ref<Record<string, string>>({});
const newBet = ref("");
const error = ref("");
const matrix = ref<any[]>([]);
const scores = ref<any[]>([]);

async function refresh() {
  try {
    event.value = await EventApi.get(eventId);
    inviteLinks.value = await EventApi.listInviteLinks(eventId, sig);
    matrix.value = await EventApi.matrix(eventId, sig);
    scores.value = await EventApi.scores(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function refreshMatrix() {
  try {
    matrix.value = await EventApi.matrix(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function refreshScores() {
  try {
    scores.value = await EventApi.scores(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function addBet() {
  if (!newBet.value) return;
  try {
    event.value = await EventApi.addBet(eventId, sig, newBet.value);
    newBet.value = "";
    matrix.value = await EventApi.matrix(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function deleteBet(b: { id: string }) {
  if (!confirm("Supprimer ce pari et tous ses pronostics ?")) return;
  try {
    event.value = await EventApi.deleteBet(eventId, b.id, sig);
    matrix.value = await EventApi.matrix(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

const newInvite = ref("");

async function addInvite() {
  try {
    event.value = await EventApi.addInvite(eventId, sig, newInvite.value);
    newInvite.value = "";
    inviteLinks.value = await EventApi.listInviteLinks(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function revokeInvite(i) {
  if (!confirm(`Révoquer ${i.name} ?`)) return;
  try {
    event.value = await EventApi.revokeInvite(eventId, i.id, sig);
    inviteLinks.value = await EventApi.listInviteLinks(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function approveInvite(i) {
  try {
    event.value = await EventApi.approveInvite(eventId, i.id, sig);
    inviteLinks.value = await EventApi.listInviteLinks(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}
async function setStatus(b: { id: string }, status: "open" | "true" | "false") {
  try {
    event.value = await EventApi.setBetStatus(eventId, b.id, sig, status);
    matrix.value = await EventApi.matrix(eventId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function closeEvent() {
  try {
    await fetch(
      `${
        import.meta.env.VITE_API_BASE
      }/events/${eventId}/close?sig=${encodeURIComponent(sig)}`,
      { method: "POST" }
    );
    await refresh();
  } catch (e: any) {
    error.value = e.message;
  }
}

async function openEvent() {
  try {
    await fetch(
      `${
        import.meta.env.VITE_API_BASE
      }/events/${eventId}/open?sig=${encodeURIComponent(sig)}`,
      { method: "POST" }
    );
    await refresh();
  } catch (e: any) {
    error.value = e.message;
  }
}

function statusLabel(status: string) {
  switch (status) {
    case "open":
      return "Statut ouvert : prono en cours";
    case "true":
      return " Statut fermé : prono validé : Oui";
    case "false":
      return "Statut fermé : prono validé : Non";
    default:
      return status;
  }
}

function resultLabel(choice: any, b: any, event: any) {
  // si pari pas encore résolu
  if (b.status === "open") {
    return "⏳ En attente de résultat";
  }

  // si aucun choix fait par l'invité
  if (!choice) {
    return event.closed || b.status !== "open" ? "❌ Perdu" : "—";
  }

  // gagné si choix correspond au résultat
  if (
    (choice === "YES" && b.status === "true") ||
    (choice === "NO" && b.status === "false")
  ) {
    return "✅ Gagné";
  }

  // sinon perdu
  return "❌ Perdu";
}

function choiceLabel(choice: string) {
  if (choice === "YES") return "Oui";
  if (choice === "NO") return "Non";
  return choice;
}

function parseInviteUrl(rawUrl: string) {
  const url = new URL(rawUrl);

  // Extraire le hash (car Vue Router en mode hash)
  let cleanPath = "";
  if (url.hash && url.hash.startsWith("#")) {
    cleanPath = url.hash.slice(1).split("?")[0]; // enlève le #
  } else {
    cleanPath = url.pathname;
  }

  // Découpe en segments
  const pathSegments = cleanPath.split("/").filter(Boolean);

  // On s’attend à un format: /invite/:eventId/:inviteId
  const eventId = pathSegments[1];
  const inviteId = pathSegments[2];

  // Récupérer sig (soit dans hash, soit dans query string)
  const params = new URLSearchParams(url.hash.split("?")[1] || url.search);
  const sig = params.get("sig") || "";

  return { eventId, inviteId, sig };
}

async function shareInvite(i: { url: string; name: string }) {
  if (!i.url) return;

  if (navigator.share) {
    try {
      await navigator.share({
        title: `Invitation pour ${i.name}`,
        url: i.url,
      });
    } catch (e) {
      console.log("Partager annulé", e);
    }
  } else {
    // fallback copie dans le presse-papier
    await navigator.clipboard.writeText(i.url);
    alert("Lien copié dans le presse-papier !");
  }
}
onMounted(refresh);
</script>
