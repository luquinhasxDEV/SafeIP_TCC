<template>
  <aside class="sidebar">
    <div class="perfil">
      <img src="@/assets/imgTCC.webp" alt="Foto" class="foto-perfil">
      <div>
        <h3>Gerente</h3>
        <p>SafeIP</p>
      </div>
    </div>

    <nav>
      <router-link to="/gerente/produtos"><span>🛒</span> Produtos</router-link>
      <router-link to="/gerente/registro"><span>💰</span> Vendas</router-link>
      <router-link to="/gerente/lista"><span>🔍</span> Lista</router-link>

      <a href="#" @click.prevent="logout"><span>🚪</span> Sair</a>
    </nav>

    <div class="logo">
      <p>SafeIP</p>
      <small>Este software foi desenvolvido pela SafeIP.</small>
    </div>
  </aside>
</template>

<script setup>
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

const logout = async () => {
  const cpfLogado = localStorage.getItem("cpfLogado");
  
  if (cpfLogado) {
    try {
      await axios.post("http://localhost:8080/safeip/sair", { cpf: cpfLogado });
    } catch (error) {
      console.warn("Aviso: Falha ao registrar log de saída no backend.", error.message);
    }
  }
  localStorage.removeItem("cpfLogado");
  localStorage.removeItem("role"); 
  localStorage.removeItem("isIpBlocked"); 

  router.replace("/login"); 
};
</script>

<style scoped>
.sidebar {
  background: #1d1b1f;
  color: #fff;
  width: 400px;
  height: 100vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.perfil {
  display: flex;
  align-items: center;
  gap: 10px;
}

.foto-perfil {
  border-radius: 50%;
  width: 60px;
  height: 60px;
}

nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 30px;
}

nav a {
  text-decoration: none;
  color: #fff;
  padding: 10px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: 0.3s;
}

nav a.router-link-active,
nav a:hover {
  background: #5e2ced;
}

.logo {
  font-size: 12px;
  text-align: center;
}
</style>