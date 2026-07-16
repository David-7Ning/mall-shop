<template>
  <!-- 订单详情页面 -->
  <view class="container">
    
    <!-- 📦 订单状态 -->
    <view class="status-section">
      <view class="status-icon">
        <text v-if="order.status === 0">💰</text>
        <text v-else-if="order.status === 1"></text>
        <text v-else-if="order.status === 2">🚚</text>
        <text v-else-if="order.status === 3">✅</text>
        <text v-else-if="order.status === 4">❌</text>
      </view>
      <text class="status-text">{{ getStatusText(order.status) }}</text>
    </view>

    <!-- 📍 收货地址 -->
    <view class="address-section">
      <view class="section-title">收货信息</view>
      <view class="address-info">
        <text class="receiver-name">{{ order.address }}</text>
      </view>
    </view>

    <!-- 🛒 商品信息 -->
    <view class="product-section">
      <view class="section-title">商品信息</view>
      <view 
        class="product-item" 
        v-for="item in orderItems" 
        :key="item.id"
      >
        <image 
          class="product-image" 
          :src="item.productImage || '/static/placeholder.png'" 
          mode="aspectFill"
        />
        <view class="product-info">
          <text class="product-name">{{ item.productName }}</text>
          <view class="product-bottom">
            <text class="product-price">¥{{ item.price }}</text>
            <text class="product-quantity">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 💰 价格明细 -->
    <view class="price-section">
      <view class="section-title">价格明细</view>
      <view class="price-item">
        <text class="price-label">商品总价</text>
        <text class="price-value">¥{{ goodsTotal.toFixed(2) }}</text>
      </view>
      <view class="price-item total">
        <text class="price-label">实付金额</text>
        <text class="price-value">¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</text>
      </view>
    </view>

    <!-- 📝 订单信息 -->
    <view class="info-section">
      <view class="section-title">订单信息</view>
      <view class="info-item">
        <text class="info-label">订单编号</text>
        <text class="info-value">{{ order.orderNo }}</text>
      </view>
      <view class="info-item">
        <text class="info-label">创建时间</text>
        <text class="info-value">{{ formatTime(order.createTime) }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="action-bar" v-if="order.status === 0">
      <button class="cancel-btn" @click="cancelOrder">取消订单</button>
      <button class="pay-btn" @click="payOrder">立即支付</button>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderDetail } from '../../api/index'

// ==================== 响应式数据 ====================

/** 订单数据 */
const order = ref({})

/** 订单商品明细 */
const orderItems = ref([])

/** 登录状态 */
const isLoggedIn = ref(false)

// ==================== 计算属性 ====================

/** 商品总价 */
const goodsTotal = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// ==================== 页面生命周期 ====================

/** 页面加载时执行 */
onMounted(() => {
  checkLoginStatus()
})

/** 页面显示时执行 */
onShow(() => {
  checkLoginStatus()
})

// ==================== 方法定义 ====================

/**
 * 检查登录状态
 */
function checkLoginStatus() {
  const token = uni.getStorageSync('token')
  const user = uni.getStorageSync('userInfo')
  
  if (token && user) {
    isLoggedIn.value = true
    loadOrderDetail()
  } else {
    isLoggedIn.value = false
    
    uni.showModal({
      title: '提示',
      content: '请先登录后查看订单详情',
      showCancel: false,
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/login' })
        }
      }
    })
  }
}

// ==================== 方法定义 ====================

/**
 * 加载订单详情
 */
async function loadOrderDetail() {
  try {
    // 从页面参数获取订单ID
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const orderId = currentPage.options.id
    
    if (!orderId) {
      uni.showToast({ title: '订单ID不存在', icon: 'none' })
      return
    }
    
    // 调用后端 API 获取订单详情
    const data = await getOrderDetail(orderId)
    order.value = data || {}
    orderItems.value = data?.items || []
    
    console.log('订单详情加载成功:', order.value)
  } catch (error) {
    console.error('加载订单详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

/**
 * 获取订单状态文本
 * @param {Number} status - 订单状态
 */
function getStatusText(status) {
  const statusMap = {
    0: '待付款',
    1: '已付款',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 格式化时间
 * @param {String} time - 时间字符串
 */
function formatTime(time) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

/**
 * 取消订单
 */
async function cancelOrder() {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用取消订单 API
          uni.showToast({ title: '订单已取消', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          console.error('取消订单失败:', error)
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 支付订单
 */
function payOrder() {
  uni.showToast({ title: '支付功能开发中', icon: 'none' })
  // TODO: 后续对接支付功能
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

/* ==================== 订单状态样式 ==================== */
.status-section {
  background: linear-gradient(135deg, #ff6700, #ff8c00);
  padding: 60rpx 30rpx;
  text-align: center;
}

.status-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.status-text {
  font-size: 36rpx;
  color: #ffffff;
  font-weight: bold;
}

/* ==================== 通用区块样式 ==================== */
.section-title {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
  padding: 24rpx 30rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
}

/* ==================== 收货地址样式 ==================== */
.address-section {
  margin-top: 20rpx;
  background-color: #ffffff;
}

.address-info {
  padding: 24rpx 30rpx;
}

.receiver-name {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

/* ==================== 商品信息样式 ==================== */
.product-section {
  margin-top: 20rpx;
  background-color: #ffffff;
}

.product-item {
  display: flex;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background-color: #f8f8f8;
  margin-right: 20rpx;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 32rpx;
  color: #ff6700;
  font-weight: bold;
}

.product-quantity {
  font-size: 26rpx;
  color: #999;
}

/* ==================== 价格明细样式 ==================== */
.price-section {
  margin-top: 20rpx;
  background-color: #ffffff;
}

.price-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 30rpx;
}

.price-label {
  font-size: 28rpx;
  color: #666;
}

.price-value {
  font-size: 28rpx;
  color: #333;
}

.price-item.total {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 24rpx;
}

.price-item.total .price-label {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
}

.price-item.total .price-value {
  font-size: 36rpx;
  color: #ff6700;
  font-weight: bold;
}

/* ==================== 订单信息样式 ==================== */
.info-section {
  margin-top: 20rpx;
  background-color: #ffffff;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #666;
}

.info-value {
  font-size: 28rpx;
  color: #333;
}

/* ==================== 底部操作栏样式 ==================== */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: flex-end;
  padding: 20rpx 30rpx;
  background-color: #ffffff;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn {
  width: 200rpx;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 28rpx;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 36rpx;
  margin-right: 20rpx;
}

.pay-btn {
  width: 240rpx;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 28rpx;
  color: #ffffff;
  background-color: #ff6700;
  border-radius: 36rpx;
}
</style>