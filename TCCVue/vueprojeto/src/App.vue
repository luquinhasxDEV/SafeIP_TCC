<template>
  <div id="app">
    <!-- Aqui vai aparecer a view atual -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background: #f8f8f8; 
  font-family: 'Arial', sans-serif; 
}
</style>