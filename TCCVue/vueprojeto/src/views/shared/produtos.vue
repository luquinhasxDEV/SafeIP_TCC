<script setup>
import { ref, computed, onMounted, watch, defineAsyncComponent } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import SidebarGerente from '@/components/sidebarGerente.vue'
import SidebarFuncionario from '@/components/sidebarFuncionario.vue'

// 🚨 CORREÇÃO DO CAMINHO: Assumindo que o arquivo está em src/views/chefe/
import CadastroProdutoModal from '@/views/chefe/CadastroProdutoModal.vue'

const route = useRoute()

const Sidebar = computed(() => {
  if (route.path.startsWith('/gerente')) {
    return SidebarGerente
  } else (route.path.startsWith('/funcionario')) 
    return SidebarFuncionario
})

const produtos = ref([])
const termoBusca = ref('')
const ordenacao = ref('Maior Preço')
// 🚨 ESTADO PARA CONTROLAR O MODAL DE CADASTRO
const mostrarModalCadastro = ref(false)
const mostrarModalEdicao = ref(false)
const mostrarModalSafeIp = ref(false) 
const tamanhos = ['P', 'PP', 'M', 'G', 'GG', 'XG']

const cargoLogado = computed(() => localStorage.getItem('role') || '')

// Importação Assíncrona e Condicional do Componente SafeIP
const SafeIpModulesModal = defineAsyncComponent(() => 
  import('@/views/chefe/SafeIpModulesModal.vue')
)

// Lógica para bloquear a rolagem do body
watch(mostrarModalSafeIp, (isModalOpen) => {
  if (isModalOpen) {
    document.body.classList.add('modal-open');
  } else {
    document.body.classList.remove('modal-open');
  }
});

// Dados para Edição
const produtoEditando = ref({
  id: null,
  nome: '',
  preco: null,
  descricao: '',
  estoque: null,
  tamanhos_disponiveis: [],
  ativo: true
})

async function carregarProdutos() {
  try {
    const response = await axios.get('http://localhost:8080/safeip/produtos')
    produtos.value = response.data.map(prod => {
      let tamanhosArray = []
      if (Array.isArray(prod.tamanhos_disponiveis)) {
        tamanhosArray = prod.tamanhos_disponiveis.map(t => String(t))
      } else if (typeof prod.tamanhos_disponiveis === 'string' && prod.tamanhos_disponiveis.trim() !== '') {
        tamanhosArray = prod.tamanhos_disponiveis.split(',').map(s => s.trim()).filter(s => s !== '')
      }
      return {
        ...prod,
        preco: prod.preco ?? 0,
        estoque: prod.estoque ?? 0,
        tamanhos_disponiveis: tamanhosArray,
        ativo: Boolean(prod.ativo)
      }
    })
  } catch (error) {
    console.error('Erro ao carregar produtos:', error)
  }
}

// 🎯 FUNÇÃO ATUALIZADA: Agora ABRE O MODAL
function abrirModalCadastro() { 
  if (cargoLogado.value !== 'gerente') return alert('Acesso negado.');
  mostrarModalCadastro.value = true;
}

// 🚨 NOVA FUNÇÃO: Fecha o modal de cadastro e recarrega a lista
function fecharEAtualizarLista() {
  mostrarModalCadastro.value = false;
  carregarProdutos(); // Recarrega para que o novo produto apareça
}

function abrirModalEdicao(produto) {
  if (cargoLogado.value !== 'gerente') return alert('Acesso negado.');
  produtoEditando.value = { ...produto, tamanhos_disponiveis: [...(produto.tamanhos_disponiveis || [])] }
  mostrarModalEdicao.value = true
}

function fecharModalEdicao() {
  mostrarModalEdicao.value = false
  produtoEditando.value = { id:null, nome:'', preco:null, descricao:null, estoque:null, tamanhos_disponiveis:[], ativo:true }
}

async function editarProduto() {
  if (cargoLogado.value !== 'gerente') {
    alert('Acesso negado. Apenas gerentes podem editar produtos.');
    return;
  }
  
  const tamanhosStr = (produtoEditando.value.tamanhos_disponiveis || [])
    .map(t => String(t).trim())
    .filter(t => t !== '')
    .join(',')

  const produtoData = {
    nome: produtoEditando.value.nome,
    preco: produtoEditando.value.preco,
    descricao: produtoEditando.value.descricao,
    estoque: produtoEditando.value.estoque,
    tamanhos_disponiveis: tamanhosStr,
    ativo: produtoEditando.value.ativo
  }

  try {
    const response = await axios.put(`http://localhost:8080/safeip/produtos/${produtoEditando.value.id}`, produtoData, {
      headers: { 'Content-Type': 'application/json', 'cpf-logado': localStorage.getItem('cpfLogado') }
    })
    alert(response.data)
    fecharModalEdicao()
    carregarProdutos()
  } catch (error) {
    console.error('Erro ao editar produto:', error)
    alert(error.response?.data || 'Erro ao editar produto.')
  }
}


const produtosFiltrados = computed(() => {
  if (!termoBusca.value.trim()) return produtos.value
  const busca = termoBusca.value.toLowerCase()
  return produtos.value.filter(item => 
    item.nome.toLowerCase().includes(busca) || 
    item.descricao.toLowerCase().includes(busca)
  )
})

const produtosOrdenados = computed(() => {
  const lista = [...produtosFiltrados.value]
  return ordenacao.value === 'Maior Preço'
    ? lista.sort((a, b) => b.preco - a.preco)
    : lista.sort((a, b) => a.preco - b.preco)
})

function corEstoque(qtd) {
  if (qtd > 50) return 'verde'
  if (qtd >= 25) return 'amarelo'
  return 'vermelho'
}

onMounted(carregarProdutos)
</script>

<template>
  <div class="container">
    <component :is="Sidebar" class="sidebar-fixa" />

    <main class="conteudo">
      <header class="topo">
        <h1>Produtos</h1>

        <select v-model="ordenacao">
          <option>Maior Preço</option>
          <option>Menor Preço</option>
        </select>

        <div class="busca">
          <input type="text" v-model="termoBusca" placeholder="Pesquisar" />
        </div>

        <button 
          v-if="cargoLogado === 'gerente'" 
          class="btn-add" 
          @click="abrirModalCadastro"
        >+</button>
      </header>
      
      <CadastroProdutoModal
        v-if="mostrarModalCadastro"
        @close="mostrarModalCadastro = false"
        @produtoCadastrado="fecharEAtualizarLista"
      />
      
      <div v-if="mostrarModalEdicao" class="modal-bg">
        <div class="modal">
          <h2>Editar Produto</h2>
          <form @submit.prevent="editarProduto">
            <div class="form-group">
              <label for="edit-nome">Nome:</label>
              <input type="text" id="edit-nome" v-model="produtoEditando.nome" required />
            </div>
            <div class="form-group">
              <label for="edit-preco">Preço (R$):</label>
              <input type="number" id="edit-preco" v-model.number="produtoEditando.preco" step="0.01" required />
            </div>
            <div class="form-group">
              <label for="edit-descricao">Descrição:</label>
              <textarea id="edit-descricao" v-model="produtoEditando.descricao"></textarea>
            </div>
            <div class="form-group">
              <label for="edit-estoque">Estoque:</label>
              <input type="number" id="edit-estoque" v-model.number="produtoEditando.estoque" required />
            </div>
            
            <div class="form-group">
              <label>Tamanhos Disponíveis:</label>
              <div class="tamanhos-checkboxes">
                <div v-for="t in tamanhos" :key="t" class="tamanho-option">
                  <input type="checkbox" :id="'edit-tamanho-' + t" :value="t" v-model="produtoEditando.tamanhos_disponiveis">
                  <label :for="'edit-tamanho-' + t">{{ t }}</label>
                </div>
              </div>
            </div>
            
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="produtoEditando.ativo"> Produto Ativo
              </label>
            </div>

            <div class="modal-botoes">
              <button type="button" @click="fecharModalEdicao">Cancelar</button>
              <button type="submit">Salvar Edição</button>
            </div>
          </form>
        </div>
      </div>
      <section class="produtos">
        <div
          v-for="item in produtosOrdenados"
          :key="item.id"
          class="card"
          :class="{ inativo: !item.ativo }"
        >
          <h2>{{ item.nome }} | R$ {{ Number(item.preco).toFixed(2) }}</h2>
          <p>{{ item.descricao }}</p>
          <p><strong>Tamanhos:</strong> {{ (item.tamanhos_disponiveis || []).join(', ') }}</p>
          <div class="estoque-container">
            <span :class="['estoque', corEstoque(item.estoque)]">{{ item.estoque }}</span>
            
            <button 
              v-if="cargoLogado === 'gerente'" 
              class="btn-editar" 
              @click="abrirModalEdicao(item)"
            >✎</button>
          </div>
        </div>
      </section>
    </main>
    
    <template v-if="cargoLogado === 'gerente'">
        <button 
          class="btn-safeip-flutuante" 
          @click="mostrarModalSafeIp = true"
        >
          🛡️
        </button>

        <SafeIpModulesModal 
          v-if="mostrarModalSafeIp" 
          @close="mostrarModalSafeIp = false"
        />
    </template>

  </div>
</template>

<style scoped>
/* Estilo global para bloquear o scroll do body */
:global(.modal-open) {
  overflow: hidden !important; 
  padding-right: 15px; 
}

.container { display: flex; }
.conteudo {
  margin-left: 360px; 
  padding: 40px;
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
.topo { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.busca input { padding: 8px; border-radius: 15px; border: 1px solid #ccc; }
.btn-add { margin-left: auto; background: #4f46e5; border: none; color: white; font-size: 22px; border-radius: 50%; width: 40px; height: 40px; cursor: pointer; }
.produtos { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.card { background: #fff; border-radius: 10px; padding: 15px; box-shadow: 0 0 10px rgba(0,0,0,0.05); transition: opacity 0.3s; position: relative; z-index: 0; }
.card.inativo { opacity: 0.5; }
.card h2 { font-size: 16px; margin: 10px 0; }
.card p { font-size: 14px; margin-bottom: 5px; }
.estoque-container { display: flex; align-items: center; gap: 8px; }
.btn-editar { background: #2196F3; border: none; color: white; border-radius: 5px; cursor: pointer; padding: 4px 8px; }
.estoque { display: inline-block; padding: 4px 10px; border-radius: 6px; color: #fff; font-weight: bold; margin: 5px 0; }
.estoque.verde { background: #4caf50; }
.estoque.amarelo { background: #ffc107; color: #000; }
.estoque.vermelho { background: #f44336; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 999; }
.modal { background: white; padding: 20px; border-radius: 10px; width: 400px; max-height: 90vh; display: flex; flex-direction: column; overflow-y: auto; z-index: 1001; }
.modal input, .modal textarea { margin-bottom: 10px; padding: 8px; border: 1px solid #ccc; border-radius: 6px; }
.modal-botoes { display: flex; justify-content: space-between; margin-top: 10px; }
.modal-botoes button:first-child { background: #ccc; }
.modal-botoes button:last-child { background: #4f46e5; color: white; }
.form-group { margin-bottom: 12px; display: flex; flex-direction: column; }
.tamanhos-checkboxes { display: flex; gap: 10px; flex-wrap: wrap; }
.tamanho-option { display: flex; align-items: center; gap: 5px; }

/* Estilo para o botão flutuante SafeIP */
.btn-safeip-flutuante {
  position: fixed;
  bottom: 40px; 
  right: 40px; 
  background: #5e2ced; 
  color: white;
  border: none;
  font-size: 28px; 
  border-radius: 50%;
  width: 60px;
  height: 60px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.25);
  transition: background 0.3s, transform 0.2s;
  z-index: 1000; 
  display: flex;
  justify-content: center;
  align-items: center;
}

.btn-safeip-flutuante:hover {
  background: #7d4dff;
  transform: scale(1.05);
}
</style>