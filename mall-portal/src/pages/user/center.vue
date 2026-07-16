<template>
  <!-- 个人中心页面 -->
  <view class="container">
    
    <!-- 👤 用户信息区 -->
    <view class="user-header" @click="handleUserClick">
      <image 
        class="avatar" 
        :src="isLoggedIn ? (userInfo.avatar || '/static/default-avatar.png') : '/static/default-avatar.png'" 
        mode="aspectFill"
      />
      <view class="user-info">
        <text class="username">{{ isLoggedIn ? (userInfo.nickname || '用户') : '点击登录' }}</text>
        <text class="user-desc">{{ isLoggedIn ? (userInfo.desc || '欢迎回来') : '登录后享受更多服务' }}</text>
      </view>
      <text class="arrow-icon">›</text>
    </view>

    <!-- 📦 我的订单 -->
    <view class="order-section">
      <view class="section-header">
        <text class="section-title">我的订单</text>
        <text class="section-more" @click="goToOrderList">全部订单 ›</text>
      </view>
      <view class="order-grid">
        <view class="order-item" @click="goToOrderList(1)">
          <text class="order-icon"></text>
          <text class="order-text">待付款</text>
          <text class="order-badge" v-if="orderCount.unpaid > 0">{{ orderCount.unpaid }}</text>
        </view>
        <view class="order-item" @click="goToOrderList(2)">
          <text class="order-icon"></text>
          <text class="order-text">待发货</text>
          <text class="order-badge" v-if="orderCount.unshipped > 0">{{ orderCount.unshipped }}</text>
        </view>
        <view class="order-item" @click="goToOrderList(3)">
          <text class="order-icon"></text>
          <text class="order-text">待收货</text>
          <text class="order-badge" v-if="orderCount.unreceived > 0">{{ orderCount.unreceived }}</text>
        </view>
        <view class="order-item" @click="goToOrderList(4)">
          <text class="order-icon">⭐</text>
          <text class="order-text">待评价</text>
          <text class="order-badge" v-if="orderCount.unreviewed > 0">{{ orderCount.unreviewed }}</text>
        </view>
      </view>
    </view>

    <!-- 🔧 常用功能 -->
    <view class="menu-section">
      <view class="menu-item" @click="goToAddress">
        <text class="menu-icon">📍</text>
        <text class="menu-text">收货地址</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToFavorite">
        <text class="menu-icon">❤️</text>
        <text class="menu-text">我的收藏</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToCoupon">
        <text class="menu-icon"></text>
        <text class="menu-text">优惠券</text>
        <text class="menu-badge" v-if="couponCount > 0">{{ couponCount }}</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goToService">
        <text class="menu-icon">💬</text>
        <text class="menu-text">在线客服</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- ⚙️ 设置 -->
    <view class="menu-section">
      <view class="menu-item" @click="goToSettings">
        <text class="menu-icon">⚙️</text>
        <text class="menu-text">设置</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 已登录：退出登录按钮（放在底部） -->
    <view v-if="isLoggedIn" class="logout-section">
      <view class="logout-btn" @click="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'

// ==================== 响应式数据 ====================

/** 登录状态 */
const isLoggedIn = ref(false)

/** 用户信息 */
const userInfo = ref({
  avatar: '',
  nickname: '',
  desc: ''
})

/** 订单数量统计 */
const orderCount = ref({
  unpaid: 0,
  unshipped: 0,
  unreceived: 0,
  unreviewed: 0
})

/** 优惠券数量 */
const couponCount = ref(0)

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
    userInfo.value = user
    console.log('用户已登录:', user)
  } else {
    isLoggedIn.value = false
    userInfo.value = {
      avatar: '',
      nickname: '',
      desc: ''
    }
    console.log('用户未登录')
  }
}

/**
 * 点击用户区域：未登录跳转登录页，已登录无操作
 */
function handleUserClick() {
  if (!isLoggedIn.value) {
    uni.navigateTo({
      url: '/pages/login/login'
    })
  }
}

/**
 * 退出登录
 */
function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除本地存储
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        
        // 更新状态
        isLoggedIn.value = false
        userInfo.value = {
          avatar: '',
          nickname: '',
          desc: ''
        }
        
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
        
        console.log('用户已退出登录')
      }
    }
  })
}

/**
 * 跳转到订单列表
 * @param {Number} status - 订单状态（可选）
 */
function goToOrderList(status) {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  if (status !== undefined) {
    uni.navigateTo({
      url: `/pages/order/list?status=${status}`
    })
  } else {
    uni.navigateTo({
      url: '/pages/order/list'
    })
  }
}

/**
 * 跳转到收货地址
 */
function goToAddress() {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  uni.navigateTo({
    url: '/pages/address/list'
  })
}

/**
 * 跳转到我的收藏
 */
function goToFavorite() {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  uni.showToast({ title: '收藏列表开发中', icon: 'none' })
}

/**
 * 跳转到优惠券
 */
function goToCoupon() {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  uni.showToast({ title: '优惠券列表开发中', icon: 'none' })
}

/**
 * 跳转到在线客服
 */
function goToService() {
  uni.showToast({ title: '客服功能开发中', icon: 'none' })
}

/**
 * 跳转到设置
 */
function goToSettings() {
  uni.showToast({ title: '设置页面开发中', icon: 'none' })
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* ==================== 用户信息区样式 ==================== */
.user-header {
  background: linear-gradient(135deg, #ff6700, #ff8533);
  padding: 60rpx 30rpx 40rpx;
  display: flex;
  align-items: center;
  position: relative;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  margin-right: 24rpx;
  background-color: #ffffff;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 36rpx;
  color: #ffffff;
  font-weight: bold;
  display: block;
  margin-bottom: 12rpx;
}

.user-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.arrow-icon {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* ==================== 退出登录区域 ==================== */
.logout-section {
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 20rpx;
}

.logout-btn {
  background-color: #ff4444;
  color: #ffffff;
  text-align: center;
  padding: 24rpx;
  border-radius: 12rpx;
}

.logout-text {
  font-size: 30rpx;
  color: #ffffff;
  font-weight: bold;
}

/* ==================== 订单区域样式 ==================== */
.order-section {
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.section-more {
  font-size: 26rpx;
  color: #999;
}

.order-grid {
  display: flex;
  justify-content: space-around;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.order-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.order-text {
  font-size: 26rpx;
  color: #666;
}

.order-badge {
  position: absolute;
  top: -8rpx;
  right: -16rpx;
  background-color: #ff6700;
  color: #ffffff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

/* ==================== 菜单列表样式 ==================== */
.menu-section {
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.menu-badge {
  background-color: #ff6700;
  color: #ffffff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  margin-right: 16rpx;
}

.menu-arrow {
  font-size: 40rpx;
  color: #ccc;
}
</style>