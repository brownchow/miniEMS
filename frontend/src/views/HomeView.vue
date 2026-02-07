<template>
  <div class="home">
    <el-container>
      <el-header class="header">
        <div class="logo">miniEMS 能源管理系统</div>
        <el-menu
          mode="horizontal"
          :router="true"
          :default-active="$route.path"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/dashboard">监控仪表板</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="card">
              <template #header>
                <span>系统概览</span>
              </template>
              <p>欢迎使用 miniEMS 能源管理系统</p>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="card">
              <template #header>
                <span>快速入口</span>
              </template>
              <el-button type="primary" @click="$router.push('/dashboard')">
                进入监控仪表板
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.home {
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.card {
  margin-bottom: 20px;
}
</style>
