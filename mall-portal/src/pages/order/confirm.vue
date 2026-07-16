<template>
  <!-- 订单确认页面 -->
  <view class="container">
    
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view class="error-state" v-else-if="error">
      <text class="error-icon">⚠️</text>
      <text class="error-text">{{ error }}</text>
      <button class="retry-btn" @click="loadOrderData">点击重试</button>
    </view>

    <!-- 正常内容 -->
    <template v-else>
    
    <!-- 📍 收货地址 -->
    <view class="address-section" @click="selectAddress">
      <view class="address-info" v-if="address">
        <view class="address-header">
          <text class="receiver-name">{{ address.name }}</text>
          <text class="receiver-phone">{{ address.phone }}</text>
        </view>
        <text class="address-detail">{{ address.detail }}</text>
      </view>
      <view class="no-address" v-else>
        <text class="no-address-icon">📍</text>
        <text class="no-address-text">请选择收货地址</text>
      </view>
      <text class="arrow-icon">›</text>
    </view>

    <!--  商品信息 -->
    <view class="product-section">
      <view class="section-title">商品信息</view>
      <view 
        class="product-item" 
        v-for="item in orderItems" 
        :key="item.id"
      >
        <image 
          class="product-image" 
          :src="item.imageUrl || '/static/placeholder.png'" 
          mode="aspectFill"
        />
        <view class="product-info">
          <text class="product-name">{{ item.name }}</text>
          <text class="product-spec">{{ item.spec || '默认规格' }}</text>
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
      <view class="price-item">
        <text class="price-label">运费</text>
        <text class="price-value">¥{{ freight.toFixed(2) }}</text>
      </view>
      <view class="price-item">
        <text class="price-label">优惠</text>
        <text class="price-value discount">-¥{{ discount.toFixed(2) }}</text>
      </view>
      <view class="price-item total">
        <text class="price-label">实付金额</text>
        <text class="price-value">¥{{ actualTotal.toFixed(2) }}</text>
      </view>
    </view>

    <!-- 📝 订单备注 -->
    <view class="remark-section">
      <view class="section-title">订单备注</view>
      <input 
        class="remark-input" 
        type="text" 
        placeholder="选填，可以告诉商家您的特殊需求"
        v-model="remark"
        maxlength="100"
      />
    </view>

    <!-- 底部提交栏 -->
    <view class="submit-bar">
      <view class="submit-left">
        <text class="submit-label">实付：</text>
        <text class="submit-price">¥{{ actualTotal.toFixed(2) }}</text>
      </view>
      <button class="submit-btn" @click="submitOrder">提交订单</button>
    </view>

    </template>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { createOrder, getCartList } from '../../api/index'

// ==================== 响应式数据 ====================

/** 收货地址 */
const address = ref(null)

/** 订单商品列表 */
const orderItems = ref([])

/** 订单备注 */
const remark = ref('')

/** 是否正在加载 */
const loading = ref(false)

/** 错误信息 */
const error = ref('')

/** 登录状态 */
const isLoggedIn = ref(false)

// ==================== 计算属性 ====================

/** 商品总价 */
const goodsTotal = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

/** 运费 */
const freight = computed(() => {
  return goodsTotal.value >= 99 ? 0 : 10
})

/** 优惠金额 */
const discount = computed(() => {
  return goodsTotal.value >= 500 ? 50 : 0
})

/** 实付金额 */
const actualTotal = computed(() => {
  return goodsTotal.value + freight.value - discount.value
})

// ==================== 页面生命周期 ====================

/** 页面加载时执行 */
onMounted(() => {
  checkLoginStatus()
})

/** 页面参数加载时执行（从地址选择页返回时触发） */
onLoad(() => {
  const selectedAddress = uni.getStorageSync('selectedAddress')
  if (selectedAddress) {
    address.value = JSON.parse(selectedAddress)
    uni.removeStorageSync('selectedAddress')
    console.log('已选择地址:', address.value)
  }
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
    loadOrderData()
  } else {
    isLoggedIn.value = false
    
    uni.showModal({
      title: '提示',
      content: '请先登录后再下单',
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
 * 加载订单数据
 */
async function loadOrderData() {
  loading.value = true
  error.value = ''
  
  try {
    // 获取当前登录用户ID
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 调用购物车 API 获取已选中的商品
    const cartList = await getCartList({ userId })
    
    // 过滤出已选中的商品
    const checkedItems = (cartList || []).filter(item => item.checked)
    
    if (checkedItems.length === 0) {
      error.value = '请选择要结算的商品'
      setTimeout(() => {
        uni.navigateBack()
      }, 2000)
      return
    }
    
    // 转换为订单商品格式
    orderItems.value = checkedItems.map(item => ({
      id: item.id,
      productId: item.productId,
      name: item.productName,
      price: item.price,
      quantity: item.quantity,
      imageUrl: item.imageUrl || item.productImage || '',
      spec: item.spec || '默认规格'
    }))
    
    console.log('订单数据加载成功:', orderItems.value.length, '件商品')
  } catch (err) {
    console.error('加载订单数据失败:', err)
    error.value = '加载失败，请重试'
  } finally {
    loading.value = false
  }
}

/**
 * 选择收货地址
 */
function selectAddress() {
  // 跳转到地址列表页
  uni.navigateTo({
    url: '/pages/address/list?selectMode=true'
  })
}

/**
 * 提交订单
 */
async function submitOrder() {
  if (!address.value) {
    uni.showToast({ title: '请选择收货地址', icon: 'none' })
    return
  }
  
  if (orderItems.value.length === 0) {
    uni.showToast({ title: '订单商品不能为空', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '提交中...' })
  
  try {
    // 获取当前登录用户ID
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 构建订单数据
    const orderData = {
      userId: userId,
      address: `${address.value.name} ${address.value.phone} ${address.value.detail}`,
      items: orderItems.value.map(item => ({
        productId: item.productId || item.id,
        productName: item.name,
        price: item.price,
        quantity: item.quantity
      })),
      remark: remark.value
    }
    
    console.log('提交订单数据:', orderData)
    
    // 调用后端 API 创建订单
    const orderNo = await createOrder(orderData)
    
    uni.hideLoading()
    uni.showToast({ title: '订单提交成功', icon: 'success' })
    
    // 跳转到订单详情页（需要订单ID，这里先用订单号代替）
    setTimeout(() => {
      // TODO: 后端返回订单ID后，改为传递 orderId
      // uni.redirectTo({ url: `/pages/order/detail?id=${orderId}` })
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
    
  } catch (error) {
    uni.hideLoading()
    console.error('提交订单失败:', error)
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

/* ==================== 收货地址样式 ==================== */
.address-section {
  background-color: #ff6700;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.receiver-name {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: bold;
  margin-right: 20rpx;
}

.receiver-phone {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.address-detail {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  display: block;
}

.no-address {
  display: flex;
  align-items: center;
}

.no-address-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.no-address-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.arrow-icon {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* ==================== 商品信息样式 ==================== */
.product-section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #eeeeee;
}

.product-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  background-color: #f8f8f8;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 16rpx;
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
  font-size: 28rpx;
  color: #666;
}

/* ==================== 价格明细样式 ==================== */
.price-section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.price-label {
  font-size: 28rpx;
  color: #666;
}

.price-value {
  font-size: 28rpx;
  color: #333;
}

.price-value.discount {
  color: #ff6700;
}

.price-item.total {
  margin-top: 16rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #eeeeee;
}

.price-item.total .price-label {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.price-item.total .price-value {
  font-size: 36rpx;
  color: #ff6700;
  font-weight: bold;
}

/* ==================== 订单备注样式 ==================== */
.remark-section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.remark-input {
  width: 100%;
  height: 80rpx;
  font-size: 28rpx;
  color: #333;
}

/* ==================== 底部提交栏样式 ==================== */
.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.submit-left {
  display: flex;
  align-items: baseline;
}

.submit-label {
  font-size: 28rpx;
  color: #333;
}

.submit-price {
  font-size: 40rpx;
  color: #ff6700;
  font-weight: bold;
}

.submit-btn {
  width: 240rpx;
  height: 80rpx;
  background-color: #ff6700;
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

/* ==================== 加载状态样式 ==================== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

/* ==================== 错误状态样式 ==================== */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.error-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.error-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.retry-btn {
  width: 200rpx;
  height: 70rpx;
  background-color: #ff6700;
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 35rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}
</style>