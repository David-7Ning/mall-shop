/**
 * API 接口定义（RESTful 风格）
 * 统一管理所有后端接口
 */

import { get, post, put, del } from '../utils/request'

// ==================== 用户相关接口 ====================

/**
 * 用户登录
 * @param {Object} data - 登录数据 { username, password }
 */
export function login(data) {
  return post('/user/login', data)
}

/**
 * 用户注册
 * @param {Object} data - 注册数据 { username, password, nickname }
 */
export function register(data) {
  return post('/user/register', data)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return get('/user/info')
}

// ==================== 商品相关接口 ====================

/**
 * 获取商品列表
 * @param {Object} params - 查询参数 { categoryId, keyword, page, pageSize }
 */
export function getProductList(params) {
  return get('/product/list', params)
}

/**
 * 获取商品详情
 * @param {Number} id - 商品ID
 */
export function getProductDetail(id) {
  return get(`/product/${id}`)
}

/**
 * 搜索商品
 * @param {String} keyword - 搜索关键词
 */
export function searchProducts(keyword) {
  return get('/product/search', { keyword })
}

// ==================== 购物车相关接口 ====================

/**
 * 获取购物车列表
 * @param {Object} params - 查询参数 { userId }
 */
export function getCartList(params) {
  return get('/cart/items', params)
}

/**
 * 添加商品到购物车
 * @param {Object} data - 购物车数据 { userId, productId, quantity }
 */
export function addCart(data) {
  return post('/cart/items', data)
}

/**
 * 更新购物车商品数量
 * @param {Number} productId - 商品ID
 * @param {Object} data - 更新数据 { userId, quantity }
 */
export function updateCart(productId, data) {
  return put(`/cart/items/${productId}`, data)
}

/**
 * 删除购物车商品
 * @param {Number} productId - 商品ID
 * @param {Object} data - 删除数据 { userId }
 */
export function deleteCart(productId, data) {
  return del(`/cart/items/${productId}`, data)
}

// ==================== 订单相关接口 ====================

/**
 * 创建订单
 * @param {Object} data - 订单数据
 */
export function createOrder(data) {
  return post('/order', data)
}

/**
 * 获取订单列表
 * @param {Object} params - 查询参数 { userId, status }
 */
export function getOrderList(params) {
  return get('/order', params)
}

/**
 * 获取订单详情
 * @param {Number} id - 订单ID
 */
export function getOrderDetail(id) {
  return get(`/order/${id}`)
}

/**
 * 取消订单
 * @param {Number} id - 订单ID
 */
export function cancelOrder(id) {
  return put(`/order/${id}/cancel`)
}

/**
 * 支付订单
 * @param {Number} id - 订单ID
 */
export function payOrder(id) {
  return put(`/order/${id}/pay`)
}

// ==================== 收货地址相关接口 ====================

/**
 * 获取收货地址列表
 * @param {Object} params - 查询参数 { userId }
 */
export function getAddressList(params) {
  return get('/address/list', params)
}

/**
 * 获取收货地址详情
 * @param {Number} id - 地址ID
 */
export function getAddressDetail(id) {
  return get(`/address/${id}`)
}

/**
 * 添加收货地址
 * @param {Object} data - 地址数据
 */
export function addAddress(data) {
  return post('/address', data)
}

/**
 * 更新收货地址
 * @param {Number} id - 地址ID
 * @param {Object} data - 地址数据
 */
export function updateAddress(id, data) {
  return put(`/address/${id}`, data)
}

/**
 * 删除收货地址
 * @param {Number} id - 地址ID
 */
export function deleteAddress(id) {
  return del(`/address/${id}`)
}

/**
 * 设置默认地址
 * @param {Number} id - 地址ID
 * @param {Number} userId - 用户ID
 */
export function setDefaultAddress(id, userId) {
  return put(`/address/${id}/default`, { userId })
}