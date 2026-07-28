<template>
  <div class="my-profile fade-in-up">
    <h2 class="page-title">个人信息</h2>

    <div class="holo-panel corner-decor" style="max-width:640px;padding:32px;">
      <div class="profile-header">
        <el-avatar :size="72" class="profile-avatar">
          {{ auth.userInfo?.realName?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="profile-title">
          <h3 style="color:var(--neon-cyan);font-size:20px;margin:0;">{{ auth.userInfo?.realName }}</h3>
          <span style="color:var(--text-dim);font-size:13px;">{{ auth.userInfo?.deptName }} · {{ auth.userInfo?.postName }}</span>
        </div>
      </div>

      <el-divider style="border-color:var(--border-color)" />

      <el-descriptions :column="2" border style="--el-descriptions-item-bordered-label-background:var(--bg-panel)">
        <el-descriptions-item label="用户名" label-style="color:var(--text-secondary)">
          <span style="font-family:var(--font-mono);color:var(--neon-cyan)">{{ auth.userInfo?.username }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="姓名" label-style="color:var(--text-secondary)">
          {{ auth.userInfo?.realName }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号" label-style="color:var(--text-secondary)">
          {{ auth.userInfo?.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" label-style="color:var(--text-secondary)">
          {{ auth.userInfo?.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="性别" label-style="color:var(--text-secondary)">
          {{ auth.userInfo?.gender === 1 ? '男' : '女' }}
        </el-descriptions-item>
        <el-descriptions-item label="入职日期" label-style="color:var(--text-secondary)">
          {{ auth.userInfo?.hireDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色" label-style="color:var(--text-secondary)">
          {{ auth.userInfo?.userType === 'admin' ? '管理员' : '员工' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态" label-style="color:var(--text-secondary)">
          <el-tag :type="auth.userInfo?.status === 1 ? 'success' : 'danger'" size="small">
            {{ auth.userInfo?.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider style="border-color:var(--border-color)" />

      <div class="password-section">
        <h4 style="color:var(--neon-purple);letter-spacing:1px;margin-bottom:16px;">◆ 修改密码</h4>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width:420px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
          </el-form-item>
          <el-form-item>
            <el-button class="cyber-btn-primary" @click="handleChangePwd" :loading="pwdSubmitting">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'

const auth = useAuthStore()
const pwdFormRef = ref(null)
const pwdSubmitting = ref(false)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleChangePwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdSubmitting.value = true
  try {
    await authApi.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    auth.logout()
    setTimeout(() => window.location.reload(), 1000)
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    pwdSubmitting.value = false
  }
}
</script>

<style scoped>
.my-profile { animation: fadeInUp 0.6s ease-out; }
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}
.profile-avatar {
  background: linear-gradient(135deg, var(--neon-purple), var(--neon-pink));
  font-size: 28px;
  font-weight: 700;
}
.profile-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.password-section {
  margin-top: 8px;
}
</style>
