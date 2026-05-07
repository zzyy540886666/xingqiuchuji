import { defineStore } from 'pinia'

interface UserProfile {
  id: string
  mobile: string
  nickname: string
  level: string
}

export const useSessionStore = defineStore('session', {
  state: () => ({
    sessionToken: '',
    user: null as UserProfile | null
  }),
  getters: {
    isLoggedIn: state => Boolean(state.sessionToken)
  },
  actions: {
    restore() {
      const token = uni.getStorageSync('sessionToken')
      if (token) {
        this.sessionToken = token
      }
      this.user = {
        id: 'u-demo',
        mobile: '17768306830',
        nickname: '星球伙伴',
        level: 'Lv.1 原住民'
      }
    },
    setToken(token: string) {
      this.sessionToken = token
      uni.setStorageSync('sessionToken', token)
    },
    clear() {
      this.sessionToken = ''
      this.user = null
      uni.removeStorageSync('sessionToken')
    }
  }
})
