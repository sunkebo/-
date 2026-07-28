import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：添加 JWT token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器：统一处理错误
request.interceptors.response.use(response => {
  const { code, msg, data } = response.data
  if (code === 200) {
    return data   // 直接返回data，简化调用
  }
  ElMessage.error(msg || '请求失败')
  return Promise.reject(new Error(msg))
}, error => {
  if (error.response) {
    const { status, data } = error.response
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userType')
      const path = window.location.hash || window.location.pathname
      if (path.includes('/admin')) {
        router.push('/admin/login')
      } else {
        router.push('/employee/login')
      }
    }
    ElMessage.error(data?.msg || '请求失败')
  } else {
    ElMessage.error('网络错误，请检查连接')
  }
  return Promise.reject(error)
})

export default request
