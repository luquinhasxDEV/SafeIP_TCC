import { defineStore } from 'pinia'

export const useVendasStore = defineStore('vendas', {
  state: () => ({
    lista: []
  }),
  actions: {
    adicionarVenda(venda) {
      this.lista.push({ id: Date.now(), ...venda })
    }
  }
})
  