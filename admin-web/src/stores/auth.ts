import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AdminUser, LoginParams } from '@/api/auth'
import * as authApi from '@/api/auth'

const TOKEN_KEY = 'xq_admin_token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const admin = ref<AdminUser | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const permissions = computed(() => admin.value?.permissions || [])
  const roles = computed(() => admin.value?.roles || [])

  function hasPermission(perm: string): boolean {
    if (roles.value.includes('SUPER_ADMIN')) return true
    return permissions.value.includes(perm)
  }

  async function login(params: LoginParams) {
    const res = await authApi.login(params)
    if (res.success && res.data) {
      token.value = res.data.accessToken
      admin.value = {
        id: 0,
        username: params.username,
        displayName: res.data.displayName,
        roles: [res.data.role],
        permissions: [],
      }
      localStorage.setItem(TOKEN_KEY, res.data.accessToken)
    }
    return res
  }

  async function fetchCurrentAdmin() {
    if (admin.value) return { success: true, data: admin.value }
    try {
      const res = await authApi.getCurrentAdmin()
      if (res.success && res.data) {
        admin.value = res.data
      }
      return res
    } catch {
      if (token.value) {
        try {
          const payload = JSON.parse(atob(token.value.split('.')[1]))
          admin.value = {
            id: Number(payload.sub) || 0,
            username: '',
            displayName: payload.role || 'Admin',
            roles: [payload.role || 'ADMIN'],
            permissions: [],
          }
        } catch { /* invalid token */ }
      }
      return { success: true, data: admin.value }
    }
  }

  function logout() {
    token.value = ''
    admin.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, admin, isLoggedIn, permissions, roles, hasPermission, login, fetchCurrentAdmin, logout }
})
