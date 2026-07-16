import { createSSRApp } from 'vue'
import App from './App.vue'
/**
 * 创建应用实例
 * uni-app 要求导出一个 createApp 函数
 * 框架会在启动时自动调用这个函数来初始化 Vue 应用
 */
export function createApp() {
  // 使用 createSSRApp 创建 Vue 应用（支持服务端渲染）
  // SSR = Server Side Rendering，uni-app 为了兼容小程序必须使用这种方式
  const app = createSSRApp(App)
  
  // 返回应用实例对象
  // 可以在这里添加全局组件、指令、中间件等
  return {
    app  // 将创建好的 app 实例返回给框架使用
  }
}