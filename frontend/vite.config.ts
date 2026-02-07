import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    proxy: {
      // 后端 API 代理
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      // Grafana 代理（解决 WSL2 网络隔离问题）
      '/grafana': {
        target: 'http://localhost:3000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/grafana/, '')
      }
    }
  }
})
