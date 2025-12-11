<template>
  <div class="arquivos-modal-container" @dragover.prevent @drop="handleDrop">
    
    <div class="header-safeip">
        <button class="btn-voltar" @click="$emit('back')">← Voltar</button> 
        <h2 class="titulo-safeip">Gerenciamento de Arquivos Restritos</h2>
    </div>

    <div 
        :class="['upload-area', { 'drag-over': isDragging }]" 
        @dragenter="isDragging = true" 
        @dragleave="isDragging = false"
        @drop="isDragging = false"
        @click="triggerFileInput"
    >
        <p class="upload-icon">📁</p>
        <p>Arraste e solte arquivos **.xlsx** aqui ou clique para selecionar.</p>
        <input 
            type="file" 
            ref="fileInput" 
            @change="handleFileChange" 
            accept=".xlsx" 
            style="display: none;" 
        />
        <p v-if="uploadMessage" :class="uploadStatus === 'success' ? 'success-message' : 'error-message'">
            {{ uploadMessage }}
        </p>
    </div>
    
    <h3 class="lista-titulo">Arquivos Ativos (Acesso Restrito)</h3>

    <div class="pastas-grid">
      <div v-for="pasta in pastas" :key="pasta.id" class="pasta">
        <img src="@/assets/t (1).webp" alt="ícone pasta" @click="abrirPasta(pasta)">
        <p @click="abrirPasta(pasta)">{{ pasta.nome }}</p>
        <div class="action-buttons">
            <button @click="abrirPasta(pasta)" class="btn-acesso">
                Acessar (2FA)
            </button>
        </div>
      </div>
      <p v-if="pastas.length === 0" class="no-files-message">Nenhum arquivo .xlsx ativo. Faça o upload acima.</p>
    </div>

    <div v-if="pastaSelecionada" class="modal-bg-interno">
      <div class="modal-content">
        <h3>Abrir arquivo: {{ pastaSelecionada.nome }}</h3>
        <p>Digite o <strong>Código de Acesso</strong> (Gerado via 2FA) para liberar o arquivo.</p>
        <input type="password" v-model="codigoAcesso" placeholder="Insira o Código de Acesso" />
        <p v-if="mensagemErro" class="mensagem-erro">{{ mensagemErro }}</p>
        <div class="botoes">
          <button @click="fecharModal">Cancelar</button>
          <button @click="verificarCodigoAcesso" :disabled="isVerifying || !codigoAcesso">
              {{ isVerifying ? 'Verificando...' : 'Confirmar Acesso' }}
          </button>
        </div>
      </div>
    </div>
    
    <div v-if="mensagemSucesso" class="modal-bg-interno">
        <div class="modal-content modal-sucesso">
            <h3>Acesso Liberado!</h3>
            <p>O arquivo <strong>{{ pastaSucesso.nome }}</strong> foi {{ pastaSucesso.arquivoUrl ? 'aberto em uma nova aba.' : 'baixado com sucesso.' }}</p>
            <button @click="fecharMensagemSucesso">Entendi</button>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, defineEmits, onMounted, watch } from 'vue';
import axios from 'axios';

const emit = defineEmits(['back']); 

const STORAGE_KEY = 'safeip_uploaded_files';

const HARDCODED_FILES = [
    { nome: "Metas Futuras.xlsx", id: 1, arquivoUrl: '/docs/MetasFuturas.xlsx', localFileUrl: null }, 
    { nome: "Metas de vendas.xlsx", id: 2, arquivoUrl: '/docs/MetasDeVendas.xlsx', localFileUrl: null },
];

const pastas = ref([]); 


const loadPastasFromStorage = () => {
    const storedPastas = localStorage.getItem(STORAGE_KEY);
    const persistedFiles = storedPastas ? JSON.parse(storedPastas) : [];
    
    pastas.value = [...HARDCODED_FILES, ...persistedFiles];
};

const savePastasToStorage = () => {
    const filesToPersist = pastas.value.filter(p => p.localFileUrl !== null);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(filesToPersist));
};

watch(pastas, savePastasToStorage, { deep: true });

onMounted(() => {
    loadPastasFromStorage();
});


const pastaSelecionada = ref(null);
const codigoAcesso = ref("");
const isVerifying = ref(false);
const mensagemErro = ref("");
const mensagemSucesso = ref(false);
const pastaSucesso = ref(null);
const isDragging = ref(false);
const uploadMessage = ref("");
const uploadStatus = ref("");
const fileInput = ref(null);


const triggerFileInput = () => {
    fileInput.value.click();
};

const handleFileChange = (event) => {
    const files = event.target.files;
    if (files.length) {
        processFiles(files);
    }
};

const handleDrop = (event) => {
    event.preventDefault();
    isDragging.value = false;
    const files = event.dataTransfer.files;
    processFiles(files);
};

const processFiles = (files) => {
    if (files.length > 0) {
        const file = files[0];
        if (file.name.endsWith('.xlsx')) {
            const localUrl = URL.createObjectURL(file); 
            
            pastas.value.push({
                nome: file.name,
                id: Date.now(), 
                arquivoUrl: null, 
                localFileUrl: localUrl 
            });

            uploadMessage.value = `Arquivo "${file.name}" importado com sucesso!`;
            uploadStatus.value = 'success';
        } else {
            uploadMessage.value = "Por favor, selecione apenas arquivos .xlsx (Excel).";
            uploadStatus.value = 'error';
        }
    }
};


const executeDownload = (pasta) => {
    if (pasta.localFileUrl) {
        const link = document.createElement('a');
        link.href = pasta.localFileUrl;
        link.setAttribute('download', pasta.nome);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } else if (pasta.arquivoUrl) {
        window.open(pasta.arquivoUrl, '_blank');
    }
};

const abrirPasta = (pasta) => {
    pastaSelecionada.value = pasta;
    mensagemErro.value = ""; 
    codigoAcesso.value = ""; 
};

const fecharModal = () => {
    pastaSelecionada.value = null;
    codigoAcesso.value = "";
    mensagemErro.value = "";
};

const fecharMensagemSucesso = () => {
    mensagemSucesso.value = false;
    pastaSucesso.value = null;
    fecharModal(); 
};

const verificarCodigoAcesso = async () => {
    if (!codigoAcesso.value) {
        mensagemErro.value = "Por favor, insira o Código de Acesso.";
        return;
    }

    mensagemErro.value = "";
    isVerifying.value = true;
    
    try {
        const cpfLogado = localStorage.getItem("cpfLogado")?.trim();
        if (!cpfLogado) {
            mensagemErro.value = "Erro: CPF do usuário logado não encontrado.";
            isVerifying.value = false;
            return;
        }

        const response = await axios.post(
            'http://localhost:8080/safeip/verificarCodigo', 
            {
                codigo: codigoAcesso.value,
                tipo: 'Acesso' 
            },
            {
                headers: {
                    'Content-Type': 'application/json',
                    'cpf-logado': cpfLogado
                }
            }
        );

        if (response.data.valido) {
            const pasta = pastaSelecionada.value;
            executeDownload(pasta); 
            
            pastaSucesso.value = pasta;
            mensagemSucesso.value = true;
        } else {
            mensagemErro.value = "Código de Acesso Inválido ou Expirado! Tente gerar um novo.";
        }

    } catch (error) {
        console.error("Erro ao verificar código:", error.response?.data || error);
        mensagemErro.value = "Erro de conexão com o servidor. Tente novamente.";
    } finally {
        isVerifying.value = false;
    }
};
</script>

<style scoped>
.arquivos-modal-container { 
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
    align-items: center; border-bottom: 2px solid #e0e0e0; 
    padding-bottom: 15px; 
    margin-bottom: 25px; 
}
.titulo-safeip { font-size: 1.8rem; color: #242424; font-weight: 700; flex-grow: 1; text-align: center; }
.btn-voltar { background: #5e2ced; color: white; border: none; padding: 10px 18px; border-radius: 5px; cursor: pointer; font-weight: 600; transition: background 0.2s; white-space: nowrap; }
.btn-voltar:hover { background: #4a37e0; }
.upload-area { border: 3px dashed #ccc; border-radius: 15px; padding: 30px; text-align: center; cursor: pointer; transition: all 0.3s; background-color: #f0f4ff; color: #555; margin-bottom: 30px; }
.upload-area.drag-over { border-color: #5e2ced; background-color: #e6ebff; box-shadow: 0 0 10px rgba(94, 44, 237, 0.2); }
.upload-icon { font-size: 40px; margin-bottom: 10px; }
.success-message { color: #27ae60; font-weight: 600; margin-top: 15px; }
.error-message { color: #e74c3c; font-weight: 600; margin-top: 15px; }
.lista-titulo { font-size: 1.4rem; color: #0d1b3e; margin-bottom: 20px; border-bottom: 1px solid #ddd; padding-bottom: 5px; }
.no-files-message { width: 100%; text-align: center; color: #999; font-style: italic; margin-top: 20px; }
.pastas-grid { display: flex; gap: 30px; flex-wrap: wrap; justify-content: flex-start; }
.pasta { width: 150px; padding: 10px; text-align: center; border-radius: 8px; display: flex; flex-direction: column; align-items: center; }
.pasta img { width: 100px; height: 100px; margin-bottom: 5px; cursor: pointer; transition: transform 0.2s; }
.pasta img:hover { transform: scale(1.05); }
.pasta p { font-weight: 600; color: #333; cursor: pointer; margin-bottom: 10px; }
.action-buttons { display: flex; justify-content: center; width: 100%; }
.btn-acesso { padding: 8px 15px; border: none; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s; background: #5e2ced; color: white; }
.btn-acesso:hover { background: #4a37e0; }
.modal-bg-interno { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.7); display: flex; justify-content: center; align-items: center; z-index: 10; }
.modal-content { background: white; padding: 30px; border-radius: 12px; text-align: center; width: 350px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5); }
.modal-content input { width: 85%; padding: 12px 15px; margin: 15px 0 10px 0; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; outline: none; transition: border-color 0.3s, box-shadow 0.3s; }
.modal-content input:focus { border-color: #5e2ced; box-shadow: 0 0 0 3px rgba(94, 44, 237, 0.2); }
.mensagem-erro { color: #e74c3c; font-weight: bold; margin-bottom: 15px; }
.modal-sucesso { border-color: #00b894; background-color: #f7fff7; }
.modal-sucesso h3 { color: #00b894; }
.modal-sucesso button { background: #00b894 !important; color: white !important; }
.botoes { display: flex; justify-content: space-between; margin-top: 20px; }
.botoes button { padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; transition: background-color 0.3s; }
.botoes button:first-child { background: #e0e0e0; color: #333; }
.botoes button:last-child { background: #5e2ced; color: white; }
.botoes button:hover:not(:disabled) { opacity: 0.9; }
.botoes button:disabled { cursor: not-allowed; opacity: 0.6; }
</style>