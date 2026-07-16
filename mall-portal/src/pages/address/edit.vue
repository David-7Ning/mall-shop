<template>
  <!-- 添加/编辑收货地址页 -->
  <view class="container">
    
    <!-- 表单区域 -->
    <view class="form-section">
      <!-- 收货人姓名 -->
      <view class="form-item">
        <text class="label">收货人</text>
        <input 
          class="input" 
          v-model="formData.name" 
          placeholder="请输入收货人姓名"
          maxlength="20"
        />
      </view>
      
      <!-- 收货人电话 -->
      <view class="form-item">
        <text class="label">手机号码</text>
        <input 
          class="input" 
          v-model="formData.phone" 
          placeholder="请输入手机号码"
          type="number"
          maxlength="11"
        />
      </view>
      
      <!-- 所在地区 -->
      <picker 
        mode="multiSelector"
        :range="regionColumns"
        :value="regionIndex"
        @change="onRegionChange"
        @columnchange="onColumnChange"
      >
        <view class="form-item">
          <text class="label">所在地区</text>
          <text class="input" :class="{ placeholder: !regionText }">
            {{ regionText || '请选择省/市/区' }}
          </text>
        </view>
      </picker>
      
      <!-- 详细地址 -->
      <view class="form-item">
        <text class="label">详细地址</text>
        <input 
          class="input" 
          v-model="formData.detail" 
          placeholder="街道、门牌号等详细地址"
          maxlength="100"
        />
      </view>
      
      <!-- 设为默认地址 -->
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch 
          :checked="formData.isDefault === 1" 
          @change="onDefaultChange"
          color="#ff6700"
        />
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-btn" @click="saveAddress">
      <text class="save-text">保存地址</text>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAddressDetail, addAddress, updateAddress } from '../../api/index'

// ==================== 响应式数据 ====================

/** 表单数据 */
const formData = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

/** 是否正在加载 */
const loading = ref(false)

/** 当前编辑的地址ID */
const addressId = ref(null)

/** 是否显示地区选择器 */
const showRegionPicker = ref(false)

/** 地区选择器的值（用于 picker 组件） */
const regionValue = ref(['', '', ''])

/** 地区选择器的索引 */
const regionIndex = ref([0, 0, 0])

/** 登录状态 */
const isLoggedIn = ref(false)

/** 地区数据（简化版，只包含部分省市） */
const regionData = ref([
  {
    name: '北京市',
    city: [{ name: '北京市', district: ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '顺义区', '通州区', '大兴区', '昌平区'] }]
  },
  {
    name: '上海市',
    city: [{ name: '上海市', district: ['黄浦区', '徐汇区', '长宁区', '静安区', '普陀区', '虹口区', '杨浦区', '浦东新区', '闵行区', '宝山区'] }]
  },
  {
    name: '广东省',
    city: [
      { name: '广州市', district: ['荔湾区', '越秀区', '海珠区', '天河区', '白云区', '黄埔区', '番禺区', '花都区', '南沙区', '增城区'] },
      { name: '深圳市', district: ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区', '盐田区', '龙华区', '坪山区', '光明区'] },
      { name: '东莞市', district: ['莞城街道', '南城街道', '东城街道', '万江街道', '石龙镇', '虎门镇', '长安镇', '厚街镇'] }
    ]
  },
  {
    name: '浙江省',
    city: [
      { name: '杭州市', district: ['上城区', '下城区', '江干区', '拱墅区', '西湖区', '滨江区', '萧山区', '余杭区', '临平区'] },
      { name: '宁波市', district: ['海曙区', '江北区', '北仑区', '镇海区', '鄞州区', '奉化区', '余姚市', '慈溪市'] },
      { name: '温州市', district: ['鹿城区', '龙湾区', '瓯海区', '洞头区', '瑞安市', '乐清市', '龙港市'] }
    ]
  },
  {
    name: '江苏省',
    city: [
      { name: '南京市', district: ['玄武区', '秦淮区', '建邺区', '鼓楼区', '浦口区', '栖霞区', '雨花台区', '江宁区', '六合区'] },
      { name: '苏州市', district: ['姑苏区', '虎丘区', '吴中区', '相城区', '吴江区', '常熟市', '张家港市', '昆山市'] },
      { name: '无锡市', district: ['梁溪区', '锡山区', '惠山区', '滨湖区', '新吴区', '江阴市', '宜兴市'] }
    ]
  },
  {
    name: '四川省',
    city: [
      { name: '成都市', district: ['锦江区', '青羊区', '金牛区', '武侯区', '成华区', '龙泉驿区', '青白江区', '新都区', '温江区'] },
      { name: '绵阳市', district: ['涪城区', '游仙区', '安州区', '江油市', '三台县', '盐亭县', '梓潼县'] }
    ]
  },
  {
    name: '湖北省',
    city: [
      { name: '武汉市', district: ['江岸区', '江汉区', '硚口区', '汉阳区', '武昌区', '青山区', '洪山区', '东西湖区', '江夏区'] },
      { name: '宜昌市', district: ['西陵区', '伍家岗区', '点军区', '猇亭区', '夷陵区', '宜都市', '当阳市', '枝江市'] }
    ]
  },
  {
    name: '山东省',
    city: [
      { name: '济南市', district: ['历下区', '市中区', '槐荫区', '天桥区', '历城区', '长清区', '章丘区', '济阳区'] },
      { name: '青岛市', district: ['市南区', '市北区', '黄岛区', '崂山区', '李沧区', '城阳区', '即墨区', '胶州市'] }
    ]
  }
])

// ==================== 计算属性 ====================

/** 地区选择器的列数据 */
const regionColumns = computed(() => {
  const provinces = regionData.value.map(p => p.name)
  const cities = regionData.value[regionIndex.value[0]]?.city.map(c => c.name) || []
  const districts = regionData.value[regionIndex.value[0]]?.city[regionIndex.value[1]]?.district || []
  
  return [provinces, cities, districts]
})

/** 地区文本 */
const regionText = computed(() => {
  const { province, city, district } = formData.value
  if (province || city || district) {
    return `${province}${city}${district}`
  }
  return ''
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
    initPage()
  } else {
    isLoggedIn.value = false
    
    uni.showModal({
      title: '提示',
      content: '请先登录后再管理地址',
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

/**
 * 初始化页面
 */
function initPage() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  
  if (options.id) {
    addressId.value = parseInt(options.id)
    loadAddressDetail()
  }
}

// ==================== 方法定义 ====================

/**
 * 加载地址详情（编辑模式）
 */
async function loadAddressDetail() {
  loading.value = true
  
  try {
    const data = await getAddressDetail(addressId.value)
    formData.value = {
      name: data.name,
      phone: data.phone,
      province: data.province,
      city: data.city,
      district: data.district,
      detail: data.detail,
      isDefault: data.isDefault
    }
    
    // 设置地区选择器的值
    regionValue.value = [data.province, data.city, data.district]
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 切换默认地址
 */
function onDefaultChange(e) {
  formData.value.isDefault = e.detail.value ? 1 : 0
}

/**
 * 地区选择变化
 * @param {Object} e - 选择器事件
 */
function onRegionChange(e) {
  const [provinceIdx, cityIdx, districtIdx] = e.detail.value
  const province = regionData.value[provinceIdx]?.name || ''
  const city = regionData.value[provinceIdx]?.city[cityIdx]?.name || ''
  const district = regionData.value[provinceIdx]?.city[cityIdx]?.district[districtIdx] || ''
  
  formData.value.province = province
  formData.value.city = city
  formData.value.district = district
  regionIndex.value = [provinceIdx, cityIdx, districtIdx]
}

/**
 * 列变化时更新后续列的数据
 * @param {Object} e - 列变化事件
 */
function onColumnChange(e) {
  const column = e.detail.column
  const value = e.detail.value
  
  if (column === 0) {
    // 省份变化，重置城市和区县索引
    regionIndex.value = [value, 0, 0]
  } else if (column === 1) {
    // 城市变化，重置区县索引
    regionIndex.value = [regionIndex.value[0], value, 0]
  }
}

/**
 * 保存地址
 */
async function saveAddress() {
  // 表单验证
  if (!formData.value.name) {
    uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
    return
  }
  
  if (!formData.value.phone || formData.value.phone.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号码', icon: 'none' })
    return
  }
  
  if (!formData.value.province) {
    uni.showToast({ title: '请选择所在地区', icon: 'none' })
    return
  }
  
  if (!formData.value.detail) {
    uni.showToast({ title: '请输入详细地址', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    
    const userInfo = uni.getStorageSync('userInfo')
    const userId = userInfo?.id || 1
    
    const data = {
      ...formData.value,
      userId
    }
    
    if (addressId.value) {
      // 编辑模式
      await updateAddress(addressId.value, data)
      uni.hideLoading()
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      // 新增模式
      await addAddress(data)
      uni.hideLoading()
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    
    // 返回上一页
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
    
  } catch (error) {
    uni.hideLoading()
    console.error('保存失败:', error)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style lang="scss">
/* ==================== 容器样式 ==================== */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

/* ==================== 表单区域样式 ==================== */
.form-section {
  background-color: #ffffff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 0 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 28rpx;
  color: #333;
  width: 160rpx;
  flex-shrink: 0;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.input.placeholder {
  color: #999;
}

.switch-item {
  justify-content: space-between;
}

/* ==================== 保存按钮样式 ==================== */
.save-btn {
  position: fixed;
  bottom: 40rpx;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #ff6700, #ff8533);
  color: #ffffff;
  padding: 24rpx 200rpx;
  border-radius: 50rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 103, 0, 0.3);
}

.save-text {
  font-size: 30rpx;
  font-weight: bold;
}
</style>