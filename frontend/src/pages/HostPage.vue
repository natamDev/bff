<template>
  <div class="card" v-if="event">
    <div
      class="row"
      style="align-items: center; justify-content: space-between"
    >
      <h2 style="margin: 0">Admin — {{ event.title }}</h2>
      <div class="row" style="gap: 8px">
        <button class="ghost" @click="refresh">Rafraîchir</button>
        <button v-if="!event.closed" @click="closeEvent">Clôturer</button>
        <button v-else @click="openEvent">Ouvrir</button>
      </div>
    </div>
    <p class="muted">{{ event.description }}</p>

    <div class="sep"></div>
    <div class="row">
      <div class="col">
        <label
          >Nouveau pari<br /><input
            v-model="newBet"
            placeholder="A1 marque avant 10:00"
        /></label>
      </div>
      <div class="col" style="align-self: end">
        <button @click="addBet" :disabled="!newBet">Ajouter</button>
      </div>
    </div>

    <div class="sep"></div>
    <h3>Paris</h3>
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
              Statut: <span class="pill">{{ b.status }}</span>
            </div>
          </div>
          <div class="row">
            <button class="ghost" @click="setStatus(b, 'open')">Ouvrir</button>
            <button class="ghost" @click="setStatus(b, 'true')">Vrai</button>
            <button class="ghost" @click="setStatus(b, 'false')">Faux</button>
          </div>
        </div>
      </div>
    </div>
    <p v-else class="muted">Aucun pari pour l’instant.</p>

    <div class="sep"></div>
    <div class="card">
      <h3>Liens invités</h3>
      <div class="list">
        <div v-for="(url, name) in inviteLinks" :key="name">
          <b>{{ name }}:</b> <a :href="url" target="_blank">{{ url }}</a>
        </div>
      </div>
    </div>

    <div class="sep"></div>
    <div class="card">
      <h3>Matrix des pronostics</h3>
      <div v-if="matrix.length" class="list">
        <div
          class="card"
          v-for="r in matrix"
          :key="r.betId"
          style="padding: 12px"
        >
          <div style="margin-bottom: 6px">
            <b>{{ r.text }}</b> — <span class="pill">{{ r.status }}</span>
          </div>
          <div class="list">
            <div
              v-for="(choice, name) in r.answers"
              :key="name"
              style="display: flex; justify-content: space-between"
            >
              <div>{{ name }}</div>
              <div>{{ choice }}</div>
            </div>
          </div>
        </div>
      </div>
      <p v-else class="muted">Aucun pronostic pour l’instant.</p>
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

async function refresh() {
  try {
    event.value = await EventApi.get(eventId);
    inviteLinks.value = await EventApi.listInviteLinks(eventId, sig);
    matrix.value = await EventApi.matrix(eventId, sig);
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

onMounted(refresh);
</script>
