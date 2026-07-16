<script>
// App.vue 是根组件
// 所有页面都会渲染在这个组件内部
export default {
  onLaunch: function() {
    // 应用启动时执行（只执行一次）
    console.log('商城应用启动')
  },
  onShow: function() {
    // 应用从后台切换到前台时执行
    console.log('App Show')
  },
  onHide: function() {
    // 应用切换到后台时执行
    console.log('App Hide')
  }
}
</script>

<style>
/* 全局样式：在这里定义的样式会应用到所有页面 */
page {
  background-color: #f5f5f5;  /* 页面背景色：浅灰色 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 28rpx;  /* uni-app 使用 rpx 单位（响应式像素） */
  color: #333;  /* 默认文字颜色 */
}

/* 清除默认边距和内边距 */
view, text {
  box-sizing: border-box;
}
</style>