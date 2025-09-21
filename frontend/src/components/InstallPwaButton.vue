<template>
  <button v-if="deferredPrompt" @click="installApp">📲 Installer l'app</button>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";

const deferredPrompt = ref<any>(null);

onMounted(() => {
  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault();
    deferredPrompt.value = e; // on stocke l’event
  });
});

async function installApp() {
  if (!deferredPrompt.value) return;
  deferredPrompt.value.prompt();
  const choice = await deferredPrompt.value.userChoice;
  if (choice.outcome === "accepted") {
    console.log("PWA installée ✅");
  } else {
    console.log("Installation refusée ❌");
  }
  deferredPrompt.value = null;
}
</script>
