<template>
  <div class="logs-container-interno">
    <div class="header-logs">
      
      <button class="btn-voltar" @click="$emit('back')">← Voltar</button>
      <h1 class="titulo-log">Registro de Modificações</h1>

      <div class="controles">
        <div class="filtro-data">
          <label for="data-filtro">Filtro de Data:</label>
          <input
            type="date"
            id="data-filtro"
            v-model="dataSelecionada"
            @change="filtrarLogs"
          />
        </div>
      </div>
    </div>

    <div v-if="loading" class="mensagem-status">
      <p>Carregando logs...</p>
    </div>

    <div v-else-if="logsExibidos.length === 0" class="sem-logs">
      <p>
          Sem Dados Disponíveis Para a Data Selecionada.
      </p>
    </div>

    <div v-else class="logs-listagem">
      <div
        v-for="log in logsExibidos"
        :key="log.id"
        class="log-card"
      >
        <div class="log-content">
          <div class="log-tempo-data">
            <strong class="hora">{{ log.hora }}</strong>
            <small v-if="!dataSelecionada" class="data-log-completa">
              {{ log.data }}
            </small>
          </div>

          <div class="log-detalhes">
              <p class="acao-text">{{ log.acao }}</p>
              <div class="usuario-info">
                  <span class="label">Usuário:</span>
                  <span class="cpf">{{ log.usuario }}</span>
              </div>
          </div>
        </div>
      </div>
    </div>

    <button class="btn-atualizar" @click="fetchLogs">Recarregar Dados</button>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, defineEmits } from 'vue'
import axios from 'axios'

const emit = defineEmits(['back'])

const logsBrutos = ref([])
const dataSelecionada = ref('')
const loading = ref(true)

async function fetchLogs() {
  loading.value = true
  try {
    const cpfLogado = localStorage.getItem("cpfLogado")?.trim();
    const res = await axios.get('http://localhost:8080/safeip/logs', {
        headers: { 'cpf-logado': cpfLogado }
    })
    logsBrutos.value = res.data || []
  } catch (err) {
    console.error('Erro ao carregar logs', err)
    logsBrutos.value = []
  } finally {
    loading.value = false
  }
}

function filtrarLogs() {
}

const logsExibidos = computed(() => {
    let logsParaExibir = logsBrutos.value; 

    if (dataSelecionada.value) {
        logsParaExibir = logsParaExibir.filter(item => {
            if (!item.dataHora) return false;
            const dataLog = item.dataHora.split('T')[0];
            return dataLog === dataSelecionada.value;
        });
    }

    const logsFormatados = logsParaExibir.map(item => {
        const [data, hora] = item.dataHora ? item.dataHora.split('T') : ['', ''];
        return {
            id: item.id,
            usuario: item.usuario || 'Desconhecido',
            acao: item.acao,
            dataHoraCompleta: item.dataHora,
            data: data.split('-').reverse().join('/'), 
            hora: hora.split('.')[0]
        };
    });

    logsFormatados.sort((a, b) => {
        if (a.dataHoraCompleta > b.dataHoraCompleta) return -1; 
        if (a.dataHoraCompleta < b.dataHoraCompleta) return 1;  
        return 0;
    });

    return logsFormatados;
});


onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.logs-container-interno {
    padding: 20px;
    background: #f8f8f8;
    border-radius: 10px;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.header-logs {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
}

.titulo-log {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0d1b3e;
  margin: 0;
}

.controles {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-left: auto;
}

.filtro-data {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0d1b3e;
  font-weight: 500;
}
.filtro-data input {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
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

/* Estilos de Lista de Logs */
.logs-listagem {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-height: calc(100% - 180px);
    overflow-y: auto;
    flex-grow: 1;
}

.log-card {
  background: #0b2a57;
  color: #fff;
  border-radius: 10px;
  padding: 15px 20px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.log-content {
    display: flex;
    gap: 20px;
}

.log-tempo-data {
    width: 100px; 
    flex-shrink: 0;
    text-align: right;
    border-right: 1px solid rgba(255, 255, 255, 0.2);
    padding-right: 20px;
}

.hora {
    display: block; 
    font-size: 1.1rem;
    font-weight: 700;
    color: #4b8bff;
    margin-bottom: 2px;
}

.data-log-completa {
    display: block;
    font-size: 0.8rem;
    color: #ffd700;
    font-weight: 500;
}

.log-detalhes {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.acao-text {
    font-size: 0.9rem;
    line-height: 1.4;
    margin: 0;
    padding-bottom: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.usuario-info {
    font-size: 0.8rem;
    opacity: 0.9;
    display: flex;
    gap: 5px;
}

.label {
    font-weight: 500;
    color: #a0a8b4;
}

.cpf {
    font-weight: 700;
    color: #ff9900;
}

/* Estilos de Status e Botão */
.sem-logs, .mensagem-status {
  text-align: center;
  color: #333;
  font-weight: 500;
  padding: 15px;
  background-color: #eee;
  border-radius: 10px;
  margin-bottom: 10px;
}

.btn-atualizar {
  display: block;
  margin: 20px auto 0;
  background-color: #4b8bff;
  border: none;
  padding: 10px 30px;
  border-radius: 20px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-atualizar:hover {
  background-color: #366bdb;
}
</style>