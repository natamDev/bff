<template>
  <div class="card">
    <h2>Créer un événement</h2>
    <div class="row">
      <div class="col"><label>Titre<br /><input v-model="title" /></label></div>
      <div class="col"><label>Lieu<br /><input v-model="place" /></label></div>
    </div>
    <div class="row">
      <div class="col"><label>Début<br /><input v-model="startAt" placeholder="2025-09-19T19:00:00+02:00"/></label></div>
      <div class="col"><label>Fin<br /><input v-model="endAt" placeholder="2025-09-19T20:00:00+02:00"/></label></div>
    </div>
    <label>Description<br /><textarea v-model="description" rows="3"></textarea></label>
    <div class="sep"></div>
    <label>Invités (un par ligne)
      <textarea v-model="inviteText" rows="4" placeholder="Léo
Franck
Lola"></textarea>
    </label>
    <div class="row" style="justify-content:flex-end;margin-top:16px">
      <button @click="create">Créer</button>
    </div>

    <div v-if="res" class="sep"></div>
    <div v-if="res" class="card">
      <h3>Liens générés</h3>
      <p><b>Host:</b> <a :href="res.hostLink" target="_blank">{{res.hostLink}}</a></p>
      <div class="list">
        <div v-for="(url, name) in res.inviteLinks" :key="name"><b>{{name}}:</b> <a :href="url" target="_blank">{{url}}</a></div>
      </div>
    </div>
    <p v-if="error" style="color:#fca5a5">{{error}}</p>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { EventApi } from '../api'

const router = useRouter()
const title = ref('')
const place = ref('')
const startAt = ref('')
const endAt = ref('')
const description = ref('')
const inviteText = ref('')
const res = ref<any>(null)
const error = ref('')

async function create(){
  error.value=''
  try{
    const invites = inviteText.value.split(/\n+/).map(s=>s.trim()).filter(Boolean)
    const r = await EventApi.create({ title: title.value, description: description.value, place: place.value, startAt: startAt.value, endAt: endAt.value, invites })
    res.value = r
    const url = new URL(r.hostLink)
    const eventId = url.pathname.split('/')[2]
    const sig = url.searchParams.get('sig') || ''
    router.push({ path: `/host/${eventId}`, query: { sig }})
  }catch(e:any){ error.value = e.message }
}
</script>
