import request from '@/utils/request'

export const postApi = {
  list(params) { return request.get('/admin/posts', { params }) },
  get(id)      { return request.get(`/admin/posts/${id}`) },
  create(d)    { return request.post('/admin/posts', d) },
  update(id, d) { return request.put(`/admin/posts/${id}`, d) },
  delete(id)   { return request.delete(`/admin/posts/${id}`) }
}
