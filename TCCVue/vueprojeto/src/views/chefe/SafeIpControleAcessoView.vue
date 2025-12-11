<template>
    <div class="acesso-container">
        <div class="header-safeip">
            <button class="btn-voltar" @click="$emit('back')">← Voltar</button> 
            <h2 class="titulo-safeip">Controle de Acesso ao Sistema</h2>
            <div></div> 
        </div>

        <div v-if="isLoading" class="loading-state">Carregando dados de acesso...</div>
        
        <div v-else class="content-grid">
            
            <div class="divisao usuarios-ativos">
                <h3 class="titulo-divisao ativo">Usuários Ativos (Com Acesso)</h3>
                
                <p v-if="usuariosAtivos.length === 0" class="no-data">Nenhum usuário ativo encontrado.</p>

                <div v-else class="lista-acesso">
                    <div class="item-header">
                        <span>Nome / CPF</span>
                        <span>IP de Acesso</span>
                        <span>Ação</span>
                    </div>
                    <div v-for="user in usuariosAtivos" :key="user.id" class="item-acesso">
                        <div class="info-usuario">
                            <strong>{{ user.nome }}</strong>
                            <small>{{ user.cpf }}</small>
                        </div>
                        <span class="ip-acesso">{{ user.ipAcesso || 'Não registrado' }}</span>
                        
                        <button 
                            class="btn-acao btn-remover" 
                            @click="toggleStatus(user.id, 0)" 
                            :disabled="isProcessing[user.id]"
                        >
                            {{ isProcessing[user.id] ? 'Processando...' : 'Bloquear' }}
                        </button>
                    </div>
                </div>
            </div>

            <div class="divisao usuarios-bloqueados">
                <h3 class="titulo-divisao bloqueado">Usuários Bloqueados</h3>
                
                <p v-if="usuariosBloqueados.length === 0" class="no-data">Nenhum usuário bloqueado.</p>

                <div v-else class="lista-acesso">
                    <div class="item-header">
                        <span>Nome / CPF</span>
                        <span>Ação</span>
                    </div>
                    <div 
                        v-for="user in usuariosBloqueados" 
                        :key="user.id" 
                        class="item-acesso item-bloqueado"
                    >
                        <div class="info-usuario">
                            <strong>{{ user.nome }}</strong>
                            <small>{{ user.cpf }}</small>
                        </div>
                        
                        <button 
                            class="btn-acao btn-reativar" 
                            @click="toggleStatus(user.id, 1)"
                            :disabled="isProcessing[user.id]"
                        >
                            {{ isProcessing[user.id] ? 'Processando...' : 'Reativar' }}
                        </button>
                    </div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, defineEmits } from "vue";
import axios from "axios";

const emit = defineEmits(['back']);
const usuarios = ref([]);
const isLoading = ref(true);
const isProcessing = ref({}); 

const usuariosAtivos = computed(() => {
    return usuarios.value.filter(u => u.ativo === 1 || u.ativo === true);
});

const usuariosBloqueados = computed(() => {
    return usuarios.value.filter(u => u.ativo === 0 || u.ativo === false);
});

const buscarUsuarios = async () => {
    isLoading.value = true;
    try {
        const cpfLogado = localStorage.getItem('cpfLogado')?.trim();
        const response = await axios.get("http://localhost:8080/safeip/equipe", {
            headers: { 'cpf-logado': cpfLogado }
        });
        
        usuarios.value = response.data;
    } catch (error) {
        console.error("Erro ao buscar usuários de acesso:", error);
        alert("Erro ao carregar a lista de usuários. Verifique o servidor.");
    } finally {
        isLoading.value = false;
    }
};

const toggleStatus = async (userId, novoStatus) => {
    isProcessing.value[userId] = true;
    
    const acaoFinalizada = novoStatus === 1 ? 'ativado' : 'bloqueado';
    
    try {
        const cpfLogado = localStorage.getItem('cpfLogado')?.trim();
        
        await axios.put(
            `http://localhost:8080/safeip/equipe/${userId}/status`, 
            { ativo: novoStatus }, 
            { 
                headers: { 
                    'cpf-logado': cpfLogado,
                    'Content-Type': 'application/json'
                } 
            }
        );

        const index = usuarios.value.findIndex(u => u.id === userId);
        if (index !== -1) {
            usuarios.value[index].ativo = novoStatus;
        }

        alert(`Usuário ${acaoFinalizada} com sucesso!`);
    } catch (error) {
        console.error(`Erro ao tentar ${novoStatus === 1 ? 'ativar' : 'bloquear'} usuário:`, error.response?.data || error);
        
        const errorMessage = error.response?.data || `Falha ao ${novoStatus === 1 ? 'ativar' : 'bloquear'} o usuário. Verifique a permissão.`;
        alert(`ERRO: ${errorMessage}`);
        
    } finally {
        isProcessing.value[userId] = false;
    }
};

onMounted(() => {
  buscarUsuarios();
});
</script>

<style scoped>
.acesso-container { 
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
    background: #5e2ced; color: white; 
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

.loading-state { 
    text-align: center; 
    color: #999; 
    margin-top: 50px; 
}
.content-grid {
    display: grid;
    grid-template-columns: 1fr 1fr; 
    gap: 30px;
    flex-grow: 1;
}

.divisao {
    background: #fff;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
}

.titulo-divisao {
    font-size: 1.5rem;
    padding-bottom: 10px;
    margin-bottom: 15px;
    font-weight: 600;
    border-bottom: 2px solid;
}
.titulo-divisao.ativo { 
    border-color: #00b894; 
    color: #00b894; 
}
.titulo-divisao.bloqueado { 
    border-color: #e74c3c; 
    color: #e74c3c; 
}

.no-data {
    text-align: center;
    color: #999;
    padding: 30px;
    font-style: italic;
}

.lista-acesso {
    flex-grow: 1;
    overflow-y: auto;
}
.item-header, .item-acesso {
    display: grid;
    grid-template-columns: 2fr 1.5fr 1fr; 
    gap: 15px;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #eee;
}
.item-header {
    font-weight: 700;
    color: #555;
    border-bottom: 2px solid #ddd;
    padding-bottom: 15px;
    margin-bottom: 5px;
    font-size: 0.9em;
}

.info-usuario strong { 
    display: block; 
    font-size: 1.1em; 
    color: #333; 
}
.info-usuario small { 
    color: #888; 
    font-size: 0.9em; 
}
.ip-acesso { 
    font-style: italic; 
    color: #666; 
}

.item-acesso.item-bloqueado {
    background: #ffebeb; 
    opacity: 0.8; 
    border-radius: 5px;
    padding: 10px;
    margin-top: 5px;
    grid-template-columns: 2.5fr 1fr; 
}
.item-bloqueado .info-usuario strong { 
    color: #555; 
}
.item-bloqueado .info-usuario small { 
    color: #777; 
}

.btn-acao {
    padding: 8px 12px;
    border: none;
    border-radius: 5px;
    color: white;
    cursor: pointer;
    font-weight: 600;
    transition: 0.3s;
    white-space: nowrap;
}
.btn-remover { 
    background: #e74c3c; 
}
.btn-remover:hover:not(:disabled) { 
    background: #c0392b; 
}
.btn-reativar { 
    background: #00b894; 
}
.btn-reativar:hover:not(:disabled) { 
    background: #019170; 
}
.btn-acao:disabled { 
    background: #ccc; 
    cursor: not-allowed; 
}
</style>