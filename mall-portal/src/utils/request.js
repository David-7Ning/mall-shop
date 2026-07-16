/**
 * HTTP 请求封装
 * 基于 uni.request 封装，统一处理请求和响应
 */

// 后端 API 基础地址
const BASE_URL = 'http://localhost:8080/api'

/**
 * 发起 HTTP 请求
 * @param {Object} options - 请求配置
 * @returns {Promise} 请求结果
 */
function request(options) {
  return new Promise((resolve, reject) => {
    
    // 获取 token（如果已登录）
    const token = uni.getStorageSync('token') || ''
    
    // 发起请求
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      timeout: 10000,  // 设置超时时间为 10 秒
      success: (res) => {
        // 请求成功
        if (res.statusCode === 200) {
          // 后端返回格式：{ code: 200, msg: '成功', data: {} }
          if (res.data.code === 200) {
            // 如果 data 为 null，返回成功标识（适用于添加、删除等操作）
            if (res.data.data === null || res.data.data === undefined) {
              resolve({ code: 200, message: res.data.msg || '操作成功' })
            } else {
              resolve(res.data.data)
            }
          } else {
            // 业务错误
            uni.showToast({
              title: res.data.msg || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else if (res.statusCode === 401) {
          // 未登录，跳转到登录页
          uni.showToast({ title: '请先登录', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 1500)
          reject(res)
        } else {
          // 其他错误
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        // 网络错误
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * GET 请求
 * @param {String} url - 请求地址
 * @param {Object} params - 请求参数
 */
export function get(url, params = {}) {
  // 将参数拼接到 URL 上（GET 请求）
  const queryString = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = queryString ? `${url}?${queryString}` : url
  return request({ url: fullUrl, method: 'GET' })
}

/**
 * POST 请求
 * @param {String} url - 请求地址
 * @param {Object} data - 请求数据
 */
export function post(url, data = {}) {
  return request({ url, method: 'POST', data })
}

/**
 * PUT 请求
 * @param {String} url - 请求地址
 * @param {Object} data - 请求数据
 */
export function put(url, data = {}) {
  return request({ url, method: 'PUT', data })
}

/**
 * DELETE 请求
 * @param {String} url - 请求地址
 * @param {Object} params - 请求参数
 */
export function del(url, params = {}) {
  return request({ url, method: 'DELETE', data: params })
}