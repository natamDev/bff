<template>
  <div class="card">
    <h2>🏠 Accueil</h2>
    <InstallPwaButton />
    <div class="sep"></div>
    <h3>📅 Mes événements</h3>
    <div v-if="myEvents.length">
      <div
        v-for="ev in myEvents"
        :key="ev.eventId"
        class="row"
        style="justify-content: space-between; padding: 6px 0"
      >
        <div>
          <b>{{ ev.title || ev.eventId }}</b> — {{ ev.place }} |
          {{ ev.startAt ? new Date(ev.startAt).toLocaleDateString() : "?" }}
          → {{ ev.endAt ? new Date(ev.endAt).toLocaleDateString() : "?" }} ({{
            ev.closed ? "Clôturé" : "Ouvert"
          }})
        </div>
        <router-link :to="`/host/${ev.eventId}?sig=${ev.sig}`"
          >🔗 Ouvrir</router-link
        >
      </div>
    </div>
    <p v-else class="muted">Aucun événement enregistré.</p>

    <div class="sep"></div>
    <h3>🎲 Mes pronos</h3>
    <div v-if="myPronos.length">
      <div
        v-for="prono in myPronos"
        :key="prono.eventId + prono.inviteId"
        class="row"
        style="justify-content: space-between; padding: 6px 0"
      >
        <div>
          <b>{{ prono.title || prono.eventId }}</b> — {{ prono.place }} |
          {{
            prono.startAt ? new Date(prono.startAt).toLocaleDateString() : "?"
          }}
          →
          {{ prono.endAt ? new Date(prono.endAt).toLocaleDateString() : "?" }}
          ({{ prono.closed ? "Clôturé" : "Ouvert" }}) ({{
            prono.revoked ? "Révoquer" : "Approuvé"
          }})
        </div>
        <router-link
          :to="`/invite/${prono.eventId}/${prono.inviteId}?sig=${prono.sig}`"
        >
          🎯 Voir
        </router-link>
      </div>
    </div>
    <p v-else class="muted">Aucun prono enregistré.</p>
  </div>
</template>

<script setup lang="ts">
import InstallPwaButton from "../components/InstallPwaButton.vue";
import { ref, onMounted } from "vue";
import { EventApi } from "../api";

const myEvents = ref<any[]>([]);
const myPronos = ref<any[]>([]);

onMounted(async () => {
  const evts = JSON.parse(localStorage.getItem("myEvents") || "[]");
  const pronos = JSON.parse(localStorage.getItem("myPronos") || "[]");

  for (const e of evts) {
    try {
      const data = await EventApi.get(e.eventId);
      Object.assign(e, data);
    } catch {
      e.title = "(invalide)";
    }
  }

  for (const p of pronos) {
    try {
      const data = await EventApi.getAsInvite(p.eventId, p.inviteId, p.sig);
      Object.assign(p, data);
    } catch {
      p.title = "(invalide)";
    }
  }

  myEvents.value = evts;
  myPronos.value = pronos;
});
</script>
