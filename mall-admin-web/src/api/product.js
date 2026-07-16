import request from './request'

// 管理员登录
export const login = (data) => {
  return request.post('/admin/login', data)
}

// 获取商品列表
export const getProductList = (params) => {
  return request.get('/admin/product/list', { params })
}

// 新增商品
export const addProduct = (data) => {
  return request.post('/admin/product', data)
}

// 编辑商品
export const updateProduct = (data) => {
  return request.put('/admin/product', data)
}

// 删除商品
export const deleteProduct = (id) => {
  return request.delete(`/admin/product/${id}`)
}

// 切换上下架状态
export const updateProductStatus = (id, status) => {
  return request.put(`/admin/product/${id}/status`, null, { params: { status } })
}

// 获取分类列表
export const getCategoryList = () => {
  return request.get('/admin/product/categories')
}