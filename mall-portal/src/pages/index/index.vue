<template>
  <!-- 商城首页 -->
  <view class="container">
    
    <!-- 🔝 顶部搜索栏 -->
    <view class="search-bar">
      <view class="search-box">
        <text class="search-icon"></text>
        <input 
          type="text" 
          placeholder="搜索商品" 
          class="search-input"
          v-model="keyword"
          @confirm="onSearch"
        />
      </view>
    </view>

    <!--  轮播图区域 -->
    <swiper 
      class="banner-swiper" 
      :indicator-dots="true" 
      :autoplay="true" 
      :interval="3000" 
      :duration="500"
    >
      <swiper-item v-for="(item, index) in banners" :key="index">
        <image 
          class="banner-image" 
          :src="item.image" 
          mode="aspectFill"
          @click="onBannerClick(item)"
        />
      </swiper-item>
    </swiper>

    <!-- 📦 快捷分类入口 -->
    <view class="category-grid">
      <view 
        class="category-item" 
        v-for="(cat, index) in categories" 
        :key="index"
        @click="goToCategory(cat.id)"
      >
        <image class="category-icon" :src="cat.icon" mode="aspectFit" />
        <text class="category-name">{{ cat.name }}</text>
      </view>
    </view>

    <!-- 🔥 推荐商品列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">🔥 热门推荐</text>
        <text class="section-more" @click="goToProductList">查看更多 ></text>
      </view>
      
      <view class="product-list">
        <view 
          class="product-card" 
          v-for="(product, index) in products" 
          :key="index"
          @click="goToDetail(product.id)"
        >
          <!-- 商品图片 -->
          <image 
            class="product-image" 
            :src="product.imageUrl || '/static/placeholder.png'" 
            mode="aspectFill"
          />
          
          <!-- 商品信息 -->
          <view class="product-info">
            <text class="product-name">{{ product.name }}</text>
            <view class="product-bottom">
              <text class="product-price">¥{{ product.price }}</text>
              <view class="add-cart-btn" @click.stop="addCart(product)">
                <text>+</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API：使用 setup 语法糖
import { ref, onMounted } from 'vue'
import { getProductList } from '../../api/index'

// ==================== 响应式数据定义 ====================

/** 搜索关键词 */
const keyword = ref('')

/** 加载状态 */
const loading = ref(false)

/** 轮播图数据（暂时用模拟数据） */
const banners = ref([
  { id: 1, image: '/static/banner1.png', link: '' },
  { id: 2, image: '/static/banner2.png', link: '' },
  { id: 3, image: '/static/banner3.png', link: '' }
])

/** 分类数据 */
const categories = ref([
  { id: 1, name: '手机数码', icon: '/static/category-phone.png' },
  { id: 2, name: '电脑办公', icon: '/static/category-computer.png' },
  { id: 3, name: '家用电器', icon: '/static/category-appliance.png' },
  { id: 4, name: '服装鞋包', icon: '/static/category-clothes.png' }
])

/** 商品列表数据 */
const products = ref([])

// ==================== 页面生命周期 ====================

/** 页面加载时执行：获取首页数据 */
onMounted(() => {
  loadHomeData()
})

// ==================== 方法定义 ====================

/**
 * 加载首页数据
 * 调用后端 API 获取商品列表
 */
async function loadHomeData() {
  loading.value = true
  try {
    // 调用后端 API 获取商品列表
    // request.js 已经解包了数据，直接返回 data 字段（商品数组）
    const data = await getProductList({})
    
    // 处理响应数据
    if (data && Array.isArray(data)) {
      // 取前 8 个商品作为热门推荐
      products.value = data.slice(0, 8)
      console.log('首页数据加载成功，商品数量:', products.value.length)
    } else {
      console.error('接口返回数据异常:', data)
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '网络请求失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 搜索事件处理
 * @param {Event} e - 输入事件对象
 */
function onSearch(e) {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }
  
  // 跳转到商品列表页并带上搜索关键词
  uni.navigateTo({
    url: `/pages/product/list/list?keyword=${keyword.value}`
  })
}

/**
 * 点击轮播图
 * @param {Object} item - 轮播图数据项
 */
function onBannerClick(item) {
  if (item.link) {
    // 如果有链接则跳转
    console.log('点击轮播图:', item)
  }
}

/**
 * 跳转到分类页面
 * @param {Number} categoryId - 分类ID
 */
function goToCategory(categoryId) {
  uni.navigateTo({
    url: `/pages/product/list/list?categoryId=${categoryId}`
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
 * 加入购物车
 * @param {Object} product - 商品信息
 */
async function addCart(product) {
  // 检查是否登录
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  // 获取用户 ID
  const userInfo = uni.getStorageSync('userInfo')
  const userId = userInfo?.id
  
  if (!userId) {
    uni.showToast({ title: '用户信息异常', icon: 'none' })
    return
  }
  
  try {
    // 调用加入购物车 API
    const { addCart: addCartApi } = await import('../../api/index')
    const res = await addCartApi({
      userId,
      productId: product.id,
      productName: product.name,
      imageUrl: product.imageUrl,
      price: product.price,
      quantity: 1
    })
    
    if (res.code === 200) {
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    } else {
      uni.showToast({ title: res.message || '添加失败', icon: 'none' })
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;  /* 最小高度为视口高度 */
  background-color: #f5f5f5;
  padding-bottom: 120rpx;  /* 底部留出 tabBar 的空间 */
}

/* ==================== 搜索栏样式 ==================== */
.search-bar {
  background-color: #ff6700;
  padding: 20rpx 30rpx;
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;  /* 两端对齐 */
}

.search-box {
  flex: 1;
  background-color: #ffffff;
  border-radius: 40rpx;
  padding: 16rpx 30rpx;
  display: flex;
  align-items: center;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  height: 44rpx;
  font-size: 28rpx;
}

/* ==================== 轮播图样式 ==================== */
.banner-swiper {
  width: 100%;
  height: 360rpx;
  margin: 20rpx 0;
}

.banner-image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}

/* ==================== 分类网格样式 ==================== */
.category-grid {
  display: flex;  /* 弹性布局 */
  justify-content: space-around;  /* 均匀分布 */
  padding: 30rpx 20rpx;
  background-color: #ffffff;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
}

.category-item {
  display: flex;
  flex-direction: column;  /* 垂直排列：图标+文字 */
  align-items: center;
  width: 120rpx;
}

.category-icon {
  width: 90rpx;
  height: 90rpx;
  margin-bottom: 12rpx;
}

.category-name {
  font-size: 24rpx;
  color: #333;
  text-align: center;
}

/* ==================== 区块标题样式 ==================== */
.section {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-more {
  font-size: 26rpx;
  color: #999;
}

/* ==================== 商品列表样式 ==================== */
.product-list {
  display: flex;
  flex-wrap: wrap;  /* 允许换行 */
  gap: 20rpx;  /* 商品卡片间距 */
}

.product-card {
  width: calc(50% - 10rpx);  /* 一行显示2个，减去间距 */
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 340rpx;
  background-color: #f8f8f8;  /* 占位背景色 */
}

.product-info {
  padding: 20rpx;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  display: -webkit-box;  /* 多行文本截断 */
  -webkit-line-clamp: 2;  /* 最多显示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  height: 78rpx;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.product-price {
  font-size: 36rpx;
  color: #ff6700;
  font-weight: bold;
}

.add-cart-btn {
  width: 56rpx;
  height: 56rpx;
  min-width: 56rpx;
  min-height: 56rpx;
  background-color: #ff6700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.add-cart-btn text {
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
  line-height: 1;
  display: block;
}
</style>