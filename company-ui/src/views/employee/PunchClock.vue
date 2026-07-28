<template>
  <div class="punch-page fade-in-up">
    <h2 class="page-title">打卡签到</h2>

    <div style="display:flex;gap:24px;flex-wrap:wrap;">
      <!-- 打卡区域 -->
      <div class="holo-panel corner-decor punch-area" style="flex:1;min-width:340px;padding:40px;text-align:center;">
        <div class="punch-clock">
          <span class="punch-time">{{ currentTime }}</span>
          <span class="punch-date">{{ currentDate }}</span>
        </div>

        <div v-if="todayStatus" class="today-record" style="margin:24px 0;">
          <div class="record-item">
            <span>签到：</span>
            <span class="record-time">{{ todayStatus.checkinTime || '-' }}</span>
          </div>
          <div class="record-item">
            <span>签退：</span>
            <span class="record-time">{{ todayStatus.checkoutTime || '-' }}</span>
          </div>
          <div class="record-item">
            <span>状态：</span>
            <el-tag :style="punchStatusStyle(todayStatus.status)" size="small">
              {{ punchStatusLabel(todayStatus.status) }}
            </el-tag>
          </div>
        </div>

        <div class="punch-actions">
          <el-button
            class="punch-btn punch-in-btn"
            :disabled="punchDisabled.in"
            :loading="punching === 'in'"
            @click="handlePunch('in')"
          >
            <span class="punch-icon">▶</span>
            <span>签到打卡</span>
          </el-button>
          <el-button
            class="punch-btn punch-out-btn"
            :disabled="punchDisabled.out"
            :loading="punching === 'out'"
            @click="handlePunch('out')"
          >
            <span class="punch-icon">⏹</span>
            <span>签退打卡</span>
          </el-button>
        </div>

        <p class="punch-hint">上班 9:00 前 · 下班 18:00 后</p>
      </div>

      <!-- 补卡申请 -->
      <div class="holo-panel corner-decor" style="flex:1;min-width:340px;padding:24px;">
        <h3 style="color:var(--neon-purple);letter-spacing:2px;margin-bottom:20px;">◆ 补卡申请</h3>
        <el-form :model="suppForm" label-width="80px">
          <el-form-item label="补卡日期">
            <el-date-picker v-model="suppForm.checkDate" type="date" placeholder="选择日期"
              value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
          <el-form-item label="补卡类型">
            <el-radio-group v-model="suppForm.type">
              <el-radio value="in" style="color:var(--text-primary)">签到补卡</el-radio>
              <el-radio value="out" style="color:var(--text-primary)">签退补卡</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="申请原因">
            <el-input v-model="suppForm.reason" type="textarea" :rows="3" placeholder="请说明补卡原因" />
          </el-form-item>
          <el-form-item>
            <el-button class="cyber-btn-primary" @click="handleSupplement" :loading="suppSubmitting">
              提交申请
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { checkinApi } from '@/api/checkin'
import dayjs from 'dayjs'

const currentTime = ref('')
const currentDate = ref('')
const todayStatus = ref(null)
const punching = ref(null)
const suppSubmitting = ref(false)
let timer = null

const suppForm = reactive({ checkDate: '', type: 'in', reason: '' })

const punchDisabled = computed(() => {
  if (!todayStatus.value) return { in: false, out: true }
  const s = todayStatus.value
  return {
    in: !!s.checkinTime,
    out: !s.checkinTime || !!s.checkoutTime
  }
})

const punchStatusLabel = (s) => ({ normal: '正常', late: '迟到', early: '早退', absent: '缺勤' }[s] || s)
const punchStatusStyle = (s) => {
  const map = { normal: 'color:#00ff88;border-color:#00ff88', late: 'color:#ff8c00;border-color:#ff8c00', early: 'color:#ffdd00;border-color:#ffdd00', absent: 'color:#ff2d95;border-color:#ff2d95' }
  return map[s] || ''
}

const updateClock = () => {
  const now = dayjs()
  currentTime.value = now.format('HH:mm:ss')
  currentDate.value = now.format('YYYY-MM-DD  dddd')
}

const loadToday = async () => {
  try {
    todayStatus.value = await checkinApi.today()
  } catch (e) { todayStatus.value = null }
}

const handlePunch = async (type) => {
  punching.value = type
  try {
    await checkinApi.punch(type)
    ElMessage.success(type === 'in' ? '签到成功！' : '签退成功！')
    await loadToday()
  } catch (e) {
    ElMessage.error(e.message || '打卡失败')
  } finally {
    punching.value = null
  }
}

const handleSupplement = async () => {
  if (!suppForm.checkDate) { ElMessage.warning('请选择补卡日期'); return }
  if (!suppForm.reason) { ElMessage.warning('请填写申请原因'); return }

  suppSubmitting.value = true
  try {
    await checkinApi.supplement({
      checkDate: suppForm.checkDate,
      type: suppForm.type,
      reason: suppForm.reason
    })
    ElMessage.success('补卡申请已提交')
    suppForm.checkDate = ''
    suppForm.reason = ''
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    suppSubmitting.value = false
  }
}

onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadToday()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.punch-page { animation: fadeInUp 0.6s ease-out; }
.punch-area { display: flex; flex-direction: column; align-items: center; }
.punch-clock {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.punch-time {
  font-family: var(--font-mono);
  font-size: 56px;
  font-weight: 700;
  color: var(--neon-cyan);
  text-shadow: 0 0 25px rgba(0, 240, 255, 0.5);
  letter-spacing: 6px;
}
.punch-date {
  font-size: 14px;
  color: var(--text-dim);
  margin-top: 8px;
}
.today-record {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 280px;
}
.record-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--text-secondary);
}
.record-time {
  font-family: var(--font-mono);
  color: var(--text-primary);
}
.punch-actions {
  display: flex;
  gap: 20px;
  margin: 28px 0;
}
.punch-btn {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  cursor: pointer;
}
.punch-in-btn {
  background: rgba(0, 240, 255, 0.1);
  border: 2px solid var(--neon-cyan);
  color: var(--neon-cyan);
}
.punch-in-btn:hover:not(:disabled) {
  background: rgba(0, 240, 255, 0.2);
  box-shadow: 0 0 40px rgba(0, 240, 255, 0.3);
  transform: scale(1.05);
}
.punch-out-btn {
  background: rgba(179, 71, 234, 0.1);
  border: 2px solid var(--neon-purple);
  color: var(--neon-purple);
}
.punch-out-btn:hover:not(:disabled) {
  background: rgba(179, 71, 234, 0.2);
  box-shadow: 0 0 40px rgba(179, 71, 234, 0.3);
  transform: scale(1.05);
}
.punch-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.punch-icon { font-size: 28px; }
.punch-hint {
  font-size: 12px;
  color: var(--text-dim);
  letter-spacing: 1px;
}
</style>
