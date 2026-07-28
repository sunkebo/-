import request from '@/utils/request'

export const checkinApi = {
  // 管理员
  list(params)   { return request.get('/admin/checkins', { params }) },
  audit(id, d)   { return request.put(`/admin/checkins/${id}/audit`, d) },
  stats(params)  { return request.get('/admin/checkins/stats', { params }) },
  // 员工
  punch(type)    { return request.post('/employee/checkin/punch', { type }) },
  today()        { return request.get('/employee/checkin/today') },
  myList(params) { return request.get('/employee/checkins', { params }) },
  supplement(d)  { return request.post('/employee/checkins/supplement', d) }
}
