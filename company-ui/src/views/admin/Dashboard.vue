<template>
  <div class="dashboard fade-in-up">
    <h2 class="page-title">管理仪表盘</h2>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(0,240,255,0.1);color:var(--neon-cyan)">
          <el-icon :size="28"><OfficeBuilding /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.deptCount }}</div>
          <div class="stat-label">部门总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(179,71,234,0.1);color:var(--neon-purple)">
          <el-icon :size="28"><Briefcase /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.postCount }}</div>
          <div class="stat-label">岗位总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(0,255,136,0.1);color:var(--neon-green)">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">员工总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(255,140,0,0.1);color:var(--neon-orange)">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.todayCheckin }}</div>
          <div class="stat-label">今日打卡人数</div>
        </div>
      </div>
    </div>

    <!-- 3D地球 + 考勤统计 -->
    <div class="bottom-row">
      <div class="holo-panel globe-panel corner-decor" style="flex:1;min-width:300px;height:320px;padding:16px;">
        <DataGlobe />
      </div>
      <div class="holo-panel checkin-panel corner-decor" style="flex:1;min-width:300px;height:320px;padding:20px;">
        <h3 style="color:var(--neon-cyan);margin-bottom:16px;letter-spacing:2px;">◆ 今日考勤概况</h3>
        <div class="checkin-stats">
          <div class="checkin-stat-item">
            <span class="checkin-label">正常</span>
            <el-progress :percentage="checkinPercent.normal" :color="'#00ff88'" :stroke-width="12" />
            <span class="checkin-num">{{ checkinData.normal || 0 }}</span>
          </div>
          <div class="checkin-stat-item">
            <span class="checkin-label">迟到</span>
            <el-progress :percentage="checkinPercent.late" :color="'#ff8c00'" :stroke-width="12" />
            <span class="checkin-num">{{ checkinData.late || 0 }}</span>
          </div>
          <div class="checkin-stat-item">
            <span class="checkin-label">早退</span>
            <el-progress :percentage="checkinPercent.early" :color="'#ffdd00'" :stroke-width="12" />
            <span class="checkin-num">{{ checkinData.early || 0 }}</span>
          </div>
          <div class="checkin-stat-item">
            <span class="checkin-label">缺勤</span>
            <el-progress :percentage="checkinPercent.absent" :color="'#ff2d95'" :stroke-width="12" />
            <span class="checkin-num">{{ checkinData.absent || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import DataGlobe from '@/three/DataGlobe.vue'
import { deptApi } from '@/api/dept'
import { postApi } from '@/api/post'
import { userApi } from '@/api/user'
import { checkinApi } from '@/api/checkin'

const stats = reactive({
  deptCount: 0,
  postCount: 0,
  userCount: 0,
  todayCheckin: 0
})

const checkinData = reactive({
  normal: 0, late: 0, early: 0, absent: 0
})

const total = computed(() => {
  const { normal, late, early, absent } = checkinData
  return normal + late + early + absent || 1
})

const checkinPercent = computed(() => ({
  normal: Math.round(checkinData.normal / total.value * 100),
  late: Math.round(checkinData.late / total.value * 100),
  early: Math.round(checkinData.early / total.value * 100),
  absent: Math.round(checkinData.absent / total.value * 100)
}))

onMounted(async () => {
  try {
    const [deptRes, postRes, userRes, checkinStatRes] = await Promise.all([
      deptApi.list(),
      postApi.list({ page: 1, size: 1 }),
      userApi.list({ page: 1, size: 1 }),
      checkinApi.stats({})
    ])
    stats.deptCount = Array.isArray(deptRes) ? deptRes.length : 0
    stats.postCount = postRes?.total || 0
    stats.userCount = userRes?.total || 0
    if (checkinStatRes) {
      checkinData.normal = checkinStatRes.normal || 0
      checkinData.late = checkinStatRes.late || 0
      checkinData.early = checkinStatRes.early || 0
      checkinData.absent = checkinStatRes.absent || 0
      stats.todayCheckin = checkinData.normal + checkinData.late + checkinData.early
    }
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
  }
})
</script>

<style scoped>
.dashboard { animation: fadeInUp 0.6s ease-out; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}
.bottom-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}
.checkin-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.checkin-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.checkin-label {
  width: 40px;
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 1px;
}
.checkin-stat-item :deep(.el-progress) { flex: 1; }
.checkin-num {
  width: 36px;
  text-align: right;
  font-family: var(--font-mono);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
