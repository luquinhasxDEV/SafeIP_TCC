<template>
  <div class="captcha-gerador-container">
    <main class="conteudo-modal">
      <div class="header-captcha">
        <button class="btn-padrao-voltar" @click="$emit('back')">← Voltar</button>
        <h2 class="title">CAPTCHA (Códigos de Uso Único)</h2>
        <div></div> 
      </div>
      
      <p class="subtitle">Clique em "Gerar" para obter os códigos temporários.</p>

      <div class="generators-wrapper">
        <div class="code-block">
          <h3>Código para Liberação de Venda:</h3>
          <button class="generate-button green" @click="generateSalesCode">
            Gerar
          </button>
          <div class="code-display">
            <span class="code">{{ salesCode || '--- Clique em Gerar ---' }}</span>
          </div>
        </div>

        <div class="code-block">
          <h3>Código para Acessar o Sistema:</h3>
          <button class="generate-button green" @click="generateAccessCode">
            Gerar
          </button>
          <div class="code-display">
            <span class="code">{{ accessCode || '--- Clique em Gerar ---' }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue'; 
import axios from 'axios';

const emit = defineEmits(['back']); 
const salesCode = ref(null);
const accessCode = ref(null);

function generateBlock(length = 3) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function generateFormattedCode() {
  return `${generateBlock()}-${generateBlock()}-${generateBlock()}-${generateBlock(2)}`;
}

async function sendCodeToBackend(codigo, tipo) {
  try {
    const cpfLogado = localStorage.getItem('cpfLogado')?.trim();
    if (!cpfLogado) {
      alert("CPF do usuário não encontrado no localStorage!");
      return;
    }

    console.log("Enviando para backend:", { codigo, tipo, cpfLogado });

    const response = await axios.post(
      'http://localhost:8080/safeip/codigoCaptcha',
      { codigo, tipo },
      {
        headers: {
          'Content-Type': 'application/json',
          'cpf-logado': cpfLogado
        }
      }
    );

    console.log(`Código ${tipo} enviado com sucesso:`, response.data);
  } catch (error) {
    if (error.response) {
      console.error("Erro do backend:", error.response.status, error.response.data);
      alert(`Erro do backend: ${error.response.status}`);
    } else {
      console.error("Erro de rede ou Axios:", error.message);
      alert("Erro ao enviar o código para o backend.");
    }
  }
}

async function generateSalesCode() {
  const code = generateFormattedCode();
  salesCode.value = code;
  await sendCodeToBackend(code, 'Venda');
}

async function generateAccessCode() {
  const code = generateFormattedCode();
  accessCode.value = code;
  await sendCodeToBackend(code, 'Acesso');
}
</script>

<style scoped>

.captcha-gerador-container {
    flex-grow: 1; 
    display: flex;
    flex-direction: column; 
    overflow-y: auto;
}

.conteudo-modal {
    flex: 1; 
    display: flex;
    flex-direction: column; 
    justify-content: flex-start; 
    align-items: center; 
    padding: 20px 40px; 
    background-color: #f8f8f8; 
    border-radius: 10px;
}

.header-captcha { 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    border-bottom: 2px solid #e0e0e0; 
    padding-bottom: 15px; 
    margin-bottom: 25px; 
    width: 100%;
}

.btn-padrao-voltar {
    background: #5e2ced; 
    color: white; 
    border: none; 
    padding: 10px 18px; 
    border-radius: 5px; 
    cursor: pointer; 
    font-weight: 600; 
    transition: background 0.2s; 
    white-space: nowrap; 
    font-size: 1em;
}

.btn-padrao-voltar:hover { 
    background: #4a37e0; 
}

.title { 
  font-size: 1.8rem; 
  color: #242424; 
  font-weight: 700; 
  flex-grow: 1; 
  text-align: center;
  margin: 0 20px;
}
.subtitle { 
  font-size: 1.1em; 
  color: #666; 
  margin-bottom: 40px; 
}

.generators-wrapper { 
  display: flex; 
  gap: 30px; 
  width: 100%; 
  max-width: 750px; 
  margin-bottom: 20px; 
  flex-wrap: wrap; 
  justify-content: center; 
}

.code-block { 
  flex: 1 1 300px; 
  padding: 20px; 
  background-color: #194a73; 
  border-radius: 8px; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); 
}

.code-block h3 { 
  font-size: 1.2em; 
  color: #ffffff; 
  margin-bottom: 20px; 
  font-weight: 500; 
  text-align: center; 
}

.generate-button { 
  padding: 10px 25px; 
  color: white; 
  border: none; 
  border-radius: 4px; 
  font-size: 1.1em; 
  font-weight: 600; 
  cursor: pointer; 
  transition: background-color 0.3s ease; 
  margin-bottom: 20px; 
  background-color: #38c172; 
}

.generate-button:hover { 
  background-color: #2da862; 
}

.code-display { 
  padding: 10px; 
  margin-top: 5px; 
  background-color: #333; 
  border-radius: 4px; 
  min-height: 40px; 
  width: 100%; 
  display: flex;
  justify-content: center;
  align-items: center;
}

.code { 
  font-size: 1.2rem; 
  font-weight: 700; 
  letter-spacing: 1px; 
  font-family: 'Consolas', monospace; 
  color: #ffcc00; 
  word-break: break-all; 
  text-align: center;
}
</style>