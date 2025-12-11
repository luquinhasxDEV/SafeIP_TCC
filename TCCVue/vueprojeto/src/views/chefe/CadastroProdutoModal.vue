<script setup>
import { ref, defineEmits } from 'vue'
import axios from 'axios'

const emit = defineEmits(['close', 'produtoCadastrado'])

const tamanhosDisponiveis = ['P', 'PP', 'M', 'G', 'GG', 'XG']
const cargoLogado = localStorage.getItem('role') || ''
const cpfLogado = localStorage.getItem('cpfLogado') || ''

const novoProduto = ref({
  nome: '', 
  preco: null, 
  descricao: '', 
  estoque: null, 
  tamanhos_disponiveis: [],
  ativo: true
})


async function cadastrarProduto() {
  if (cargoLogado !== 'gerente') {
    alert('Acesso negado. Apenas gerentes podem cadastrar produtos.');
    return;
  }
  
  const tamanhosStr = (novoProduto.value.tamanhos_disponiveis || [])
    .map(t => String(t).trim())
    .filter(t => t !== '')
    .join(',')

  const produtoData = {
    nome: novoProduto.value.nome,
    preco: novoProduto.value.preco,
    descricao: novoProduto.value.descricao,
    estoque: novoProduto.value.estoque,
    tamanhos_disponiveis: tamanhosStr,
    ativo: novoProduto.value.ativo
  }

  try {
    const response = await axios.post('http://localhost:8080/safeip/produtos', produtoData, {
      headers: { 
        'Content-Type': 'application/json', 
        'cpf-logado': cpfLogado
      }
    })
    
    alert(response.data) 
    
    emit('produtoCadastrado') 

  } catch (error) {
    console.error('Erro ao cadastrar produto:', error.response?.data || error)
    alert(error.response?.data || 'Erro ao cadastrar produto. Verifique os campos.');
  }
}


function fechar() {
  emit('close')
}
</script>

<template>
  <div class="modal-bg" @click.self="fechar">
    <div class="modal">
      <header class="modal-header">
        <h2>Cadastrar Novo Produto</h2>
        <button class="btn-fechar" @click="fechar">×</button>
      </header>
      
      <form @submit.prevent="cadastrarProduto">
        <div class="form-group">
          <label for="nome">Nome do Produto:</label>
          <input type="text" id="nome" v-model="novoProduto.nome" required maxlength="100" />
        </div>

        <div class="form-group row">
          <div class="col">
            <label for="preco">Preço (R$):</label>
            <input type="number" id="preco" v-model.number="novoProduto.preco" step="0.01" min="0.01" required />
          </div>
          <div class="col">
            <label for="estoque">Estoque Inicial:</label>
            <input type="number" id="estoque" v-model.number="novoProduto.estoque" min="1" required />
          </div>
        </div>

        <div class="form-group">
          <label for="descricao">Descrição:</label>
          <textarea id="descricao" v-model="novoProduto.descricao" maxlength="500"></textarea>
        </div>

        <div class="form-group">
          <label>Tamanhos Disponíveis:</label>
          <div class="tamanhos-checkboxes">
            <div v-for="t in tamanhosDisponiveis" :key="t" class="tamanho-option">
              <input type="checkbox" :id="'tamanho-' + t" :value="t" v-model="novoProduto.tamanhos_disponiveis">
              <label :for="'tamanho-' + t">{{ t }}</label>
            </div>
          </div>
        </div>

        <div class="form-group checkbox-ativo">
          <label>
            <input type="checkbox" v-model="novoProduto.ativo"> Produto Ativo
          </label>
        </div>

        <button type="submit" class="btn-submit">Cadastrar Produto</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-bg { 
  position: fixed; 
  top: 0; left: 0; right: 0; bottom: 0; 
  background: rgba(0, 0, 0, 0.7); 
  display: flex; 
  align-items: center; 
  justify-content: center;
  z-index: 1050; 
}
.modal {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.modal-header h2 { margin: 0; font-size: 20px; }
.btn-fechar {
  background: transparent;
  border: none;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  color: #666;
}

.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
.form-group input[type="text"], 
.form-group input[type="number"], 
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-sizing: border-box;
}
.form-group textarea { resize: vertical; min-height: 80px; }
.form-group.row { display: flex; gap: 20px; }
.form-group.row .col { flex: 1; }
.tamanhos-checkboxes { display: flex; gap: 15px; flex-wrap: wrap; margin-top: 5px; }
.tamanho-option { display: flex; align-items: center; gap: 5px; }
.checkbox-ativo { margin-top: 15px; }
.checkbox-ativo label { display: flex; align-items: center; font-weight: normal; }
.checkbox-ativo input[type="checkbox"] { margin-right: 8px; width: auto; }
.btn-submit {
  width: 100%;
  background: #4f46e5;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 15px;
}
.btn-submit:hover { background: #4338ca; }
</style>