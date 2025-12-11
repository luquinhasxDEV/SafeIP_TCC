<template>
  <div class="container">
    <component :is="Sidebar" />


    <main class="conteudo">
      <header class="topbar">
        <div class="busca">
          <input type="text" placeholder="CPF do cliente" v-model="buscaCpf" />
          <button @click="buscarCpf">🔍</button>
        </div>


        <div class="cliente" v-if="venda">
          <div class="dados">
            <b>{{ venda.cpf }}</b>
            <span>Cliente há 10 anos</span>
          </div>
        </div>
      </header>


      <section v-if="venda" class="card">
        <h2>Detalhes da Venda</h2>
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Produto</th>
              <th>Valor Unitário</th>
              <th>Quantidade</th>
              <th>Pagamento</th>
              <th>Data</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>#{{ venda.id }}</td>
              <td>{{ venda.produto }}</td>
              <td>R$ {{ precoUnitario(venda).toLocaleString() }}</td>
              <td>{{ venda.quantidade }}</td>
              <td>{{ venda.pagamento }}</td>
              <td>{{ formatarData(venda.data) }}</td>
              <td>R$ {{ venda.valor.toLocaleString() }}</td>
            </tr>
          </tbody>
        </table>


        <div class="assinatura">
          <p><strong>Assinatura do Cliente:</strong> ____________________</p>
        </div>
      </section>


      <section v-else class="sem-venda">
        <p>Nenhuma venda encontrada.</p>
      </section>
    </main>
  </div>
</template>


<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useVendasStore } from '@/stores/vendas'
import SidebarGerente from '@/components/sidebarGerente.vue'
import SidebarFuncionario from '@/components/sidebarFuncionario.vue'


const route = useRoute()
const store = useVendasStore()


const Sidebar = computed(() =>
  route.path.startsWith('/gerente') ? SidebarGerente : SidebarFuncionario
)


const venda = ref(null)
const buscaCpf = ref('')


onMounted(() => {
  const id = Number(route.params.id)
  venda.value = store.lista.find(v => v.id === id)
})


function precoUnitario(venda) {
  return venda.quantidade ? venda.valor / venda.quantidade : venda.valor
}


function buscarCpf() {
  if (!buscaCpf.value) return
  const encontrada = store.lista.find(v => v.cpf === buscaCpf.value)
  venda.value = encontrada || null
}


function formatarData(data) {
  return new Date(data).toLocaleDateString('pt-BR')
}
</script>


<style scoped>
.container {
  display: flex;
  min-height: 100vh;
}


.conteudo {
  flex: 1;
  padding: 30px;
  background: #f2f3f5;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}


.busca {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}


.busca input {
  border: none;
  padding: 10px 15px;
  outline: none;
  width: 250px;
}


.busca button {
  background: #5e2ced;
  color: white;
  border: none;
  padding: 10px 15px;
  cursor: pointer;
}


/* CLIENTE */
.cliente {
  display: flex;
  align-items: center;
  gap: 10px;
}


.foto {
  width: 50px;
  height: 50px;
  border-radius: 50%;
}


.dados b {
  display: block;
}


.dados span {
  font-size: 12px;
  color: #555;
}


/* CARD */
.card {
  background: #1d1b1f;
  color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}


.card h2 {
  margin-bottom: 15px;
  color: #d6d1e8;
}


table {
  width: 100%;
  border-collapse: collapse;
  color: #fff;
}


th,
td {
  padding: 10px;
  text-align: center;
  border-bottom: 1px solid #333;
}


th {
  background: #d6d1e8;
  color: #000;
  text-transform: uppercase;
  font-size: 12px;
}


/* RODAPÉ */
.assinatura {
  margin-top: 25px;
  text-align: right;
  font-style: italic;
}

.sem-venda {
  text-align: center;
  color: #555;
  margin-top: 40px;
}
</style>



