import request from './request'

// 获取用户列表
export const getUserList = (params) => {
  return request.get('/admin/user/list', { params })
}

// 禁用/启用用户
export const updateUserStatus = (id, status) => {
  return request.put(`/admin/user/${id}/status`, null, { params: { status } })
}