<template>
  <div class="login-page">
    <LoginParticles />
    <div class="login-overlay">
      <div class="login-container">
        <div class="login-header">
          <div class="login-hex">⬡</div>
          <h1 class="login-title">NEXUS</h1>
          <p class="login-subtitle">公司综合管理系统 · 管理员</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              class="login-btn"
              size="large"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="btn-text">授 权 登 录</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="hint-text">admin / admin123</span>
          <el-link class="switch-link" @click="$router.push('/employee/login')">
            员工入口 →
          </el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import LoginParticles from '@/three/LoginParticles.vue'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/admin/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.login-overlay {
  position: relative;
  z-index: 1;
}
.login-container {
  width: 420px;
  padding: 48px 40px;
  background: rgba(13, 13, 26, 0.85);
  border: 1px solid rgba(0, 240, 255, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(20px);
  box-shadow: 0 0 60px rgba(0, 0, 0, 0.5), 0 0 30px rgba(0, 240, 255, 0.08);
}
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.login-hex {
  font-size: 48px;
  color: var(--neon-cyan);
  animation: glow-pulse 2s infinite;
  margin-bottom: 8px;
}
.login-title {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 12px;
  color: var(--neon-cyan);
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
  margin: 0;
}
.login-subtitle {
  font-size: 13px;
  color: var(--text-dim);
  letter-spacing: 4px;
  margin-top: 8px;
}

.login-form {
  margin-top: 8px;
}

.login-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.2), rgba(179, 71, 234, 0.2));
  border: 1px solid rgba(0, 240, 255, 0.5);
  color: var(--neon-cyan);
  font-size: 16px;
  letter-spacing: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.login-btn:hover {
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.35), rgba(179, 71, 234, 0.35));
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.3);
  transform: translateY(-1px);
}

.login-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}
.hint-text {
  font-size: 12px;
  color: var(--text-dim);
  font-family: var(--font-mono);
}
.switch-link {
  font-size: 13px;
  color: var(--neon-purple);
}
</style>
