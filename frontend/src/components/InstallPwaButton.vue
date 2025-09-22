<template>
  <div>
    <!-- Bouton Android/Chrome -->
    <button v-if="canInstall" @click="installApp">📲 Installer l'app</button>

    <!-- Message iOS -->
    <div v-else-if="isIos" class="muted ios-hint">
      📱 Sur iPhone/iPad :<br />
      Ouvrez Safari → Menu <b>Partager</b> → <b>Ajouter à l’écran d’accueil</b>.
    </div>

    <!-- Message fallback -->
    <div v-else-if="!isSupported" class="muted">
      ⚠️ Installation PWA non supportée sur ce navigateur.
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";

const deferredPrompt = ref<any>(null);
const canInstall = ref(false);
const isIos = ref(false);
const isSupported = ref(true);

onMounted(() => {
  const ua = window.navigator.userAgent;

  // Détection iOS Safari
  isIos.value = /iPhone|iPad|iPod/.test(ua) && /Safari/.test(ua);

  // Android/Chrome/Edge/Brave → écoute de beforeinstallprompt
  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault();
    deferredPrompt.value = e;
    canInstall.value = true;
  });

  // Desktop Chrome/Edge → similaire, mais plus restrictif
  if (window.matchMedia("(display-mode: standalone)").matches) {
    // déjà installée
    canInstall.value = false;
  }

  // Vérification support PWA
  if (!("serviceWorker" in navigator)) {
    isSupported.value = false;
  }
});

async function installApp() {
  if (!deferredPrompt.value) return;

  deferredPrompt.value.prompt();
  const choice = await deferredPrompt.value.userChoice;

  if (choice.outcome === "accepted") {
    console.log("✅ PWA installée");
  } else {
    console.log("❌ Installation refusée");
  }

  deferredPrompt.value = null;
  canInstall.value = false;
}
</script>

<style scoped>
.muted {
  font-size: 0.9em;
  color: #94a3b8;
  margin-top: 8px;
}
.ios-hint {
  border: 1px dashed #22c55e;
  padding: 8px;
  border-radius: 8px;
  background: rgba(34, 197, 94, 0.1);
}
</style>
