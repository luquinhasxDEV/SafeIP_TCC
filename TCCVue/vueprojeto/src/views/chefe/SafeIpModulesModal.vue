<template>
  <div class="safeip-modal-container">
    <div class="safeip-modal-overlay" @click.self="$emit('close')">
      <div class="safeip-modal-content">
        <div v-if="currentView === 'modules'" class="modules-view">
          <div class="safeip-header">
            <h1 class="titulo">SafeIP</h1>
            <button @click="$emit('close')" class="btn-close">✖</button>
          </div>

          <h3 class="subtitulo">
            No mercado de luxo, a confiança é o seu maior ativo, e a segurança de dados e transações
            é não negociável.
          </h3>
          <div class="cards-grid">
            <div class="card" v-for="(modulo, index) in modulos" :key="index">
              <div class="icone">
                <img :src="modulo.icone" :alt="modulo.nome" v-if="modulo.icone" />
              </div>
              <h4>{{ modulo.nome }}</h4>
              <button @click="acessarModulo(modulo)">Acessar</button>
            </div>
          </div>
        </div>

        <SafeIpLogView v-else-if="currentView === 'logs'" @back="currentView = 'modules'" />

        <SafeIpLoginCaptchaView
          v-else-if="currentView === 'captcha'"
          @back="currentView = 'modules'"
          @logged-in="handleCaptchaLogin"
        />

        <SafeIpCaptchaContentView
          v-else-if="currentView === 'captcha-content'"
          @back="currentView = 'modules'"
        />

        <SafeIpArquivosView
          v-else-if="currentView === 'arquivos'"
          @back="currentView = 'modules'"
        />

        <SafeIpEquipeView
          v-else-if="currentView === 'equipe'"
          @back="currentView = 'modules'"
          @mudar-view="handleViewChange"
          @selecionar-funcionario="handleSelecionarFuncionario"
        />

        <SafeIpCadastrarFuncionarioView
          v-else-if="currentView === 'cadastro-funcionario'"
          @back="currentView = 'equipe'"
          @funcionario-adicionado="currentView = 'equipe'"
        />

        <SafeIpFuncionarioDetalhesView
          v-else-if="currentView === 'detalhes-funcionario' && funcionarioSelecionadoId"
          :funcionarioId="funcionarioSelecionadoId"
          @back="handleBackFromDetalhes"
        />

        <SafeIpControleAcessoView
          v-else-if="currentView === 'controle-acesso'"
          @back="currentView = 'modules'"
        />
      </div>
    </div>

    <div v-if="isCodeModalOpen" class="modal-overlay-interno">
      <div class="modal-content-interno">
        <h3>Acesso Restrito: {{ currentModule?.nome }}</h3>
        <p>
          Digite o <strong>Código de Acesso</strong> atual (Gerado via 2FA) para liberar o
          relatório:
        </p>

        <input
          type="password"
          v-model="passwordInput"
          @keyup.enter="handlePasswordSubmit"
          placeholder="Insira o Código de Acesso"
          :disabled="isLoading"
        />

        <p v-if="mensagemErro" class="mensagem-erro">{{ mensagemErro }}</p>

        <div class="modal-actions">
          <button @click="isCodeModalOpen = false" class="btn-cancel" :disabled="isLoading">
            Cancelar
          </button>
          <button
            @click="handlePasswordSubmit"
            class="btn-confirm"
            :disabled="isLoading || !passwordInput"
          >
            {{ isLoading ? 'Verificando...' : 'Entrar' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import axios from 'axios'
import { defineEmits } from 'vue'

import SafeIpLogView from './SafeIpLogView.vue'
import SafeIpLoginCaptchaView from './SafeIpLoginCaptchaView.vue'
import SafeIpCaptchaContentView from './SafeIpCaptchaContentView.vue'
import SafeIpArquivosView from './SafeIpArquivosView.vue'
import SafeIpEquipeView from './SafeIpEquipeView.vue'
import SafeIpCadastrarFuncionarioView from './SafeIpCadastrarFuncionarioView.vue'
import SafeIpFuncionarioDetalhesView from './SafeIpFuncionarioDetalhesView.vue'
import SafeIpControleAcessoView from './SafeIpControleAcessoView.vue'

const emit = defineEmits(['close'])
const router = useRouter()
const currentView = ref('modules')
const isCodeModalOpen = ref(false)
const currentModule = ref(null)
const passwordInput = ref('')
const mensagemErro = ref('')
const isLoading = ref(false)

const funcionarioSelecionadoId = ref(null)

import captchaIcon from '@/assets/0d5bdf85-7116-41dd-9dd9-46ef0b9e1edf.png'
import logsIcon from '@/assets/b2cee526-0f8e-4a32-a27f-1a31012b39e1.png'
import arquivosIcon from '@/assets/b3d7ecf2-604a-4f9d-896c-0e1f02c4ae34.png'
import equipeIcon from '@/assets/f583b0e2-68b8-439c-8b72-30a6eb03b986.png'
import financeiroIcon from '@/assets/36fa1fd7-2a12-4d25-bbf9-d48551fd8369.png'
import controleIcon from '@/assets/8a87ed11-40cf-46d6-988d-ad2867690f41.png'

const modulos = [
  {
    nome: 'Captcha',
    rota: '/gerente/captcha',
    icone: captchaIcon,
    arquivoUrl: null,
    view: 'captcha',
  },
  { nome: 'Logs', rota: null, icone: logsIcon, arquivoUrl: null, view: 'logs' },
  { nome: 'Arquivos', rota: null, icone: arquivosIcon, arquivoUrl: null, view: 'arquivos' },
  { nome: 'Equipe', rota: null, icone: equipeIcon, arquivoUrl: null, view: 'equipe' },
  {
    nome: 'Financeiro',
    rota: null,
    icone: financeiroIcon,
    arquivoUrl: '/docs/financeiro.xlsx',
    view: 'external',
  },
  {
    nome: 'Controle de Acesso',
    rota: null,
    icone: controleIcon,
    arquivoUrl: null,
    view: 'controle-acesso',
  },
]

function acessarModulo(modulo) {
  if (modulo.view === 'logs') {
    currentView.value = 'logs'
  } else if (modulo.view === 'captcha') {
    currentView.value = 'captcha'
    currentModule.value = modulo
  } else if (modulo.view === 'arquivos') {
    currentView.value = 'arquivos'
    currentModule.value = modulo
  } else if (modulo.view === 'equipe') {
    currentView.value = 'equipe'
    currentModule.value = modulo
  } else if (modulo.view === 'controle-acesso') {
    currentView.value = 'controle-acesso'
    currentModule.value = modulo
  } else if (modulo.view === 'external') {
    currentModule.value = modulo
    passwordInput.value = ''
    mensagemErro.value = ''
    isCodeModalOpen.value = true
  } else if (modulo.view === 'page' && modulo.rota) {
    emit('close')
    router.push(modulo.rota)
  } else {
    console.error('Módulo sem rota/ação configurada')
  }
}

function handleSelecionarFuncionario(id) {
  funcionarioSelecionadoId.value = id // Guarda o ID
  currentView.value = 'detalhes-funcionario' // Muda a view
}

function handleBackFromDetalhes() {
  currentView.value = 'equipe' // Volta para a lista
  funcionarioSelecionadoId.value = null // Limpa o ID
}

function handleViewChange(viewName) {
  currentView.value = viewName
}

function handleCaptchaLogin() {
  currentView.value = 'captcha-content'
}

async function handlePasswordSubmit() {
  if (!passwordInput.value) {
    mensagemErro.value = 'Por favor, insira o Código de Acesso.'
    return
  }

  isLoading.value = true
  mensagemErro.value = ''

  try {
    const cpfLogado = localStorage.getItem('cpfLogado')?.trim()
    if (!cpfLogado) {
      mensagemErro.value = 'Erro: CPF do usuário logado não encontrado.'
      isLoading.value = false
      return
    }

    const response = await axios.post(
      'http://localhost:8080/safeip/verificarCodigo',
      {
        codigo: passwordInput.value,
        tipo: 'Acesso',
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'cpf-logado': cpfLogado,
        },
      }
    )

    if (response.data.valido) {
      window.open(currentModule.value.arquivoUrl, '_blank')

      console.log(`Acesso liberado para: ${currentModule.value.nome}`)

      isCodeModalOpen.value = false

      currentView.value = 'modules'

      currentModule.value = null
      passwordInput.value = ''
    } else {
      mensagemErro.value = 'Código de Acesso Inválido ou Expirado! Tente gerar um novo.'
      passwordInput.value = ''
    }
  } catch (error) {
    console.error('Erro ao verificar código:', error.response?.data || error)
    mensagemErro.value = 'Erro de conexão com o servidor. Tente novamente.'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
/* Estilos existentes */
.safeip-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1001;
}
.safeip-modal-content {
  background: #f8f8f8;
  padding: 30px;
  border-radius: 15px;
  width: 90%;
  max-width: 1200px;
  height: 80%;
  max-height: 90vh;
  overflow-y: hidden;
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
}
.modules-view {
  flex-grow: 1;
  overflow-y: auto;
  padding-right: 15px;
}
.safeip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}
.btn-close:hover {
  color: #e74c3c;
}
.titulo {
  font-size: 2rem;
  font-weight: 700;
  color: #5e2ced;
}
.subtitulo {
  color: #555;
  margin-bottom: 50px;
  font-weight: 500;
  text-align: center;
}
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  justify-items: center;
}
.card {
  background: #1a1a1a;
  color: white;
  width: 100%;
  max-width: 350px;
  height: 250px;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s;
}
.card:hover {
  transform: translateY(-5px);
}
.icone img {
  width: 70px;
  height: 60px;
}
.card h4 {
  text-align: center;
}
.card button {
  padding: 12px 25px;
  background: linear-gradient(90deg, #5e2ced, #7d4dff);
  border: none;
  border-radius: 5px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: opacity 0.3s, transform 0.2s;
}
.card button:hover {
  opacity: 0.9;
  transform: scale(1.02);
}
.modal-overlay-interno {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1010;
}
.modal-content-interno {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 400px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.modal-content-interno h3 {
  color: #5e2ced;
  margin-bottom: 15px;
}
.modal-content-interno input {
  padding: 12px 15px;
  margin: 20px 0 10px 0;
  width: 90%;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 1rem;
  text-align: center;
}
.mensagem-erro {
  color: #e74c3c;
  font-weight: 600;
  margin-bottom: 20px;
  font-size: 0.95rem;
}
.modal-actions {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  width: 90%;
  margin-top: 10px;
}
.btn-confirm {
  background-color: #5e2ced;
  color: white;
  padding: 8px 25px;
  border: none;
  border-radius: 5px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
}
.btn-confirm:hover:not(:disabled) {
  background-color: #4a37e0;
}
.btn-confirm:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.btn-cancel {
  background-color: #e0e0e0;
  color: #333;
  padding: 8px 25px;
  border: none;
  border-radius: 5px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}
.btn-cancel:hover:not(:disabled) {
  background-color: #d0d0d0;
}
.btn-cancel:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>