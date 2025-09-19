<template>
  <div class="card" v-if="event">
    <h2 style="margin-top:0">{{event.title}}</h2>
    <p class="muted">{{event.description}}</p>
    <div class="sep"></div>

    <h3>Mes pronostics</h3>
    <div class="list" v-if="event.bets?.length">
      <div class="card" v-for="b in event.bets" :key="b.id" style="padding:12px">
        <div style="display:flex;align-items:center;justify-content:space-between;gap:10px">
          <div>
            <div><b>{{b.text}}</b></div>
            <div class="muted">Statut: <span class="pill">{{b.status}}</span></div>
          </div>
          <div class="row">
            <button class="ghost" :disabled="b.status!=='open'" @click="predict(b,'YES')">Oui</button>
            <button class="ghost" :disabled="b.status!=='open'" @click="predict(b,'NO')">Non</button>
          </div>
        </div>
      </div>
    </div>
    <p v-else class="muted">Aucun pari pour l’instant.</p>

    <p v-if="error" style="color:#fca5a5">{{error}}</p>
  </div>
  <div v-else class="card">Chargement…</div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { EventApi } from '../api'

const route = useRoute()
const eventId = route.params.eventId as string
const inviteId = route.params.inviteId as string
const sig = route.query.sig as string || ''

const event = ref<any>(null)
const error = ref('')

async function load(){
  try{ event.value = await EventApi.getAsInvite(eventId, inviteId, sig) }
  catch(e:any){ error.value = e.message }
}

async function predict(b:{id:string}, choice:'YES'|'NO'){
  try{ await EventApi.predict(eventId, b.id, inviteId, sig, choice); await load() }
  catch(e:any){ error.value = e.message }
}

onMounted(load)
</script>
