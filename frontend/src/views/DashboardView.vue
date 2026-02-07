<template>
  <div class="dashboard">
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
        <el-card class="card">
          <template #header>
            <span>BMS 实时监控</span>
          </template>
          <!-- 嵌入 Grafana 仪表板 -->
          <iframe
            src="http://localhost:3000/d/b048eb23-2969-42c5-84f1-9c4a8d520b4b/emse4bbaa-e8a1a8-e79b98"
            width="100%"
            height="600"
            frameborder="0"
          />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.dashboard {
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
