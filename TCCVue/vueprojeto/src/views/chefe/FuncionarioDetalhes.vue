<template>
  <div class="layout">
    <SidebarGerente />

    <div class="content">
      <h1>Gerenciar Funcionário</h1>

      <div v-if="loading" class="loading">Carregando...</div>

      <div v-else class="card">
        <label>Nome:</label>
        <input v-model="funcionario.nome" type="text" />

        <label>CPF:</label>
        <input v-model="funcionario.cpf" type="text" readonly />

        <label>Cargo:</label>
        <select v-model="funcionario.cargo">
          <option value="FUNCIONARIO">Funcionário</option>
          <option value="GERENTE">Gerente</option>
        </select>

        <label>Salário:</label>
        <input v-model.number="funcionario.salario" type="number" />

        <label>Status:</label>
        <p :class="funcionario.ativo ? 'ativo' : 'inativo'">
          {{ funcionario.ativo ? "Ativo" : "Inativo" }}
        </p>

        <div class="actions">
          <button class="btn-salvar" @click="salvarEdicoes">
            Salvar Alterações
          </button>

          <button
            v-if="funcionario.ativo"
            class="btn-demitir"
            @click="alterarStatus(false)"
          >
            Demitir Funcionário
          </button>

          <button
            v-else
            class="btn-reativar"
            @click="alterarStatus(true)"
          >
            Reativar Funcionário
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import axios from "axios"; 

export default {
  name: "FuncionarioDetalhes",

  components: {
    SidebarGerente,
  },

  data() {
    return {
      funcionario: {},
      loading: true,
    };
  },

  async created() {
    const baseUrl = "http://localhost:8080/safeip"; 
    const id = this.$route.params.id;

    try {
      const response = await axios.get(`${baseUrl}/equipe/${id}`);
      this.funcionario = response.data;
    } catch (error) {
      console.error("Erro ao carregar funcionário:", error);
      alert("Erro ao carregar funcionário! Verifique o console.");
    }

    this.loading = false;
  },

  methods: {
    getBaseUrl() {
        return "http://localhost:8080/safeip";
    },

    async salvarEdicoes() {
      try {
        const idFuncionario = this.funcionario.id;
        if (!idFuncionario) {
            alert("ID do funcionário não encontrado.");
            return;
        }
        const salarioLimpo = this.funcionario.salario === "" || this.funcionario.salario === null ? null : Number(this.funcionario.salario);

        const dados = {
          nome: this.funcionario.nome,
          cargo: this.funcionario.cargo,
          salario: salarioLimpo,
          ativo: this.funcionario.ativo,
          senha: null, 
        };

        await axios.put(
          `${this.getBaseUrl()}/equipe/${idFuncionario}`,
          dados,
          {
            headers: {
              "cpf-logado": localStorage.getItem("cpf"), 
            },
          }
        );

        alert("Alterações salvas com sucesso!");
      } catch (error) {
        console.error("Erro ao salvar alterações:", error);
        alert("Erro ao salvar alterações! Verifique o console.");
      }
    },

    async alterarStatus(novoStatus) {
      if (!confirm(`Tem certeza que deseja ${novoStatus ? 'reativar' : 'demitir'} o funcionário ${this.funcionario.nome}?`)) {
        return;
      }

      try {
        const idFuncionario = this.funcionario.id;
        if (!idFuncionario) {
            alert("ID do funcionário não encontrado.");
            return;
        }

        const salarioLimpo = this.funcionario.salario === "" || this.funcionario.salario === null ? null : Number(this.funcionario.salario);

        const dados = {
          nome: this.funcionario.nome,
          cargo: this.funcionario.cargo,
          salario: salarioLimpo,
          ativo: novoStatus,
          senha: null,
        };

        await axios.put(
          `${this.getBaseUrl()}/equipe/${idFuncionario}`,
          dados,
          {
            headers: {
              "cpf-logado": localStorage.getItem("cpf"),
            },
          }
        );

        this.funcionario.ativo = novoStatus;

        alert(novoStatus ? "Funcionário reativado com sucesso!" : "Funcionário demitido/desativado com sucesso!");
      } catch (error) {
        console.error("Erro ao atualizar status:", error);
        alert("Erro ao atualizar status! Verifique o console.");
      }
    },
  },
};
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.content {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  width: 450px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.12);
}

input,
select {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.actions {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn-salvar {
  background: #3b82f6;
  color: white;
  padding: 10px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
}

.btn-demitir {
  background: #dc2626;
  color: white;
  padding: 10px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
}

.btn-reativar {
  background: #16a34a;
  color: white;
  padding: 10px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
}

.ativo {
  color: #16a34a;
  font-weight: bold;
}

.inativo {
  color: #dc2626;
  font-weight: bold;
}
</style>