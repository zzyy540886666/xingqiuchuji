import { defineStore } from 'pinia'
import { operationalConfig } from '@/mock/data'

export const useConfigStore = defineStore('config', {
  state: () => ({
    loaded: false,
    config: operationalConfig
  }),
  actions: {
    bootstrap() {
      this.loaded = true
      this.config = operationalConfig
    }
  }
})
