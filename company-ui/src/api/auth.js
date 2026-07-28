import request from '@/utils/request'

export const authApi = {
  login(username, password) {
    return request.post('/auth/login', { username, password })
  },
  getInfo() {
    return request.get('/auth/info')
  },
  changePassword(oldPassword, newPassword) {
    return request.put('/auth/password', { oldPassword, newPassword })
  }
}
