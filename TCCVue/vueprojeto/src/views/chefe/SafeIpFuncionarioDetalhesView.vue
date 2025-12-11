<template>
  <div class="detalhes-container">
    <div class="header-safeip">
      <button class="btn-voltar" @click="$emit('back')">← Voltar para Equipe</button>
      <h2 class="titulo-safeip">Detalhes do Funcionário</h2>
      <div></div>
    </div>

    <div v-if="isLoading" class="loading-state">Carregando detalhes...</div>

    <div v-else-if="funcionario" class="card-detalhes">
      <h3>{{ funcionario.nome }}</h3>
      <p class="status" :class="{ ativo: funcionario.ativo, inativo: !funcionario.ativo }">
        Status: **{{ funcionario.ativo ? 'Ativo' : 'Inativo' }}**
      </p>

      <div class="info-grid">
        <div class="item">
          <strong>CPF:</strong> <span>{{ funcionario.cpf }}</span>
        </div>
        <div class="item">
          <strong>Cargo:</strong> <span>{{ funcionario.cargo }}</span>
        </div>
        <div class="item">
          <strong>Salário:</strong> <span>R$ {{ formatarSalario(funcionario.salario) }}</span>
        </div>
        <div class="item">
          <strong>Data de Contratação:</strong>
          <span>{{ formatarData(funcionario.dataContratacao) }}</span>
        </div>
      </div>

      <div class="botoes-acao">
        <button class="btn-acao btn-inativar" @click="toggleStatus" :disabled="isToggling">
          {{
            isToggling
              ? 'Aguarde...'
              : funcionario.ativo
              ? 'Inativar Funcionário'
              : 'Ativar Funcionário'
          }}
        </button>
        <button class="btn-acao btn-reset">Resetar Senha</button>
      </div>
    </div>

    <p v-else class="error-message">Funcionário não encontrado ou erro ao carregar dados.</p>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps, defineEmits } from 'vue'
import axios from 'axios'

const props = defineProps({
  funcionarioId: {
    type: [Number, String],
    required: true,
  },
})

const emit = defineEmits(['back'])
const funcionario = ref(null)
const isLoading = ref(true)
const isToggling = ref(false)

const formatarSalario = (salario) => {
  if (salario === null || salario === undefined) return '0,00'
  return parseFloat(salario).toLocaleString('pt-BR', { minimumFractionDigits: 2 })
}

const formatarData = (data) => {
  if (!data) return 'N/A'
  return new Date(data).toLocaleDateString('pt-BR')
}

const buscarDetalhes = async () => {
  isLoading.value = true
  try {
    const cpfLogado = localStorage.getItem('cpfLogado')?.trim()
    const response = await axios.get(`http://localhost:8080/safeip/equipe/${props.funcionarioId}`, {
      headers: { 'cpf-logado': cpfLogado },
    })
    funcionario.value = response.data
  } catch (error) {
    console.error('Erro ao buscar detalhes do funcionário:', error)
    funcionario.value = null
  } finally {
    isLoading.value = false
  }
}

const toggleStatus = async () => {
  if (!funcionario.value) return

  const novoStatusBoolean = !funcionario.value.ativo

  const novoStatusNumerico = novoStatusBoolean ? 1 : 0

  const cpfLogado = localStorage.getItem('cpfLogado')?.trim()

  isToggling.value = true
  try {
    await axios.put(
      `http://localhost:8080/safeip/equipe/${props.funcionarioId}/status`,
      { ativo: novoStatusNumerico },
      { headers: { 'cpf-logado': cpfLogado } }
    )

    funcionario.value.ativo = novoStatusBoolean

    alert(`Funcionário ${novoStatusBoolean ? 'ativado' : 'inativado'} com sucesso!`)
  } catch (error) {
    console.error('Erro ao alterar status:', error.response?.data || error.message)
    alert('Erro ao alterar status do funcionário.')
  } finally {
    isToggling.value = false
  }
}

onMounted(() => {
  if (props.funcionarioId) {
    buscarDetalhes()
  }
})
</script>

<style scoped>
.detalhes-container {
  flex-grow: 1;
  padding: 20px;
  background: #f8f8f8;
  border-radius: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.header-safeip {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 15px;
  margin-bottom: 25px;
  width: 100%;
}
.titulo-safeip {
  font-size: 1.8rem;
  color: #242424;
  font-weight: 700;
  flex-grow: 1;
  text-align: center;
  margin: 0 20px;
}
.btn-voltar {
  background: #5e2ced;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
  white-space: nowrap;
}
.btn-voltar:hover {
  background: #4a37e0;
}
.loading-state,
.error-message {
  text-align: center;
  color: #999;
  margin-top: 50px;
}

.card-detalhes {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
}

.card-detalhes h3 {
  font-size: 2em;
  color: #333;
  margin-bottom: 5px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
}
.status {
  font-weight: bold;
  margin-bottom: 20px;
  font-size: 1.1em;
}
.status.ativo {
  color: #00b894;
}
.status.inativo {
  color: #e74c3c;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 30px;
}
.item {
  padding: 10px 0;
  border-bottom: 1px dotted #ddd;
}
.item strong {
  color: #0d1b3e;
  display: block;
  font-size: 0.9em;
  margin-bottom: 3px;
}
.item span {
  font-size: 1.1em;
}

.botoes-acao {
  display: flex;
  justify-content: center;
  gap: 15px;
}
.btn-acao {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  font-weight: 600;
  transition: 0.3s;
}
.btn-inativar {
  background: #e74c3c;
}
.btn-inativar:hover:not(:disabled) {
  background: #c0392b;
}
.btn-inativar:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.btn-reset {
  background: #3498db;
}
.btn-reset:hover {
  background: #2980b9;
}
</style>