<template>
  <!-- 商品详情页面 -->
  <view class="container">
    
    <!-- ️ 商品图片轮播 -->
    <swiper class="product-swiper" :indicator-dots="true" :autoplay="false">
      <swiper-item>
        <image 
          class="product-image" 
          :src="product.imageUrl || '/static/placeholder.png'" 
          mode="aspectFill"
        />
      </swiper-item>
    </swiper>

    <!-- 💰 价格信息区 -->
    <view class="price-section">
      <view class="price-row">
        <text class="price-symbol">¥</text>
        <text class="price-value">{{ product.price }}</text>
      </view>
      <view class="price-info">
        <text class="original-price">¥{{ product.originalPrice || product.price * 1.2 }}</text>
        <text class="discount-tag" v-if="product.discount">-{{ product.discount }}%</text>
      </view>
    </view>

    <!-- 📝 商品标题和描述 -->
    <view class="info-section">
      <text class="product-title">{{ product.name }}</text>
      <text class="product-desc">{{ product.description || '暂无描述' }}</text>
      
      <!-- 库存状态 -->
      <view class="stock-info">
        <text class="stock-text">库存 {{ product.stock }}</text>
        <text class="stock-status" :class="{ 'out-of-stock': product.stock === 0 }">
          {{ product.stock > 0 ? '有货' : '缺货' }}
        </text>
      </view>
    </view>

    <!-- 📊 商品规格 -->
    <view class="specs-section">
      <view class="section-title">商品规格</view>
      <view class="spec-item">
        <text class="spec-label">品牌</text>
        <text class="spec-value">{{ product.brand || '官方' }}</text>
      </view>
      <view class="spec-item">
        <text class="spec-label">分类</text>
        <text class="spec-value">{{ getCategoryName(product.categoryId) }}</text>
      </view>
    </view>

    <!-- 📖 商品详情 -->
    <view class="detail-section">
      <view class="section-title">商品详情</view>
      <view class="detail-content">
        <text class="detail-text">{{ product.detail || '商品详情待补充...' }}</text>
      </view>
    </view>

    <!-- 🛒 底部操作栏 -->
    <view class="action-bar">
      <view class="action-left">
        <view class="action-btn" @click="goToCart">
          <text class="btn-icon">🛒</text>
          <text class="btn-text">购物车</text>
        </view>
        <view class="action-btn" @click="goToHome">
          <text class="btn-icon"></text>
          <text class="btn-text">首页</text>
        </view>
      </view>
      <view class="action-right">
        <button class="add-cart-btn" @click="addToCart">加入购物车</button>
        <button class="buy-now-btn" @click="buyNow">立即购买</button>
      </view>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, onMounted } from 'vue'
import { getProductDetail, addCart as addToCartApi } from '../../../api/index'

// ==================== 响应式数据 ====================

/** 商品详情数据 */
const product = ref({
  id: 0,
  name: '',
  description: '',
  price: 0,
  originalPrice: 0,
  discount: 0,
  stock: 0,
  imageUrl: '',
  categoryId: 0,
  brand: '',
  detail: ''
})

/** 分类映射 */
const categoryMap = {
  1: '手机数码',
  2: '电脑办公',
  3: '家用电器',
  4: '服装鞋包'
}

// ==================== 页面生命周期 ====================

/** 页面加载时执行 */
onMounted(() => {
  loadProductDetail()
})

// ==================== 方法定义 ====================

/**
 * 加载商品详情
 */
async function loadProductDetail() {
  
  // 获取页面参数中的商品ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const productId = currentPage.options.id || 1
  
  console.log('加载商品详情，ID:', productId)
  
  try {
    // 调用后端 API
    const data = await getProductDetail(productId)
    product.value = data
    
    console.log('商品详情加载成功:', product.value)
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

/**
 * 获取分类名称
 * @param {Number} categoryId - 分类ID
 */
function getCategoryName(categoryId) {
  return categoryMap[categoryId] || '其他'
}

/**
 * 加入购物车
 */
async function addToCart() {
  // 检查登录状态
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  if (product.value.stock === 0) {
    uni.showToast({ title: '商品缺货', icon: 'none' })
    return
  }
  
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id
    
    if (!userId) {
      uni.showToast({ title: '用户信息异常', icon: 'none' })
      return
    }
    
    await addToCartApi({
      userId: userId,
      productId: product.value.id,
      productName: product.value.name,
      imageUrl: product.value.imageUrl,
      price: product.value.price,
      quantity: 1
    })
    
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (error) {
    console.error('加入购物车失败:', error)
    uni.showToast({ title: '加入失败', icon: 'none' })
  }
}

/**
 * 立即购买
 */
function buyNow() {
  // 检查登录状态
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  if (product.value.stock === 0) {
    uni.showToast({ title: '商品缺货', icon: 'none' })
    return
  }
  
  // 跳转到订单确认页
  uni.navigateTo({
    url: '/pages/order/confirm'
  })
}

/**
 * 跳转到购物车
 */
function goToCart() {
  uni.switchTab({
    url: '/pages/cart/cart'
  })
}

/**
 * 跳转到首页
 */
function goToHome() {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;  /* 留出底部操作栏空间 */
}

/* ==================== 商品图片轮播 ==================== */
.product-swiper {
  width: 100%;
  height: 750rpx;
  background-color: #ffffff;
}

.product-image {
  width: 100%;
  height: 100%;
}

/* ==================== 价格信息区 ==================== */
.price-section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 16rpx;
}

.price-symbol {
  font-size: 36rpx;
  color: #ff6700;
  font-weight: bold;
}

.price-value {
  font-size: 64rpx;
  color: #ff6700;
  font-weight: bold;
  margin-left: 4rpx;
}

.price-info {
  display: flex;
  align-items: center;
}

.original-price {
  font-size: 28rpx;
  color: #999;
  text-decoration: line-through;
}

.discount-tag {
  font-size: 24rpx;
  color: #ff6700;
  background-color: #fff3e0;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  margin-left: 16rpx;
}

/* ==================== 商品信息区 ==================== */
.info-section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.product-title {
  font-size: 36rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 16rpx;
  line-height: 1.4;
}

.product-desc {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 20rpx;
  line-height: 1.5;
}

.stock-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stock-text {
  font-size: 26rpx;
  color: #666;
}

.stock-status {
  font-size: 26rpx;
  color: #4caf50;
  font-weight: bold;
}

.stock-status.out-of-stock {
  color: #f44336;
}

/* ==================== 商品规格区 ==================== */
.specs-section {
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

.spec-item {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.spec-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #999;
}

.spec-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

/* ==================== 商品详情区 ==================== */
.detail-section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.detail-content {
  padding: 20rpx 0;
}

.detail-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
}

/* ==================== 底部操作栏 ==================== */
.action-bar {
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

.action-left {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.btn-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
}

.btn-text {
  font-size: 22rpx;
  color: #666;
}

.action-right {
  display: flex;
  gap: 16rpx;
  flex: 1;
  margin-left: 24rpx;
}

.add-cart-btn {
  flex: 1;
  height: 80rpx;
  background-color: #ffa726;
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.buy-now-btn {
  flex: 1;
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
</style>