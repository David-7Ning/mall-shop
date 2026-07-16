<template>
  <!-- 购物车页面 -->
  <view class="container">
    
    <!-- 空购物车提示 -->
    <view class="empty-cart" v-if="cartList.length === 0">
      <text class="empty-icon">🛒</text>
      <text class="empty-text">购物车是空的</text>
      <button class="go-shopping-btn" @click="goToProductList">去逛逛</button>
    </view>

    <!-- 购物车列表 -->
    <view class="cart-list" v-else>
      <view 
        class="cart-item" 
        v-for="item in cartList" 
        :key="item.id"
      >
        <!-- 复选框 -->
        <view class="checkbox" @click="toggleSelect(item)">
          <text class="checkbox-icon" :class="{ checked: item.selected }">✓</text>
        </view>

        <!-- 商品图片 -->
        <image 
          class="product-image" 
          :src="item.imageUrl || '/static/placeholder.png'" 
          mode="aspectFill"
          @click="goToDetail(item.productId)"
        />

        <!-- 商品信息 -->
        <view class="product-info">
          <text class="product-name">{{ item.productName }}</text>
          <text class="product-spec">{{ item.spec || '默认规格' }}</text>
          
          <view class="product-bottom">
            <!-- 价格 -->
            <text class="product-price">¥{{ item.price }}</text>
            
            <!-- 数量控制器 -->
            <view class="quantity-control">
              <view class="control-btn" @click="decreaseQuantity(item)">-</view>
              <text class="quantity-text">{{ item.quantity }}</text>
              <view class="control-btn" @click="increaseQuantity(item)">+</view>
            </view>
          </view>
        </view>

        <!-- 删除按钮 -->
        <view class="delete-btn" @click="removeItem(item)">
          <text class="delete-icon">🗑️</text>
        </view>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view class="checkout-bar" v-if="cartList.length > 0">
      <view class="checkout-left">
        <view class="select-all" @click="toggleSelectAll">
          <text class="checkbox-icon" :class="{ checked: isAllSelected }">✓</text>
          <text class="select-text">全选</text>
        </view>
        <view class="total-price">
          <text class="total-label">合计：</text>
          <text class="total-value">¥{{ totalPrice.toFixed(2) }}</text>
        </view>
      </view>
      <button class="checkout-btn" @click="checkout">
        结算({{ selectedCount }})
      </button>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCartList, updateCart, deleteCart } from '../../api/index'

// ==================== 响应式数据 ====================

/** 登录状态 */
const isLoggedIn = ref(false)

/** 购物车商品列表 */
const cartList = ref([])

// ==================== 计算属性 ====================

/** 是否全选 */
const isAllSelected = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(item => item.selected)
})

/** 选中商品数量 */
const selectedCount = computed(() => {
  return cartList.value.filter(item => item.selected).length
})

/** 选中商品总价 */
const totalPrice = computed(() => {
  return cartList.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// ==================== 页面生命周期 ====================

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
    loadCartData()
  } else {
    isLoggedIn.value = false
    cartList.value = []
    
    uni.showModal({
      title: '提示',
      content: '请先登录后查看购物车',
      showCancel: true,
      cancelText: '去逛逛',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/login' })
        } else {
          uni.switchTab({ url: '/pages/index/index' })
        }
      }
    })
  }
}

// ==================== 方法定义 ====================

/**
 * 加载购物车数据
 */
async function loadCartData() {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id
    
    if (!userId) {
      uni.showToast({ title: '用户信息异常', icon: 'none' })
      return
    }
    
    const data = await getCartList({ userId })
    cartList.value = (data || []).map(item => ({
      ...item,
      selected: item.checked || false  // 使用 checked 字段作为选中状态
    }))
    
    console.log('购物车数据加载成功:', cartList.value.length, '件商品')
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

/**
 * 切换商品选中状态
 * @param {Object} item - 购物车商品项
 */
function toggleSelect(item) {
  item.selected = !item.selected
}

/**
 * 全选/取消全选
 */
function toggleSelectAll() {
  const newState = !isAllSelected.value
  cartList.value.forEach(item => {
    item.selected = newState
  })
}

/**
 * 增加商品数量
 * @param {Object} item - 购物车商品项
 */
async function increaseQuantity(item) {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 调用后端 API 更新数量（RESTful：PUT /cart/items/{productId}）
    await updateCart(item.productId, {
      userId,
      quantity: item.quantity + 1
    })
    
    // 更新本地数据
    item.quantity++
    console.log('增加数量:', item.productName, item.quantity)
  } catch (error) {
    console.error('更新失败:', error)
    uni.showToast({ title: '更新失败', icon: 'none' })
  }
}

/**
 * 减少商品数量
 * @param {Object} item - 购物车商品项
 */
async function decreaseQuantity(item) {
  if (item.quantity > 1) {
    try {
      const userInfo = uni.getStorageSync('userInfo')
      const userId = userInfo?.id || 1
      
      // 调用后端 API 更新数量（RESTful：PUT /cart/items/{productId}）
      await updateCart(item.productId, {
        userId,
        quantity: item.quantity - 1
      })
      
      // 更新本地数据
      item.quantity--
      console.log('减少数量:', item.productName, item.quantity)
    } catch (error) {
      console.error('更新失败:', error)
      uni.showToast({ title: '更新失败', icon: 'none' })
    }
  } else {
    uni.showModal({
      title: '提示',
      content: '确定要删除该商品吗？',
      success: async (res) => {
        if (res.confirm) {
          await removeItem(item)
        }
      }
    })
  }
}

/**
 * 删除商品
 * @param {Object} item - 购物车商品项
 */
async function removeItem(item) {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    // 调用后端 API 删除商品（RESTful：DELETE /cart/items/{productId}）
    await deleteCart(item.productId, {
      userId
    })
    
    // 更新本地数据
    const index = cartList.value.findIndex(i => i.productId === item.productId)
    if (index > -1) {
      cartList.value.splice(index, 1)
    }
    
    uni.showToast({ title: '已删除', icon: 'success' })
    console.log('删除商品:', item.productName)
  } catch (error) {
    console.error('删除失败:', error)
    uni.showToast({ title: '删除失败', icon: 'none' })
  }
}

/**
 * 结算
 */
function checkout() {
  if (selectedCount.value === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  
  // 获取选中的商品
  const selectedItems = cartList.value.filter(item => item.selected)
  
  // 将选中的商品数据编码后传递到订单确认页
  const cartData = encodeURIComponent(JSON.stringify(selectedItems))
  
  // 跳转到订单确认页
  uni.navigateTo({
    url: `/pages/order/confirm?cartData=${cartData}`
  })
}

/**
 * 跳转到商品详情页
 * @param {Number} productId - 商品ID
 */
function goToDetail(productId) {
  uni.navigateTo({
    url: `/pages/product/detail/detail?id=${productId}`
  })
}

/**
 * 跳转到商品列表页
 */
function goToProductList() {
  uni.switchTab({
    url: '/pages/product/list/list'
  })
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 200rpx;  /* 留出底部结算栏和 TabBar 的空间 */
}

/* ==================== 空购物车样式 ==================== */
.empty-cart {
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
  display: block;
  margin-bottom: 40rpx;
}

.go-shopping-btn {
  width: 300rpx;
  height: 80rpx;
  background-color: #ff6700;
  color: #ffffff;
  font-size: 28rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border: none;
}

/* ==================== 购物车列表样式 ==================== */
.cart-list {
  padding: 20rpx;
}

.cart-item {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  position: relative;
}

.checkbox {
  margin-right: 20rpx;
}

.checkbox-icon {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: transparent;
}

.checkbox-icon.checked {
  background-color: #ff6700;
  border-color: #ff6700;
  color: #ffffff;
}

.product-image {
  width: 160rpx;
  height: 160rpx;
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

.quantity-control {
  display: flex;
  align-items: center;
  border: 1rpx solid #eee;
  border-radius: 8rpx;
}

.control-btn {
  width: 50rpx;
  height: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #666;
}

.quantity-text {
  width: 60rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
}

.delete-btn {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

.delete-icon {
  font-size: 36rpx;
}

/* ==================== 底部结算栏样式 ==================== */
.checkout-bar {
  position: fixed;
  bottom: 100rpx;  /* 留出 TabBar 的高度 */
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.08);
  z-index: 999;  /* 确保在 TabBar 之上 */
}

.checkout-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.select-all {
  display: flex;
  align-items: center;
  margin-right: 30rpx;
}

.select-text {
  font-size: 26rpx;
  color: #666;
  margin-left: 8rpx;
}

.total-price {
  display: flex;
  align-items: baseline;
}

.total-label {
  font-size: 26rpx;
  color: #666;
}

.total-value {
  font-size: 36rpx;
  color: #ff6700;
  font-weight: bold;
}

.checkout-btn {
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
</style>