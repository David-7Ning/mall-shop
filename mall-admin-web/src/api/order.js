import request from './request'

// 获取订单列表
export const getOrderList = (params) => {
  return request.get('/admin/order/list', { params })
}

// 发货
export const shipOrder = (id) => {
  return request.put(`/admin/order/${id}/ship`)
}