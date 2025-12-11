<template>
  <div class="login-captcha-container-interno">
    <div class="header-captcha">
      <button class="btn-voltar" @click="$emit('back')">← Voltar</button>
    </div>

    <div class="login-card">
      <div class="card-header">
        <div class="profile-icon">
          <img :src="gerenteFoto" alt="Foto do Gerente" class="profile-picture" />
        </div>
        <h1>Acesso Captcha</h1>
        <p>Preencha os campos abaixo para obter acesso ao Captcha.</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin()">
        <div class="form-group">
          <label for="cpf">CPF</label>
          <input
            type="text"
            id="cpf"
            v-model="cpf"
            required
            placeholder="Digite seu CPF"
            minlength="14"
            maxlength="14"
            v-mask="'###.###.###-##'"
          />
        </div>

        <div class="form-group">
          <label for="senha">Senha de Segurança</label>
          <input
            type="password"
            id="senha"
            v-model="senha"
            placeholder="Digite sua senha de segurança"
            required
          />
        </div>

        <p v-if="erroLogin" class="mensagem-erro">
          CPF ou senha de segurança incorretos ou erro de comunicação.
        </p>

        <button type="submit" class="access-button">Acessar</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue'
import { useRouter } from 'vue-router'
import gerenteFoto from '@/assets/imgTCC.webp'

const emit = defineEmits(['back', 'logged-in'])

const router = useRouter()
const cpf = ref('')
const senha = ref('')
const erroLogin = ref(false)

async function handleLogin() {
  erroLogin.value = false

  const cpfLimpo = cpf.value.replace(/[.-]/g, '')

  const requestBody = {
    cpf: cpfLimpo,
    senha_seguranca: senha.value.trim(),
  }

  try {
    const response = await fetch('http://localhost:8080/safeip/loginCaptcha', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody),
    })

    const responseText = await response.text()

    if (!response.ok) {
      erroLogin.value = true
      throw new Error(responseText || 'CPF ou senha de segurança incorretos.')
    }

    localStorage.setItem('role', 'gerente')
    alert('Acesso de segurança autorizado! Abrindo conteúdo...')

    emit('logged-in')
  } catch (error) {
    console.error('Erro no login:', error)
    erroLogin.value = true
    alert(error.message || 'Erro de comunicação com o servidor. Tente novamente.')
  }
}
</script>

<style scoped>
.login-captcha-container-interno {
  padding: 20px;
  background: #f8f8f8;
  border-radius: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-y: auto;
}
.header-captcha {
  width: 100%;
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}
.btn-voltar {
  background: #5e2ced;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
  white-space: nowrap;
}
.btn-voltar:hover {
  background: #4a37e0;
}
.login-card {
  width: 100%;
  max-width: 450px;
  padding: 40px;
  background-color: #242424;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
  flex-shrink: 0;
  margin-top: 20px;
}
.card-header {
  margin-bottom: 30px;
}
.profile-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #4a4a4a;
  margin: 0 auto 10px;
}
.profile-picture {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto 10px;
  display: block;
}
.card-header h1 {
  font-size: 2em;
  font-weight: 700;
  margin-bottom: 5px;
  color: #5e2ced;
}
.card-header p {
  font-size: 1em;
  color: #aaaaaa;
  margin: 0;
}
.login-form {
  width: 100%;
  text-align: left;
}
.form-group {
  margin-bottom: 25px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 1.1em;
  color: #ffffff;
}
.form-group input {
  width: 100%;
  padding: 12px;
  background-color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 1em;
  color: #242424;
  box-sizing: border-box;
}
.access-button {
  width: 100%;
  padding: 12px;
  background-color: #5d35b9;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1.1em;
  font-weight: 600;
  cursor: pointer;
  margin-top: 30px;
  transition: background-color 0.3s ease;
}
.access-button:hover {
  background-color: #4c2aa3;
}
.mensagem-erro {
  color: #ff4d4d;
  font-weight: 600;
  margin-top: 15px;
  text-align: center;
}
</style>