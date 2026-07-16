<template>
  <!-- 商品列表页面 -->
  <view class="container">
    
    <!-- 🔍 搜索栏 -->
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

    <!-- 📂 分类筛选标签 -->
    <scroll-view class="category-tabs" scroll-x>
      <view 
        class="tab-item" 
        :class="{ active: selectedCategory === 0 }"
        @click="selectCategory(0)"
      >
        <text>全部</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: selectedCategory === cat.id }"
        v-for="cat in categories" 
        :key="cat.id"
        @click="selectCategory(cat.id)"
      >
        <text>{{ cat.name }}</text>
      </view>
    </scroll-view>

    <!-- 📦 商品列表 -->
    <view class="product-list">
      <view 
        class="product-card" 
        v-for="product in productList" 
        :key="product.id"
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
          <text class="product-desc">{{ product.description || '优质商品' }}</text>
          <view class="product-bottom">
            <text class="product-price">¥{{ product.price }}</text>
            <text class="product-stock">库存 {{ product.stock }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态提示 -->
    <view class="empty-state" v-if="productList.length === 0 && !loading">
      <text class="empty-icon">📦</text>
      <text class="empty-text">暂无商品</text>
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
import { getProductList, searchProducts } from '../../../api/index'

// ==================== 响应式数据 ====================

/** 搜索关键词 */
const keyword = ref('')

/** 选中的分类ID（0=全部） */
const selectedCategory = ref(0)

/** 是否正在加载 */
const loading = ref(false)

/** 分类列表 */
const categories = ref([
  { id: 1, name: '手机数码' },
  { id: 2, name: '电脑办公' },
  { id: 3, name: '家用电器' },
  { id: 4, name: '服装鞋包' }
])

/** 商品列表数据 */
const productList = ref([])

// ==================== 页面生命周期 ====================

/** 页面加载时执行 */
onMounted(() => {
  loadProducts()
})

// ==================== 方法定义 ====================

/**
 * 加载商品列表
 */
async function loadProducts() {
  loading.value = true
  
  try {
    // 构建请求参数
    const params = {}
    if (selectedCategory.value > 0) {
      params.categoryId = selectedCategory.value
    }
    
    // 调用后端 API
    const data = await getProductList(params)
    productList.value = data || []
    
    console.log('商品列表加载成功:', productList.value.length, '个商品')
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 选择分类
 * @param {Number} categoryId - 分类ID
 */
function selectCategory(categoryId) {
  selectedCategory.value = categoryId
  loadProducts()  // 重新加载商品列表
}

/**
 * 搜索事件
 */
async function onSearch() {
  if (!keyword.value.trim()) {
    // 如果搜索词为空，加载全部商品
    loadProducts()
    return
  }
  
  loading.value = true
  
  try {
    // 调用搜索 API
    const data = await searchProducts(keyword.value.trim())
    productList.value = data || []
    
    console.log('搜索结果:', productList.value.length, '个商品')
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({ title: '搜索失败', icon: 'none' })
  } finally {
    loading.value = false
  }
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
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* ==================== 搜索栏样式 ==================== */
.search-bar {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-box {
  background-color: #f5f5f5;
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

/* ==================== 分类标签样式 ==================== */
.category-tabs {
  background-color: #ffffff;
  white-space: nowrap;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eeeeee;
}

.tab-item {
  display: inline-block;
  padding: 12rpx 32rpx;
  margin: 0 10rpx;
  border-radius: 30rpx;
  background-color: #f5f5f5;
}

.tab-item text {
  font-size: 28rpx;
  color: #666;
}

.tab-item.active {
  background-color: #ff6700;
}

.tab-item.active text {
  color: #ffffff;
  font-weight: bold;
}

/* ==================== 商品列表样式 ==================== */
.product-list {
  padding: 20rpx;
}

.product-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.product-image {
  width: 100%;
  height: 400rpx;
  background-color: #f8f8f8;
}

.product-info {
  padding: 24rpx;
}

.product-name {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 12rpx;
}

.product-desc {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 16rpx;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 40rpx;
  color: #ff6700;
  font-weight: bold;
}

.product-stock {
  font-size: 24rpx;
  color: #999;
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