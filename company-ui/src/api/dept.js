import request from '@/utils/request'

export const deptApi = {
  list()     { return request.get('/admin/depts') },
  tree()     { return request.get('/admin/depts/tree') },
  get(id)    { return request.get(`/admin/depts/${id}`) },
  create(d)  { return request.post('/admin/depts', d) },
  update(id, d) { return request.put(`/admin/depts/${id}`, d) },
  delete(id) { return request.delete(`/admin/depts/${id}`) }
}
