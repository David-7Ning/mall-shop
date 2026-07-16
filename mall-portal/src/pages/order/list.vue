<template>
  <!-- 订单列表页面 -->
  <view class="container">
    
    <!-- 📑 订单状态筛选标签 -->
    <view class="status-tabs">
      <view 
        class="tab-item" 
        :class="{ active: selectedStatus === -1 }"
        @click="selectStatus(-1)"
      >
        <text>全部</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: selectedStatus === 0 }"
        @click="selectStatus(0)"
      >
        <text>待付款</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: selectedStatus === 1 }"
        @click="selectStatus(1)"
      >
        <text>待发货</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: selectedStatus === 2 }"
        @click="selectStatus(2)"
      >
        <text>待收货</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: selectedStatus === 3 }"
        @click="selectStatus(3)"
      >
        <text>已完成</text>
      </view>
    </view>

    <!-- 📦 订单列表 -->
    <view class="order-list">
      <view 
        class="order-card" 
        v-for="order in orderList" 
        :key="order.id"
        @click="goToDetail(order.id)"
      >
        <!-- 订单头部 -->
        <view class="order-header">
          <text class="order-no">订单号：{{ order.orderNo }}</text>
          <text class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </text>
        </view>

        <!-- 商品信息 -->
        <view class="order-products">
          <view class="product-item" v-for="item in order.items" :key="item.id">
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

        <!-- 订单底部 -->
        <view class="order-footer">
          <text class="order-total">共 {{ getTotalQuantity(order.items) }} 件商品，合计：</text>
          <text class="order-amount">¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="order-actions">
          <button 
            class="action-btn cancel-btn" 
            v-if="order.status === 0"
            @click.stop="cancelOrder(order)"
          >
            取消订单
          </button>
          <button 
            class="action-btn pay-btn" 
            v-if="order.status === 0"
            @click.stop="payOrder(order)"
          >
            立即支付
          </button>
          <button 
            class="action-btn confirm-btn" 
            v-if="order.status === 2"
            @click.stop="confirmReceive(order)"
          >
            确认收货
          </button>
        </view>
      </view>
    </view>

    <!-- 空状态提示 -->
    <view class="empty-state" v-if="orderList.length === 0 && !loading">
      <text class="empty-icon">📦</text>
      <text class="empty-text">暂无订单</text>
    </view>

    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <text>加载中...</text>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList, cancelOrder as cancelOrderApi, payOrder as payOrderApi } from '../../api/index'

// ==================== 响应式数据 ====================

/** 选中的订单状态（-1=全部） */
const selectedStatus = ref(-1)

/** 是否正在加载 */
const loading = ref(false)

/** 订单列表数据 */
const orderList = ref([])

/** 登录状态 */
const isLoggedIn = ref(false)

// ==================== 页面生命周期 ====================

/** 页面加载时执行 */
onMounted(() => {
  checkLoginStatus()
})

/** 页面显示时执行（从其他页面返回时更新状态） */
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
    loadOrderList()
  } else {
    isLoggedIn.value = false
    orderList.value = []
    
    uni.showModal({
      title: '提示',
      content: '请先登录后查看订单',
      showCancel: true,
      cancelText: '去逛逛',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/login' })
        } else {
          uni.navigateBack()
        }
      }
    })
  }
}

// ==================== 方法定义 ====================

/**
 * 加载订单列表
 */
async function loadOrderList() {
  loading.value = true
  
  try {
    // 获取当前登录用户ID
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 构建请求参数
    const params = { userId }
    if (selectedStatus.value >= 0) {
      params.status = selectedStatus.value
    }
    
    // 调用后端 API
    const data = await getOrderList(params)
    orderList.value = data || []
    
    console.log('订单列表加载成功:', orderList.value.length, '个订单')
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 选择订单状态
 * @param {Number} status - 订单状态
 */
function selectStatus(status) {
  selectedStatus.value = status
  loadOrderList()  // 重新加载订单列表
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
 * 获取订单状态样式类
 * @param {Number} status - 订单状态
 */
function getStatusClass(status) {
  const classMap = {
    0: 'status-wait-pay',
    1: 'status-paid',
    2: 'status-shipped',
    3: 'status-completed',
    4: 'status-cancelled'
  }
  return classMap[status] || ''
}

/**
 * 获取订单商品总数量
 * @param {Array} items - 订单商品列表
 */
function getTotalQuantity(items) {
  if (!items || items.length === 0) return 0
  return items.reduce((sum, item) => sum + item.quantity, 0)
}

/**
 * 跳转到订单详情页
 * @param {Number} orderId - 订单ID
 */
function goToDetail(orderId) {
  uni.navigateTo({
    url: `/pages/order/detail?id=${orderId}`
  })
}

/**
 * 取消订单
 * @param {Object} order - 订单对象
 */
async function cancelOrder(order) {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '取消中...' })
          // 调用取消订单 API
          await cancelOrderApi(order.id)
          uni.hideLoading()
          uni.showToast({ title: '订单已取消', icon: 'success' })
          loadOrderList()  // 重新加载订单列表
        } catch (error) {
          uni.hideLoading()
          console.error('取消订单失败:', error)
          uni.showToast({ title: error.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 支付订单
 * @param {Object} order - 订单对象
 */
async function payOrder(order) {
  uni.showModal({
    title: '确认支付',
    content: `确认支付 ¥${order.totalAmount?.toFixed(2) || '0.00'} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '支付中...' })
          // 调用支付 API
          await payOrderApi(order.id)
          uni.hideLoading()
          uni.showToast({ title: '支付成功', icon: 'success' })
          loadOrderList()  // 重新加载订单列表
        } catch (error) {
          uni.hideLoading()
          console.error('支付失败:', error)
          uni.showToast({ title: error.message || '支付失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 确认收货
 * @param {Object} order - 订单对象
 */
function confirmReceive(order) {
  uni.showModal({
    title: '提示',
    content: '确认已收到商品吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用确认收货 API
          uni.showToast({ title: '确认收货成功', icon: 'success' })
          loadOrderList()  // 重新加载订单列表
        } catch (error) {
          console.error('确认收货失败:', error)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* ==================== 状态筛选标签样式 ==================== */
.status-tabs {
  background-color: #ffffff;
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eeeeee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12rpx 0;
}

.tab-item text {
  font-size: 28rpx;
  color: #666;
}

.tab-item.active text {
  color: #ff6700;
  font-weight: bold;
}

.tab-item.active {
  position: relative;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background-color: #ff6700;
  border-radius: 2rpx;
}

/* ==================== 订单列表样式 ==================== */
.order-list {
  padding: 20rpx;
}

.order-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-no {
  font-size: 26rpx;
  color: #999;
}

.order-status {
  font-size: 28rpx;
  font-weight: bold;
}

.status-wait-pay {
  color: #ff6700;
}

.status-paid {
  color: #1890ff;
}

.status-shipped {
  color: #52c41a;
}

.status-completed {
  color: #999;
}

.status-cancelled {
  color: #ff4d4f;
}

/* 商品信息 */
.order-products {
  padding: 20rpx 30rpx;
}

.product-item {
  display: flex;
  padding: 16rpx 0;
}

.product-image {
  width: 120rpx;
  height: 120rpx;
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
  font-size: 30rpx;
  color: #ff6700;
  font-weight: bold;
}

.product-quantity {
  font-size: 26rpx;
  color: #999;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-total {
  font-size: 26rpx;
  color: #666;
}

.order-amount {
  font-size: 32rpx;
  color: #ff6700;
  font-weight: bold;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  padding: 12rpx 32rpx;
  font-size: 26rpx;
  border-radius: 30rpx;
  margin-left: 20rpx;
}

.cancel-btn {
  color: #666;
  background-color: #f5f5f5;
}

.pay-btn {
  color: #ffffff;
  background-color: #ff6700;
}

.confirm-btn {
  color: #ffffff;
  background-color: #52c41a;
}

/* ==================== 空状态样式 ==================== */
.empty-state {
  padding: 120rpx 0;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  display: block;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
}

/* ==================== 加载状态样式 ==================== */
.loading-state {
  padding: 60rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}
</style>