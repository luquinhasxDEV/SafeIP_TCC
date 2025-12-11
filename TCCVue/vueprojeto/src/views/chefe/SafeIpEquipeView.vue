<template>
    <div class="equipe-modal-container">
        
        <div class="header-safeip">
            <button class="btn-voltar" @click="$emit('back')">← Voltar</button> 
            <h2 class="titulo-safeip">Gerenciar Equipe</h2>
            
            <button @click="$emit('mudar-view', 'cadastro-funcionario')" class="btn-adicionar">
              Adicionar Funcionário
            </button>
        </div>

        <section class="acoes">
            <p v-if="funcionarios.length === 0 && !isLoading" class="no-data-message">Nenhum funcionário encontrado.</p>
            <div v-if="isLoading" class="loading-state">Carregando dados da equipe...</div>
        </section>

        <section class="cards-equipe-grid">
            <div
                class="card-funcionario"
                v-for="func in funcionarios"
                :key="func.id"
                :class="{ inativo: !func.ativo }"
            >
                <h3>{{ func.nome }}</h3>
                <p class="cargo">{{ func.cargo }}</p>

                <div class="info">
                    <p><strong>CPF:</strong> {{ func.cpf }}</p>
                    <p><strong>Salário:</strong> R$ {{ func.salario ? formatarSalario(func.salario) : '0,00' }}</p>
                    <p><strong>Cargo:</strong> {{ func.cargo }}</p>
                </div>

                <button
                    class="btn-gerenciar"
                    @click="gerenciarFuncionario(func.id)"
                >
                    Gerenciar Funcionário
                </button>
            </div>
        </section>
    </div>
</template>

<script setup>
import { ref, onMounted, defineEmits } from "vue";
import axios from "axios";

const emit = defineEmits(['back', 'mudar-view', 'selecionar-funcionario']); 
const funcionarios = ref([]);
const isLoading = ref(true);

const formatarSalario = (salario) => {
    return parseFloat(salario).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
}

const listarFuncionarios = async () => {
  isLoading.value = true;
  try {
    const cpfLogado = localStorage.getItem('cpfLogado')?.trim();
    const response = await axios.get("http://localhost:8080/safeip/equipe", {
        headers: { 'cpf-logado': cpfLogado }
    });
    funcionarios.value = response.data;
  } catch (error) {
    console.error("Erro ao buscar funcionários:", error);
  } finally {
    isLoading.value = false;
  }
};

const gerenciarFuncionario = (id) => {
    // 🚨 Emite o evento 'selecionar-funcionario' com o ID do funcionário
    emit('selecionar-funcionario', id); 
};


onMounted(() => {
  listarFuncionarios();
});
</script>

<style scoped>
/* Estilos inalterados, mantidos para completude */
.equipe-modal-container { 
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

.btn-adicionar {
    background: #00b894;
    color: white;
    border: none;
    padding: 10px 18px;
    border-radius: 5px; 
    cursor: pointer;
    transition: 0.3s;
    text-decoration: none;
    font-weight: 600;
    white-space: nowrap; 
}
.btn-adicionar:hover {
    background: #019170;
}

.acoes {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-bottom: 30px;
}
.loading-state, .no-data-message {
    margin-right: auto; 
    font-style: italic;
    color: #999;
}

.cards-equipe-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
}

.card-funcionario {
    background: #fff;
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
    text-align: center;
    transition: 0.3s;
    border: 1px solid #eee;
}

.card-funcionario h3 {
    margin: 5px 0;
    font-size: 18px;
    color: #333;
}

.cargo {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
}

.info {
    margin: 10px 0;
    font-size: 14px;
    text-align: left;
    padding: 0 10px;
    line-height: 1.6;
}
.info strong {
    color: #0d1b3e;
}

.btn-gerenciar {
    background: #5e2ced; 
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 6px;
    cursor: pointer;
    transition: 0.3s;
    display: inline-block;
    text-decoration: none;
    font-weight: 600;
    margin-top: 10px;
}

.btn-gerenciar:hover {
    background: #4a37e0;
}

.card-funcionario.inativo {
    opacity: 0.5;
    background: #ffebeb;
}
</style>