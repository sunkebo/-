<template>
  <div class="employee-layout">
    <SciFiBackground />
    <div class="layout-content">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="logo-area">
          <div class="logo-icon">
            <span class="logo-hex">⬡</span>
          </div>
          <div class="logo-text">
            <span class="logo-title">NEXUS</span>
            <span class="logo-sub">员工系统</span>
          </div>
        </div>

        <el-menu
          :default-active="currentRoute"
          router
          class="side-menu"
          background-color="transparent"
        >
          <el-menu-item index="/employee/dashboard">
            <el-icon><Monitor /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/employee/punch">
            <el-icon><Clock /></el-icon>
            <span>打卡签到</span>
          </el-menu-item>
          <el-menu-item index="/employee/checkins">
            <el-icon><List /></el-icon>
            <span>我的考勤</span>
          </el-menu-item>
          <el-menu-item index="/employee/salary">
            <el-icon><Money /></el-icon>
            <span>我的薪资</span>
          </el-menu-item>
          <el-menu-item index="/employee/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <div class="user-info">
            <el-avatar :size="36" class="avatar-purple">
              {{ auth.userInfo?.realName?.charAt(0) || 'E' }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ auth.userInfo?.realName || '员工' }}</span>
              <span class="user-role">{{ auth.userInfo?.deptName || '' }}</span>
            </div>
          </div>
          <el-button class="logout-btn" text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="main-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SciFiBackground from '@/three/SciFiBackground.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const currentRoute = computed(() => route.path)

const handleLogout = () => {
  auth.logout()
  router.push('/employee/login')
}
</script>

<style scoped>
.employee-layout {
  height: 100vh;
  display: flex;
  position: relative;
}
.layout-content {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  height: 100%;
}

.sidebar {
  width: 240px;
  min-width: 240px;
  background: rgba(13, 13, 26, 0.9);
  border-right: 1px solid rgba(179, 71, 234, 0.15);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(20px);
}

.logo-area {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(179, 71, 234, 0.1);
}
.logo-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-hex {
  font-size: 32px;
  color: var(--neon-purple);
  animation: glow-pulse 2s infinite;
}
.logo-text {
  display: flex;
  flex-direction: column;
}
.logo-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 4px;
  color: var(--neon-purple);
  text-shadow: 0 0 15px rgba(179, 71, 234, 0.5);
}
.logo-sub {
  font-size: 11px;
  color: var(--text-dim);
  letter-spacing: 3px;
}

.side-menu {
  flex: 1;
  padding-top: 8px;
  border-right: none !important;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(179, 71, 234, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar-purple {
  background: linear-gradient(135deg, var(--neon-purple), var(--neon-pink));
  color: #fff;
  font-weight: 600;
}
.user-detail {
  display: flex;
  flex-direction: column;
}
.user-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}
.user-role {
  font-size: 11px;
  color: var(--text-dim);
}
.logout-btn {
  color: var(--text-dim);
  font-size: 20px;
}
.logout-btn:hover {
  color: var(--neon-pink);
}

.main-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
