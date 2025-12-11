<template>
  <div class="container">
    <component :is="Sidebar" class="sidebar-fixa" />

    <main class="conteudo">
      <header class="header">
        <h2>Lista de Vendas</h2>
      </header>

      <section class="cards">
        <div class="card-venda" v-for="venda in vendas" :key="venda.id">
          <h3>{{ venda.produto }}</h3>
          <p><strong>Total:</strong> {{ formatarTotal(venda.total) }}</p>
          <p><strong>Quantidade:</strong> {{ venda.quantidade }}</p>
          <p><strong>Pagamento:</strong> {{ venda.pagamento }}</p>
          <p><strong>Data:</strong> {{ formatarData(venda.data_venda) }}</p>

          <router-link
            :to="isGerente
              ? { name: 'ConsultaGerente', params: { id: venda.id } }
              : { name: 'ConsultaFuncionario', params: { id: venda.id } }"
          >
          </router-link>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import SidebarFuncionario from '@/components/sidebarFuncionario.vue'
import SidebarGerente from '@/components/sidebarGerente.vue'

const route = useRoute()
const Sidebar = computed(() =>
  route.path.startsWith('/gerente') ? SidebarGerente : SidebarFuncionario
)

const vendas = ref([])

function formatarTotal(valor) {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatarData(data) {
  if (!data) return ''
  const partes = data.split('T')[0].split('-')
  return `${partes[2]}/${partes[1]}/${partes[0]}`
}

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/safeip/vendas', {
      headers: { 'Content-Type': 'application/json' }
    })
    vendas.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Erro ao buscar vendas:', error)
    vendas.value = []
  }
})
</script>

<style scoped>
.container {
  display: flex;
  min-height: 100vh;
  background: #f8f8f8;
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

.conteudo {
  margin-left: 360px; 
  flex: 1;
  padding: 50px;
  overflow-y: auto;
}

.header {
  margin-bottom: 20px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.card-venda {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.card-venda h3 {
  margin-bottom: 10px;
}

.card-venda p {
  margin-bottom: 5px;
}

.btn-detalhes {
  display: inline-block;
  margin-top: 10px;
  background: #5e2ced;
  color: #fff;
  padding: 6px 12px;
  border-radius: 6px;
  text-decoration: none;
  transition: 0.3s;
}

.btn-detalhes:hover {
  background: #4520b5;
}

.lista-page .sidebar {
  width: 360px;
}
</style>
