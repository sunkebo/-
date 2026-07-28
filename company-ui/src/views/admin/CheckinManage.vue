<template>
  <div class="checkin-manage fade-in-up">
    <h2 class="page-title">考勤管理</h2>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(0,255,136,0.1);color:var(--neon-green)">
          <el-icon :size="24"><CircleCheck /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.normal }}</div>
          <div class="stat-label">正常</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(255,140,0,0.1);color:var(--neon-orange)">
          <el-icon :size="24"><Warning /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.late }}</div>
          <div class="stat-label">迟到</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(255,221,0,0.1);color:var(--neon-yellow)">
          <el-icon :size="24"><Clock /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.early }}</div>
          <div class="stat-label">早退</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(255,45,149,0.1);color:var(--neon-pink)">
          <el-icon :size="24"><CircleClose /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ stats.absent }}</div>
          <div class="stat-label">缺勤</div>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="search-bar">
      <el-select v-model="searchDeptId" placeholder="部门筛选" clearable style="width:180px" @change="loadList">
        <el-option v-for="d in deptList" :key="d.deptId" :label="d.name" :value="d.deptId" />
      </el-select>
      <el-select v-model="searchStatus" placeholder="考勤状态" clearable style="width:140px" @change="loadList">
        <el-option label="正常" value="normal" />
        <el-option label="迟到" value="late" />
        <el-option label="早退" value="early" />
        <el-option label="缺勤" value="absent" />
      </el-select>
      <el-date-picker
        v-model="searchMonth"
        type="month"
        placeholder="选择月份"
        value-format="YYYY-MM"
        clearable
        style="width:160px"
        @change="loadList"
      />
      <el-button class="cyber-btn-primary" @click="loadStats" :loading="statsLoading">
        <el-icon><Refresh /></el-icon> 刷新统计
      </el-button>
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="员工" width="100">
          <template #default="{ row }">{{ row.realName || row.userId }}</template>
        </el-table-column>
        <el-table-column prop="checkDate" label="日期" width="120" />
        <el-table-column prop="checkinTime" label="签到时间" width="100">
          <template #default="{ row }">{{ row.checkinTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="checkoutTime" label="签退时间" width="100">
          <template #default="{ row }">{{ row.checkoutTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag
              :style="statusStyle(row.status)"
              size="small"
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审批" width="90">
          <template #default="{ row }">
            <el-tag :type="auditType(row.auditStatus)" size="small">
              {{ auditLabel(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 'pending'" text size="small" style="color:var(--neon-green)"
              @click="openAudit(row)">
              <el-icon><Select /></el-icon> 审批
            </el-button>
            <span v-else style="color:var(--text-dim);font-size:12px">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10,20,50]"
          layout="total,sizes,prev,pager,next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 审批对话框 -->
    <el-dialog v-model="auditVisible" title="考勤审批" width="440px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio value="approved" style="color:var(--neon-green)">通过</el-radio>
            <el-radio value="rejected" style="color:var(--neon-pink)">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3" placeholder="审批备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button class="cyber-btn-primary" @click="handleAudit" :loading="auditSubmitting">确认审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { checkinApi } from '@/api/checkin'
import { deptApi } from '@/api/dept'

const tableData = ref([])
const deptList = ref([])
const loading = ref(false)
const statsLoading = ref(false)
const searchDeptId = ref(null)
const searchStatus = ref(null)
const searchMonth = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const stats = reactive({ normal: 0, late: 0, early: 0, absent: 0 })

const auditVisible = ref(false)
const auditSubmitting = ref(false)
const auditId = ref(null)
const auditForm = reactive({ auditStatus: 'approved', remark: '' })

const statusLabel = (s) => ({ normal: '正常', late: '迟到', early: '早退', absent: '缺勤' }[s] || s)
const statusStyle = (s) => {
  const map = { normal: 'color:#00ff88;border-color:#00ff88', late: 'color:#ff8c00;border-color:#ff8c00', early: 'color:#ffdd00;border-color:#ffdd00', absent: 'color:#ff2d95;border-color:#ff2d95' }
  return map[s] || ''
}
const auditLabel = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s)
const auditType = (s) => ({ pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info')

const loadDepts = async () => {
  try { deptList.value = await deptApi.list() || [] } catch (e) {}
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchDeptId.value) params.deptId = searchDeptId.value
    if (searchStatus.value) params.status = searchStatus.value
    if (searchMonth.value) params.month = searchMonth.value
    const res = await checkinApi.list(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载考勤列表失败', e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  statsLoading.value = true
  try {
    const params = {}
    if (searchMonth.value) params.month = searchMonth.value
    const res = await checkinApi.stats(params)
    if (res) {
      stats.normal = res.normal || 0
      stats.late = res.late || 0
      stats.early = res.early || 0
      stats.absent = res.absent || 0
    }
  } catch (e) {
    console.error('加载统计失败', e)
  } finally {
    statsLoading.value = false
  }
}

const openAudit = (row) => {
  auditId.value = row.id
  auditForm.auditStatus = 'approved'
  auditForm.remark = ''
  auditVisible.value = true
}

const handleAudit = async () => {
  auditSubmitting.value = true
  try {
    await checkinApi.audit(auditId.value, { ...auditForm })
    ElMessage.success('审批完成')
    auditVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e.message || '审批失败')
  } finally {
    auditSubmitting.value = false
  }
}

onMounted(() => {
  loadDepts()
  loadList()
  loadStats()
})
</script>

<style scoped>
.checkin-manage { animation: fadeInUp 0.6s ease-out; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
</style>
