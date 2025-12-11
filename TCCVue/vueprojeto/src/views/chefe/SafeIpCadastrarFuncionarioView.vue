<template>
    <div class="cadastro-modal-container">
        
        <div class="header-safeip">
            <h2 class="titulo-safeip">Cadastrar Funcionário</h2>
            <button class="btn-fechar" @click="$emit('back')">✖</button>
        </div>

        <section class="form-card">
            <form @submit.prevent="adicionarFuncionario">
                <p v-if="alertMessage" :class="alertStatus === 'success' ? 'alert-success' : 'alert-error'">
                    {{ alertMessage }}
                </p>

                <label for="nome" class="label-primeiro">Nome</label>
                <input type="text" id="nome" v-model="nome" required />

                <label for="cpf">CPF</label>
                <input
                    type="text"
                    id="cpf"
                    v-model="cpf"
                    placeholder="000.000.000-00"
                    maxlength="14"
                    @input="formatarCPF" 
                    required
                />

                <label for="cargo">Cargo</label>
                <input type="text" id="cargo" v-model="cargo" required />

                <label for="senha">Senha</label>
                <input type="password" id="senha" v-model="senha" @input="validarSenha" required />
                
                <div class="requisitos">
                    <p :class="{ valido: senha.length >= 8, invalido: senha.length < 8 }">
                        {{ senha.length >= 8 ? '✅' : '❌' }} Mínimo 8 caracteres
                    </p>
                    <p :class="{ valido: possuiEspecial, invalido: !possuiEspecial }">
                        {{ possuiEspecial ? '✅' : '❌' }} Contém ao menos 1 caracter especial
                    </p>
                </div>

                <div class="barra-forca">
                    <div
                        class="forca"
                        :style="{
                            width: forcaPercent + '%',
                            backgroundColor: forcaColor,
                            transition: '0.5s',
                        }"
                    ></div>
                </div>
                <p class="texto-forca">{{ forcaTexto }}</p>

                <label for="salario">Salário</label>
                <input type="number" id="salario" v-model.number="salario" required />

                <button type="submit" class="btn-salvar" :disabled="!senhaValida || isSaving">
                    {{ isSaving ? 'Salvando...' : 'Salvar' }}
                </button>
            </form>
        </section>
    </div>
</template>

<script setup>
import { ref, computed, defineEmits } from 'vue'

const emit = defineEmits(['back', 'funcionario-adicionado']);

const nome = ref('')
const cpf = ref('') 
const cargo = ref('')
const salario = ref('')
const senha = ref('')
const possuiEspecial = ref(false)
const isSaving = ref(false)
const alertMessage = ref('')
const alertStatus = ref('')

const cpfLogado = ref(localStorage.getItem('cpfLogado') || '')

const formatarCPF = () => {
    let value = cpf.value.replace(/\D/g, '');
    if (value.length > 11) {
        value = value.substring(0, 11);
    }
    if (value.length > 0) {
        value = value.replace(/(\d{3})(\d)/, '$1.$2');
    }
    if (value.length > 7) {
        value = value.replace(/(\d{3})(\d)/, '$1.$2');
    }
    if (value.length > 11) {
        value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }
    cpf.value = value;
};


const validarSenha = () => {
  const especiais = /[!@#$%^&*()_+\-=[\]{}|;:'",.<>?/]/;
  possuiEspecial.value = especiais.test(senha.value);
}

const forcaPercent = computed(() => {
  let percent = 0
  if (senha.value.length >= 8) percent += 50
  if (possuiEspecial.value) percent += 50
  return percent
})

const forcaColor = computed(() => {
  if (forcaPercent.value < 50) return 'red'
  if (forcaPercent.value < 100) return 'orange'
  return 'green'
})

const forcaTexto = computed(() => {
  if (forcaPercent.value < 50) return 'Senha fraca'
  if (forcaPercent.value < 100) return 'Senha média'
  return 'Senha forte'
})

const senhaValida = computed(() => senha.value.length >= 8 && possuiEspecial.value)

async function adicionarFuncionario() {
  if (!senhaValida.value) {
    alertMessage.value = 'A senha não atende aos requisitos mínimos de segurança.';
    alertStatus.value = 'error';
    return;
  }
  
  const cpfApenasDigitos = cpf.value.replace(/\D/g, '');

  if (cpfApenasDigitos.length !== 11) {
    alertMessage.value = 'O CPF deve conter exatamente 11 dígitos.';
    alertStatus.value = 'error';
    return;
  }
  
  isSaving.value = true;
  alertMessage.value = '';

  try {
    const funcionario = {
      nome: nome.value,
      cpf: cpfApenasDigitos, 
      cargo: cargo.value,
      salario: salario.value,
      senha: senha.value,
      ativo: true 
    }

    const response = await fetch('http://localhost:8080/safeip/equipe', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'cpf-logado': cpfLogado.value
      },
      body: JSON.stringify(funcionario),
    })

    const message = await response.text() 

    if (!response.ok) {
      alertMessage.value = `Erro ao cadastrar: ${message}`;
      alertStatus.value = 'error';
      return;
    }

    alertMessage.value = `Sucesso: ${message}`;
    alertStatus.value = 'success';
    
    nome.value = '';
    cpf.value = ''; 
    cargo.value = '';
    salario.value = '';
    senha.value = '';
    
    emit('funcionario-adicionado'); 
    
  } catch (error) {
    console.error(error)
    alertMessage.value = 'Erro de conexão ao cadastrar funcionário.';
    alertStatus.value = 'error';
  } finally {
    isSaving.value = false;
  }
}
</script>

<style scoped>
.cadastro-modal-container { 
    flex-grow: 1; 
    padding: 10px; 
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
    border-bottom: 1px solid #e0e0e0; 
    padding-bottom: 5px;
    margin-bottom: 5px; 
}
.titulo-safeip {
    font-size: 1.6rem; 
    color: #242424;
    font-weight: 700;
}

.btn-fechar {
    border: none;
    background: #e74c3c;
    color: white;
    border-radius: 50%;
    cursor: pointer;
    width: 26px; 
    height: 26px;
    font-size: 14px;
    line-height: 1;
    transition: background 0.2s;
    font-weight: bold;
}
.btn-fechar:hover {
    background: #c0392b;
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 10px 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  max-width: 450px;
  margin: 0 auto; 
  width: 100%;
}
label {
  display: block;
  margin: 6px 0 3px;
  font-weight: bold;
  color: #333;
}
.label-primeiro {
  margin-top: 0px !important; 
}

input {
  width: 100%;
  padding: 7px;
  margin-bottom: 7px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 14px;
  box-sizing: border-box;
}
.btn-salvar {
  background: #5e2ced;
  color: white;
  border: none;
  padding: 9px; 
  border-radius: 6px;
  cursor: pointer;
  width: 100%;
  font-weight: bold;
  transition: 0.3s;
  margin-top: 8px;
}
.btn-salvar:hover:not(:disabled) {
  background: #4a37e0;
}
.btn-salvar:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.requisitos {
    margin-bottom: 3px;
}
.requisitos p {
  margin: 2px 0; 
  font-size: 12px;
}
.valido {
  color: green;
}
.invalido {
  color: red;
}
.barra-forca {
  background: #ddd;
  height: 5px; 
  border-radius: 2.5px;
  margin-bottom: 3px; 
  width: 100%;
  overflow: hidden;
}
.forca {
  height: 100%;
  border-radius: 2.5px;
  transition: width 0.5s ease, background-color 0.5s ease;
}
.texto-forca {
  font-size: 11px; 
  font-weight: bold;
  margin-bottom: 8px;
}

.alert-success, .alert-error {
    padding: 7px;
    border-radius: 4px;
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 600;
}
.alert-success {
    background: #e6ffe6;
    border: 1px solid #00b894;
    color: #00b894;
}
.alert-error {
    background: #ffe6e6;
    border: 1px solid #e74c3c;
    color: #e74c3c;
}
</style>