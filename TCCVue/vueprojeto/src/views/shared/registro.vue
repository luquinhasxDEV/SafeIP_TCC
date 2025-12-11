<template>
  <div class="container">
    <component :is="Sidebar" class="sidebar-fixa" />

    <main class="conteudo">
      <form class="form-container" @submit.prevent="registrarVenda">
        <h2>Registro de Venda</h2>

        <label for="produto">Produto</label>
        <select id="produto" v-model="produto" required>
          <option value="">Selecione um produto</option>
          <option v-for="p in produtos" :key="p.id" :value="p.nome">{{ p.nome }}</option>
        </select>

        <label for="quantidade">Quantidade</label>
        <input
          type="number"
          id="quantidade"
          v-model.number="quantidade"
          min="1"
          placeholder="Informe a quantidade"
          required
        />

        <label for="pagamento">Pagamento</label>
        <select id="pagamento" v-model="pagamento" required>
          <option value="">Selecione a forma de pagamento</option>
          <option value="pix">Pix</option>
          <option value="dinheiro">Dinheiro</option>
          <option value="cartao">Cartão</option>
        </select>

        <label for="data_venda">Data da Venda</label>
        <input type="date" id="data_venda" v-model="data" required />

        <label>Total</label>
        <p class="total">{{ totalFormatado }}</p>

        <div class="botoes">
          <button type="submit" class="registrar" :disabled="!botaoAtivo">Registrar venda</button>
          <button type="button" class="limpar" @click="limparFormulario">Limpar Registro</button>
        </div>
      </form>
    </main>

    <div v-if="mostrarModal" class="modal-overlay">
      <div class="modal">
        <button class="fechar" @click="fecharModal">✖</button>
        <h3>Venda de alto valor</h3>
        <p>Por favor, digite o código gerado pelo gerente:</p>

        <input
          type="text"
          v-model="codigoDigitado"
          placeholder="Digite o código"
        />

        <button
          class="confirmar"
          :disabled="codigoDigitado.trim() === ''"
          @click="confirmarCodigo"
        >
          Confirmar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import SidebarGerente from '@/components/sidebarGerente.vue'
import SidebarFuncionario from '@/components/sidebarFuncionario.vue'

const route = useRoute()
const Sidebar = computed(() =>
  route.path.startsWith('/gerente') ? SidebarGerente : SidebarFuncionario
)

const produtos = ref([])
const produto = ref('')
const quantidade = ref(1)
const pagamento = ref('')
const hoje = new Date()
const dataLocal = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2,'0')}-${String(hoje.getDate()).padStart(2,'0')}`
const data = ref(dataLocal)

const cpfLogado = ref(localStorage.getItem('cpfLogado') || '')
const cargo = ref(localStorage.getItem('cargoLogado') || '')

const mostrarModal = ref(false)
const codigoDigitado = ref('')
const botaoAtivo = ref(true)
let vendaPendente = null 

const totalFormatado = computed(() => {
  const precoProduto = produtos.value.find(p => p.nome === produto.value)?.preco || 0
  return (precoProduto * quantidade.value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
})

onMounted(carregarProdutos)

async function carregarProdutos() {
  try {
    const response = await axios.get('http://localhost:8080/safeip/produtos')
    produtos.value = response.data
  } catch (error) {
    console.error('Erro ao carregar produtos:', error)
  }
}

async function registrarVenda() {
  if (!produto.value || quantidade.value <= 0 || !pagamento.value || !data.value) {
    alert('Preencha todos os campos corretamente!')
    return
  }

  const produtoSelecionado = produtos.value.find(p => p.nome === produto.value)
  if (!produtoSelecionado) {
    alert('Produto inválido!')
    return
  }

  const venda = {
    produto: produto.value,
    quantidade: quantidade.value,
    pagamento: pagamento.value,
    data_venda: `${data.value}T00:00:00` 
  }

  try {
    const response = await axios.post(
      'http://localhost:8080/safeip/vendas',
      venda,
      { headers: { 'cpf-logado': cpfLogado.value } }
    )
    alert(response.data)
    limparFormulario()
  } catch (error) {
    if (error.response?.status === 403) {
      vendaPendente = venda
      mostrarModal.value = true
      botaoAtivo.value = false
    } else {
      alert(error.response?.data || 'Erro ao registrar venda')
    }
  }
}

async function confirmarCodigo() {
  try {
    const codigoLimpo = codigoDigitado.value.trim()
    
    const response = await axios.get(`http://localhost:8080/safeip/vendas/codigoCaptcha/${codigoLimpo}`)
    
    console.log('DEBUG response:', response.data) 

    if (response.status === 200 && response.data?.status === 'VALIDO') { 
        
      if (vendaPendente) {
        try {
            await axios.post(
              'http://localhost:8080/safeip/vendas?autorizada=true',
              vendaPendente,
              { 
                headers: { 
                    'cpf-logado': cpfLogado.value 
                } 
              }
            )
            alert('Venda registrada com sucesso!')
            vendaPendente = null
            limparFormulario()
            
        } catch (postError) {
            console.error('ERRO NO REGISTRO FINAL (POST):', postError.response?.data || postError);
            alert('Erro no registro final da venda após validação do código: ' + (postError.response?.data || 'Comunicação falhou.'));
            return; 
        }
      }
      

      mostrarModal.value = false
      botaoAtivo.value = true
      codigoDigitado.value = ''
      
    } else {
        alert('Resposta inesperada do servidor após validação.') 
    }

  } catch (error) {
    console.error('DEBUG error na validação (GET):', error.response || error)
    
    if (error.response?.status === 404) {
      alert('Código incorreto ou não encontrado!')
    } else {
      alert('Erro na validação do código (GET).')
    }
  }
}

function fecharModal() {
  mostrarModal.value = false
  botaoAtivo.value = true
  codigoDigitado.value = ''
  vendaPendente = null
}

function limparFormulario() {
  produto.value = ''
  quantidade.value = 1
  pagamento.value = ''
  data.value = new Date().toISOString().slice(0,10)
  vendaPendente = null
}
</script>

<style scoped>
.container {
  display: flex;
  min-height: 100vh;
  background: #f8f8f8;
}

.conteudo {
  margin-left: 360px;
  flex: 1;
  padding: 50px;
  overflow-y: auto;
}

.sidebar-fixa {
  position: fixed;
  top: 0;
  left: 0;
  width: 300px;
  height: 100vh;
  z-index: 1000;
  background-color: #141217;
}

.form-container {
  width: 500px;
  margin: 50px auto;
  padding: 20px;
  background: #1c1b29;
  border-radius: 10px;
  border: 2px solid #7a4df3;
  text-align: center;
  color: white;
}

form h2 {
  margin-bottom: 20px;
}

label {
  display: block;
  margin: 10px 0 5px;
  font-weight: bold;
}

input, select {
  width: 100%;
  padding: 8px;
  margin-bottom: 15px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
}

.total {
  font-weight: bold;
  color: white;
  margin-bottom: 15px;
}

.botoes {
  display: flex;
  justify-content: center;
  margin-top: 15px;
  gap: 10px;
}

button {
  padding: 10px 20px;
  border-radius: 25px;
  font-size: 14px;
  border: none;
  cursor: pointer;
  font-weight: bold;
}

.registrar {
  background: #4CAF50;
  color: white;
}

.registrar:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.limpar {
  background: red;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background: #1c1b29;
  border: 2px solid #7a4df3;
  border-radius: 10px;
  padding: 30px;
  width: 400px;
  color: white;
  text-align: center;
  position: relative;
}

.modal h3 {
  margin-bottom: 10px;
}

.modal input {
  width: 100%;
  margin-top: 10px;
  padding: 10px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
}

.modal .confirmar {
  margin-top: 15px;
  padding: 10px 20px;
  background: #4CAF50;
  border-radius: 25px;
  font-weight: bold;
  color: white;
  border: none;
  cursor: pointer;
}

.modal .confirmar:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.fechar {
  position: absolute;
  top: 8px;
  right: 10px;
  background: transparent;
  color: white;
  border: none;
  font-size: 18px;
  cursor: pointer;
}
</style>