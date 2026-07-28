import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const userType = ref(localStorage.getItem('userType') || '')

  async function login(username, password) {
    const res = await authApi.login(username, password)
    token.value = res.token
    userInfo.value = res.userInfo
    userType.value = res.userInfo.userType
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res.userInfo))
    localStorage.setItem('userType', res.userInfo.userType)
    return res
  }

  async function fetchUserInfo() {
    const info = await authApi.getInfo()
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    return info
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    userType.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userType')
  }

  return { token, userInfo, userType, login, fetchUserInfo, logout }
})
