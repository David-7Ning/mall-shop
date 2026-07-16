<template>
  <!-- 登录/注册页面 -->
  <view class="login-container">

    <!-- 🏷️ Logo 和标题区域 -->
    <view class="header">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">商城系统</text>
      <text class="slogan">优质商品 · 品质生活</text>
    </view>

    <!-- 📝 表单区域 -->
    <view class="form-section">

      <!-- 切换标签：登录/注册 -->
      <view class="tab-header">
        <view class="tab-item" :class="{ active: isLogin }" @click="switchTab(true)">
          <text>登录</text>
        </view>
        <view class="tab-item" :class="{ active: !isLogin }" @click="switchTab(false)">
          <text>注册</text>
        </view>
      </view>

      <!-- 👤 用户名输入框 -->
      <view class="input-group">
        <view class="input-wrapper">
          <text class="icon">👤</text>
          <input type="text" v-model="form.username" placeholder="请输入用户名" class="input-field" maxlength="20" />
        </view>
      </view>

      <!--  密码输入框 -->
      <view class="input-group">
        <view class="input-wrapper">
          <text class="icon">🔒</text>
          <input type="password" v-model="form.password" placeholder="请输入密码（6-20位）" class="input-field"
            maxlength="20" />
        </view>
      </view>

      <!-- 📧 注册时显示：确认密码（仅注册模式） -->
      <view class="input-group" v-if="!isLogin">
        <view class="input-wrapper">
          <text class="icon">🔒</text>
          <input type="password" v-model="form.confirmPassword" placeholder="请再次输入密码" class="input-field"
            maxlength="20" />
        </view>
      </view>

      <!-- 📱 注册时显示：昵称（仅注册模式） -->
      <view class="input-group" v-if="!isLogin">
        <view class="input-wrapper">
          <text class="icon">✏️</text>
          <input type="text" v-model="form.nickname" placeholder="请输入昵称（选填）" class="input-field" maxlength="50" />
        </view>
      </view>

      <!-- ✅ 提交按钮 -->
      <button class="submit-btn" :disabled="isLoading" @click="handleSubmit">
        <text>{{ isLoading ? '处理中...' : (isLogin ? '登 录' : '注 册') }}</text>
      </button>

    </view>

    <!-- ℹ️ 底部提示 -->
    <view class="footer">
      <text class="tip">登录即表示同意</text>
      <text class="link">《用户协议》</text>
      <text class="tip">和</text>
      <text class="link">《隐私政策》</text>
    </view>

  </view>
</template>

<script setup>
// Vue 3 Composition API
import { ref, reactive } from 'vue'
import { login as loginApi, register as registerApi } from '../../api/index'
// ==================== 响应式数据 ====================

/** 是否为登录模式（true=登录，false=注册） */
const isLogin = ref(true)

/** 是否正在加载（防止重复提交） */
const isLoading = ref(false)

/** 表单数据对象 */
const form = reactive({
  username: '',           // 用户名
  password: '',           // 密码
  confirmPassword: '',     // 确认密码（仅注册）
  nickname: ''            // 昵称（仅注册）
})

// ==================== 方法定义 ====================

/**
 * 切换登录/注册标签
 * @param {Boolean} login - true切换到登录，false切换到注册
 */
function switchTab(login) {
  isLogin.value = login

  // 切换时清空表单
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.nickname = ''
}

/**
 * 表单提交处理（登录或注册）
 */
async function handleSubmit() {

  // ===== 表单验证 =====

  if (!form.username.trim()) {
    uni.showToast({ title: '请输入用户名', icon: 'none' })
    return
  }

  if (!form.password.trim()) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  if (form.password.length < 6 || form.password.length > 20) {
    uni.showToast({ title: '密码长度应为6-20位', icon: 'none' })
    return
  }

  // 注册模式额外验证
  if (!isLogin.value) {
    if (form.password !== form.confirmPassword) {
      uni.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }
  }

  // ===== 开始提交 =====
  isLoading.value = true

  try {

    if (isLogin.value) {
      // 执行登录操作
      await handleLogin()
    } else {
      // 执行注册操作
      await handleRegister()
    }

  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}

/**
 * 处理登录请求
 */
async function handleLogin() {
  try {
    // 调用后端登录接口
    const res = await loginApi({
      username: form.username,
      password: form.password
    })
    
    // 后端返回的是 { user, token } 对象
    // 保存用户信息
    uni.setStorageSync('userInfo', res.user)
    // 保存 JWT Token
    uni.setStorageSync('token', res.token)
    
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1000)
  } catch (error) {
    console.error('登录失败:', error)
    throw error
  }
}


/**
 * 处理注册请求
 */
async function handleRegister() {
  try {
    // 调用后端注册接口
    await registerApi({
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username
    })
    
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    
    // 切换到登录模式
    setTimeout(() => {
      isLogin.value = true
    }, 1000)
  } catch (error) {
    console.error('注册失败:', error)
    throw error
  }
}


</script>

<style lang="scss">
/* ==================== 页面容器 ==================== */
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 60rpx;
  display: flex;
  flex-direction: column;
}

/* ==================== 头部 Logo 区域 ==================== */
.header {
  padding: 120rpx 0 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 30rpx;
  background-color: #fff;
  border-radius: 40rpx;
  padding: 20rpx;
  /* 如果没有logo图片时的占位效果 */
}

.app-name {
  font-size: 48rpx;
  color: #ffffff;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.slogan {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* ==================== 表单区域 ==================== */
.form-section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.15);
}

/* 标签栏：登录/注册切换 */
.tab-header {
  display: flex;
  margin-bottom: 50rpx;
  border-bottom: 2rpx solid #eeeeee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding-bottom: 24rpx;
  position: relative;
}

.tab-item text {
  font-size: 32rpx;
  color: #999999;
  font-weight: 500;
}

.tab-item.active text {
  color: #667eea;
  font-weight: bold;
  font-size: 36rpx;
}

/* 激活状态的下划线 */
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 120rpx;
  height: 4rpx;
  background-color: #667eea;
  border-radius: 2rpx;
}

/* 输入框组 */
.input-group {
  margin-bottom: 32rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 12rpx;
  padding: 0 28rpx;
  height: 96rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  background-color: #fff;
  border-color: #667eea;
  box-shadow: 0 0 0 4rpx rgba(102, 126, 234, 0.1);
}

.icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.input-field {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: #333;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 92rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-size: 34rpx;
  font-weight: bold;
  border-radius: 46rpx;
  margin-top: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.35);
  transition: transform 0.2s;
}

.submit-btn:active {
  transform: scale(0.98);
}

.submit-btn[disabled] {
  opacity: 0.6;
}

/* ==================== 底部提示 ==================== */
.footer {
  margin-top: auto;
  padding: 60rpx 0 40rpx;
  text-align: center;
}

.tip {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.link {
  font-size: 24rpx;
  color: #ffffff;
  text-decoration: underline;
}
</style>