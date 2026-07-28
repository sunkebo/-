import request from '@/utils/request'

export const salaryApi = {
  // 管理员
  list(params)   { return request.get('/admin/salaries', { params }) },
  create(d)      { return request.post('/admin/salaries', d) },
  batchCreate(list) { return request.post('/admin/salaries/batch', list) },
  batchGenerate(d) { return request.post('/admin/salaries/batch-generate', d) },
  update(id, d)  { return request.put(`/admin/salaries/${id}`, d) },
  delete(id)     { return request.delete(`/admin/salaries/${id}`) },
  audit(id, d)   { return request.put(`/admin/salaries/${id}/audit`, d) },
  stats(params)  { return request.get('/admin/salaries/stats', { params }) },
  // 员工
  myList(params) { return request.get('/employee/salaries', { params }) }
}
