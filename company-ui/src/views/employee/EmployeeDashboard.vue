<template>
  <div class="emp-dashboard fade-in-up">
    <h2 class="page-title">工作台</h2>

    <!-- 欢迎卡片 -->
    <div class="holo-panel corner-decor welcome-card" style="padding:28px;margin-bottom:24px;">
      <div class="welcome-content">
        <div>
          <h3 style="font-size:22px;color:var(--neon-purple);margin:0 0 8px;letter-spacing:2px">
            {{ greetingText }}，{{ auth.userInfo?.realName || '员工' }}
          </h3>
          <p style="color:var(--text-secondary);margin:0">
            {{ auth.userInfo?.deptName || '' }} · {{ auth.userInfo?.postName || '' }}
          </p>
        </div>
        <div class="welcome-time">
          <span class="time-digital">{{ currentTime }}</span>
          <span class="time-date">{{ currentDate }}</span>
        </div>
      </div>
    </div>

    <!-- 今日打卡状态 -->
    <div class="holo-panel corner-decor" style="padding:20px;margin-bottom:24px;">
      <h3 style="color:var(--neon-cyan);letter-spacing:2px;margin-bottom:16px;">◆ 今日打卡状态</h3>
      <div v-if="todayStatus" class="today-status">
        <div class="status-row">
          <span class="status-label">签到时间</span>
          <span class="status-value" :class="{ 'text-cyan': todayStatus.checkinTime }">
            {{ todayStatus.checkinTime || '未签到' }}
          </span>
        </div>
        <div class="status-row">
          <span class="status-label">签退时间</span>
          <span class="status-value" :class="{ 'text-purple': todayStatus.checkoutTime }">
            {{ todayStatus.checkoutTime || '未签退' }}
          </span>
        </div>
        <div class="status-row">
          <span class="status-label">考勤状态</span>
          <el-tag :style="statusStyle(todayStatus.status)" size="default">
            {{ statusLabel(todayStatus.status) }}
          </el-tag>
        </div>
      </div>
      <div v-else style="color:var(--text-dim);text-align:center;padding:20px;">
        暂无今日考勤记录，请前往打卡页面
      </div>
    </div>

    <!-- 本月统计 + 快捷入口 -->
    <div style="display:flex;gap:24px;flex-wrap:wrap;">
      <div class="holo-panel corner-decor" style="flex:1;min-width:280px;padding:20px;">
        <h3 style="color:var(--neon-purple);letter-spacing:2px;margin-bottom:16px;">◆ 本月概览</h3>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
          <div class="mini-stat">
            <span class="mini-num" style="color:var(--neon-green)">{{ monthStats.normal }}</span>
            <span class="mini-label">正常</span>
          </div>
          <div class="mini-stat">
            <span class="mini-num" style="color:var(--neon-orange)">{{ monthStats.late }}</span>
            <span class="mini-label">迟到</span>
          </div>
          <div class="mini-stat">
            <span class="mini-num" style="color:var(--neon-yellow)">{{ monthStats.early }}</span>
            <span class="mini-label">早退</span>
          </div>
          <div class="mini-stat">
            <span class="mini-num" style="color:var(--neon-pink)">{{ monthStats.absent }}</span>
            <span class="mini-label">缺勤</span>
          </div>
        </div>
      </div>

      <div class="holo-panel corner-decor" style="flex:1;min-width:280px;padding:20px;">
        <h3 style="color:var(--neon-cyan);letter-spacing:2px;margin-bottom:16px;">◆ 快捷入口</h3>
        <div style="display:flex;flex-direction:column;gap:12px;">
          <el-button class="quick-btn" @click="$router.push('/employee/punch')">
            <el-icon><Clock /></el-icon> 打卡签到
          </el-button>
          <el-button class="quick-btn" @click="$router.push('/employee/checkins')">
            <el-icon><List /></el-icon> 我的考勤
          </el-button>
          <el-button class="quick-btn" @click="$router.push('/employee/salary')">
            <el-icon><Money /></el-icon> 我的薪资
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { checkinApi } from '@/api/checkin'
import dayjs from 'dayjs'

const auth = useAuthStore()
const todayStatus = ref(null)
const monthStats = reactive({ normal: 0, late: 0, early: 0, absent: 0 })
const currentTime = ref('')
const currentDate = ref('')
let timer = null

const greetingText = computed(() => {
  const h = dayjs().hour()
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const statusLabel = (s) => ({ normal: '正常', late: '迟到', early: '早退', absent: '缺勤' }[s] || s)
const statusStyle = (s) => {
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
  } catch (e) { /* 可能今天还没有记录 */ }
}

const loadMonthStats = async () => {
  try {
    const month = dayjs().format('YYYY-MM')
    const res = await checkinApi.myList({ month, page: 1, size: 200 })
    if (res?.records) {
      monthStats.normal = res.records.filter(r => r.status === 'normal').length
      monthStats.late = res.records.filter(r => r.status === 'late').length
      monthStats.early = res.records.filter(r => r.status === 'early').length
      monthStats.absent = res.records.filter(r => r.status === 'absent').length
    }
  } catch (e) { console.error('加载本月统计失败', e) }
}

onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadToday()
  loadMonthStats()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.emp-dashboard { animation: fadeInUp 0.6s ease-out; }
.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.welcome-time {
  text-align: right;
}
.time-digital {
  display: block;
  font-family: var(--font-mono);
  font-size: 36px;
  font-weight: 700;
  color: var(--neon-cyan);
  text-shadow: 0 0 15px rgba(0, 240, 255, 0.4);
  letter-spacing: 3px;
}
.time-date {
  font-size: 13px;
  color: var(--text-dim);
}
.today-status {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.status-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.status-label {
  width: 80px;
  color: var(--text-secondary);
  font-size: 14px;
}
.status-value { font-family: var(--font-mono); font-weight: 500; }
.text-cyan { color: var(--neon-cyan); }
.text-purple { color: var(--neon-purple); }

.mini-stat {
  text-align: center;
  padding: 12px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
}
.mini-num { display: block; font-size: 28px; font-weight: 700; font-family: var(--font-mono); }
.mini-label { font-size: 12px; color: var(--text-dim); margin-top: 4px; }

.quick-btn {
  width: 100%;
  height: 44px;
  background: rgba(0,240,255,0.05);
  border: 1px solid rgba(0,240,255,0.2);
  color: var(--text-primary);
  font-size: 15px;
  justify-content: flex-start;
  transition: all 0.3s;
}
.quick-btn:hover {
  background: rgba(0,240,255,0.1);
  border-color: var(--neon-cyan);
  color: var(--neon-cyan);
}
</style>
