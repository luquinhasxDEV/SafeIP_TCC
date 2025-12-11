<template>
  <div class="tela_inicial">
    <div class="imagem">
      <img src="@/assets/900b0d49-1698-4a55-ae5a-63c8c3a29c1b.png" alt="Ilustração" />
    </div>

    <div class="forms">
      <div class="form-wrapper">
        <h1>Bem-Vindo!</h1>
        <p>Preencha os campos abaixo para continuar</p>

        <form @submit.prevent="login">
          <label>CPF</label>
          <input
            type="text"
            v-model="cpf"
            placeholder="000.000.000-00"
            required
            minlength="14"
            maxlength="14"
            v-mask="'###.###.###-##'"
          />

          <label>Senha</label>
          <input type="password" v-model="senha" required />

          <a href="#" class="esqueceu">Esqueceu sua senha?</a>

          <button type="submit">Login</button>
        </form>

        <div class="footer">
          <img src="@/assets/Capturadetela2025-08-04201110.png" alt="Logo SafeIP" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

const cpf = ref("");
const senha = ref("");

async function login() {
  try {
    const response = await axios.post("http://localhost:8080/safeip/login", {
      cpf: cpf.value.replace(/\D/g, ""), 
      senha: senha.value
    });

    const usuario = response.data;

    localStorage.removeItem("isIpBlocked"); 

    localStorage.setItem("cpfLogado", usuario.cpf);
    localStorage.setItem("role", usuario.cargo.toLowerCase());

    if (usuario.cargo.toLowerCase() === "gerente") {
      router.replace("/gerente/produtos");
    } else {
      router.replace("/funcionario/produtos");
    }

  } catch (error) {
    console.error(error);
    
    if (error.response && error.response.status === 403) {
      const errorMessage = error.response.data || 'Acesso Bloqueado.';
      
      if (errorMessage.includes("ACESSO BLOQUEADO") || errorMessage.includes("IP não autorizado")) {
         localStorage.setItem("isIpBlocked", "true"); 
         router.replace("/acesso-negado");
      } else {
         alert(errorMessage);
      }
      
    } else {
      alert("CPF ou senha incorretos.");
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Arial', sans-serif;
}

body {
  background-color: #f0f0f0;
  height: 100vh;
  overflow: hidden;
}

.tela_inicial {
  display: flex;
  height: 100vh;
  width: 100%;
  justify-content: space-around;
  align-items: center;
}

.imagem {
  flex: 1;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
}

.imagem img {
  width: 500px;
  height: 500px;
}

.forms {
  flex: 1;
  background-color: #252525;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.form-wrapper {
  width: 100%;
  max-width: 300px;
}

.forms h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.forms p {
  margin-bottom: 30px;
  font-size: 0.9rem;
  color: #ddd;
}

form {
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 1rem;
}

input {
  padding: 10px;
  margin-bottom: 20px;
  border: none;
  border-radius: 5px;
}

.esqueceu {
  color: #ccc;
  text-decoration: none;
  font-size: 0.8rem;
  margin-bottom: 20px;
  text-align: center;
}

button {
  padding: 12px;
  background: linear-gradient(90deg, #5b2be0, #7d4dff);
  border: none;
  border-radius: 5px;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

.footer {
  margin-top: 40px;
  text-align: center;
  font-size: 0.8rem;
  color: #aaa;
  width: 60px;
  margin-bottom: 5px;
}
</style>