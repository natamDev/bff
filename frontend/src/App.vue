<template>
  <div class="wrap">
    <header
      class="row"
      style="align-items: center; justify-content: space-between"
    >
      <h1 style="margin: 0">🎯 Bets App</h1>
      <nav class="row" style="gap: 8px">
        <RouterLink to="/home">
          <button class="ghost">Mes events/pronos</button>
        </RouterLink>
        <RouterLink to="/create">
          <button class="ghost">Nouvel event</button>
        </RouterLink>
      </nav>
    </header>
    <div class="sep"></div>
    <RouterView />
  </div>
</template>
