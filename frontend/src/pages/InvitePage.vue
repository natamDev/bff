<template>
  <div class="card" v-if="event">
    <h2 style="margin-top: 0">{{ event.title }}</h2>
    <p class="muted">{{ event.description }}</p>

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
      Événement ouvert — vous pouvez pronostiquer
    </div>
    <div class="sep"></div>

    <h3>Mes pronostics</h3>
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
              Statut: <span class="pill">{{ statusLabel(b.status) }}</span>
            </div>
          </div>

          <div class="row">
            <button
              :class="[{ ghost: b.myChoice !== 'YES' }]"
              :disabled="event.closed || b.status !== 'open'"
              @click="predict(b, 'YES')"
            >
              Oui
            </button>
            <button
              :class="[{ ghost: b.myChoice !== 'NO' }]"
              :disabled="event.closed || b.status !== 'open'"
              @click="predict(b, 'NO')"
            >
              Non
            </button>
          </div>
          <div class="row">
            Résultat: <span>{{ resultLabel(b, event) }}</span>
          </div>
        </div>
      </div>
    </div>
    <p v-else class="muted">Aucun pari pour l’instant.</p>

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
const inviteId = route.params.inviteId as string;
const sig = (route.query.sig as string) || "";

const event = ref<any>(null);
const error = ref("");

async function load() {
  try {
    event.value = await EventApi.getAsInvite(eventId, inviteId, sig);
  } catch (e: any) {
    error.value = e.message;
  }
}

async function predict(b: { id: string }, choice: "YES" | "NO") {
  try {
    await EventApi.predict(eventId, b.id, inviteId, sig, choice);
    await load();
  } catch (e: any) {
    error.value = e.message;
  }
}

function statusLabel(status: string) {
  switch (status) {
    case "open":
      return "pari ouvert";
    case "true":
      return "pari validé : vrai";
    case "false":
      return "pari validé : faux";
    default:
      return status;
  }
}

function resultLabel(b: any, event: any) {
  // si pari pas encore résolu
  if (b.status === "open") {
    return "⏳ En attente de résultat";
  }

  // si aucun choix fait par l'invité
  if (!b.myChoice) {
    return event.closed ? "❌ Perdu" : "—";
  }

  // gagné si choix correspond au résultat
  if (
    (b.myChoice === "YES" && b.status === "true") ||
    (b.myChoice === "NO" && b.status === "false")
  ) {
    return "✅ Gagné";
  }

  // sinon perdu
  return "❌ Perdu";
}

onMounted(load);
</script>
