<template>
  <!-- 收货地址列表页 -->
  <view class="container">
    
    <!-- 选择模式提示 -->
    <view class="select-mode-tip" v-if="selectMode">
      <text class="tip-text">请选择收货地址</text>
    </view>
    
    <!-- 地址列表 -->
    <view class="address-list">
      <view 
        class="address-item" 
        :class="{ 'select-mode': selectMode }"
        v-for="item in addressList" 
        :key="item.id"
        @click="selectAddress(item)"
      >
        <!-- 默认标签 -->
        <view class="default-tag" v-if="item.isDefault === 1">默认</view>
        
        <!-- 选择模式下的选中图标 -->
        <view class="select-icon" v-if="selectMode">
          <text class="icon-arrow">›</text>
        </view>
        
        <!-- 地址信息 -->
        <view class="address-info">
          <view class="user-row">
            <text class="name">{{ item.name }}</text>
            <text class="phone">{{ item.phone }}</text>
          </view>
          <view class="address-row">
            <text class="address">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detail }}</text>
          </view>
        </view>
        
        <!-- 操作按钮（非选择模式下显示） -->
        <view class="action-btns" v-if="!selectMode">
          <view class="btn-edit" @click.stop="goToEdit(item)">编辑</view>
          <view class="btn-delete" @click.stop="deleteAddress(item)">删除</view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="addressList.length === 0 && !loading">
      <text class="empty-icon">📍</text>
      <text class="empty-text">暂无收货地址</text>
    </view>

    <!-- 底部添加按钮（非选择模式下显示） -->
    <view class="add-btn" v-if="!selectMode" @click="goToAdd">
      <text class="add-icon">+</text>
      <text class="add-text">新增收货地址</text>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAddressList, deleteAddress as deleteAddressApi } from '../../api/index'

// ==================== 响应式数据 ====================

/** 地址列表 */
const addressList = ref([])

/** 是否正在加载 */
const loading = ref(false)

/** 是否为选择模式（从订单确认页跳转） */
const selectMode = ref(false)

/** 登录状态 */
const isLoggedIn = ref(false)

// ==================== 页面生命周期 ====================

/** 页面加载时执行 */
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  
  if (options.selectMode === 'true') {
    selectMode.value = true
  }
  
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
    loadAddressList()
  } else {
    isLoggedIn.value = false
    addressList.value = []
    
    if (!selectMode.value) {
      uni.showModal({
        title: '提示',
        content: '请先登录后查看地址',
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
}

// ==================== 方法定义 ====================

/**
 * 加载地址列表
 */
async function loadAddressList() {
  loading.value = true
  
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    const data = await getAddressList({ userId })
    addressList.value = data || []
    
    console.log('地址列表加载成功:', addressList.value.length, '个地址')
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 选择地址（用于订单确认页选择）
 * @param {Object} address - 地址对象
 */
function selectAddress(address) {
  // 如果是选择模式，返回订单确认页并传递地址
  if (selectMode.value) {
    // 将选中的地址存储到缓存
    uni.setStorageSync('selectedAddress', JSON.stringify(address))
    // 返回上一页
    uni.navigateBack()
    return
  }
  
  // 非选择模式，不做处理（仅用于查看）
}

/**
 * 跳转到添加地址页
 */
function goToAdd() {
  uni.navigateTo({
    url: '/pages/address/edit'
  })
}

/**
 * 跳转到编辑地址页
 * @param {Object} address - 地址对象
 */
function goToEdit(address) {
  uni.navigateTo({
    url: `/pages/address/edit?id=${address.id}`
  })
}

/**
 * 删除地址
 * @param {Object} address - 地址对象
 */
async function deleteAddress(address) {
  uni.showModal({
    title: '提示',
    content: '确定要删除该地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteAddressApi(address.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadAddressList()  // 重新加载列表
        } catch (error) {
          console.error('删除失败:', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
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
  padding-bottom: 120rpx;
}

/* ==================== 地址列表样式 ==================== */
.address-list {
  padding: 20rpx;
}

.address-item {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  position: relative;
}

.default-tag {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background-color: #ff6700;
  color: #ffffff;
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}

.address-info {
  margin-bottom: 20rpx;
}

.user-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.name {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
  margin-right: 30rpx;
}

.phone {
  font-size: 28rpx;
  color: #666;
}

.address-row {
  display: flex;
  align-items: flex-start;
}

.address {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.action-btns {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.btn-edit {
  font-size: 26rpx;
  color: #ff6700;
  padding: 8rpx 24rpx;
  border: 1rpx solid #ff6700;
  border-radius: 8rpx;
}

.btn-delete {
  font-size: 26rpx;
  color: #999;
  padding: 8rpx 24rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
}

/* ==================== 空状态样式 ==================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* ==================== 底部添加按钮样式 ==================== */
.add-btn {
  position: fixed;
  bottom: 40rpx;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #ff6700, #ff8533);
  color: #ffffff;
  padding: 24rpx 60rpx;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 20rpx rgba(255, 103, 0, 0.3);
}

.add-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
  font-weight: bold;
}

.add-text {
  font-size: 30rpx;
  font-weight: bold;
}

/* ==================== 选择模式样式 ==================== */
.select-mode-tip {
  background-color: #fff3e0;
  padding: 20rpx 30rpx;
  text-align: center;
}

.tip-text {
  font-size: 28rpx;
  color: #ff6700;
}

.address-item.select-mode {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.select-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60rpx;
  height: 60rpx;
}

.icon-arrow {
  font-size: 48rpx;
  color: #ccc;
}
</style>