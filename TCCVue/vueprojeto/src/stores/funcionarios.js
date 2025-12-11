import { defineStore } from "pinia"

export const useFuncionariosStore = defineStore("funcionarios", {
  state: () => ({
    funcionarios: JSON.parse(localStorage.getItem("funcionarios") || "[]")
  }),

  actions: {
    adicionarFuncionario(novoFuncionario) {
      const existe = this.funcionarios.some(f => f.cpf === novoFuncionario.cpf)
      if (existe) {
        throw new Error("Já existe um funcionário com esse CPF")
      }

      this.funcionarios.push(novoFuncionario)

      localStorage.setItem("funcionarios", JSON.stringify(this.funcionarios))
    },

    carregarFuncionarios() {
      this.funcionarios = JSON.parse(localStorage.getItem("funcionarios") || "[]")
    },
    atualizarFuncionario(idFuncionario, novosDados){
      const index = this.funcionarios.findIndex(f => f.id === idFuncionario)

      if(index !== -1){
        this.funcionarios[index] = {
          ...this.funcionarios[index],
          ...novosDados
        }

        localStorage.setItem("funcionarios", JSON.stringify(this.funcionarios))
        return true
      }
      return false
    },
    demitirFuncionario(idFuncionario){
      const novoArray = this.funcionarios.filter(f => f.id !== idFuncionario)

      if(novoArray.length < this.funcionarios.length){
        this.funcionarios = novoArray
        localStorage.setItem("funcionarios", JSON.stringify(this.funcionarios))
        return true
      }
      return false
    }
  }
})
