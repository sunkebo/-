import request from '@/utils/request'

export const userApi = {
  list(params)   { return request.get('/admin/users', { params }) },
  get(id)        { return request.get(`/admin/users/${id}`) },
  create(d)      { return request.post('/admin/users', d) },
  update(id, d)  { return request.put(`/admin/users/${id}`, d) },
  delete(id)     { return request.delete(`/admin/users/${id}`) },
  resetPassword(id) { return request.put(`/admin/users/${id}/reset-password`) },
  updateStatus(id, status) { return request.put(`/admin/users/${id}/status?status=${status}`) }
}
