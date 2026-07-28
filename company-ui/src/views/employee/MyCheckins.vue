<template>
  <div class="my-checkins fade-in-up">
    <h2 class="page-title">我的考勤</h2>

    <div class="search-bar">
      <el-date-picker
        v-model="searchMonth"
        type="month"
        placeholder="选择月份"
        value-format="YYYY-MM"
        clearable
        style="width:160px"
        @change="loadList"
      />
      <el-select v-model="searchStatus" placeholder="考勤状态" clearable style="width:140px" @change="loadList">
        <el-option label="正常" value="normal" />
        <el-option label="迟到" value="late" />
        <el-option label="早退" value="early" />
        <el-option label="缺勤" value="absent" />
      </el-select>
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="checkDate" label="日期" width="130" />
        <el-table-column prop="checkinTime" label="签到时间" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-cyan': row.checkinTime }">{{ row.checkinTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkoutTime" label="签退时间" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-purple': row.checkoutTime }">{{ row.checkoutTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="考勤状态" width="110">
          <template #default="{ row }">
            <el-tag :style="statusStyle(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审批状态" width="110">
          <template #default="{ row }">
            <el-tag :type="auditType(row.auditStatus)" size="small">
              {{ auditLabel(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { checkinApi } from '@/api/checkin'
import dayjs from 'dayjs'

const tableData = ref([])
const loading = ref(false)
const searchMonth = ref(dayjs().format('YYYY-MM'))
const searchStatus = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const statusLabel = (s) => ({ normal: '正常', late: '迟到', early: '早退', absent: '缺勤' }[s] || s)
const statusStyle = (s) => {
  const map = { normal: 'color:#00ff88;border-color:#00ff88', late: 'color:#ff8c00;border-color:#ff8c00', early: 'color:#ffdd00;border-color:#ffdd00', absent: 'color:#ff2d95;border-color:#ff2d95' }
  return map[s] || ''
}
const auditLabel = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s)
const auditType = (s) => ({ pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info')

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchMonth.value) params.month = searchMonth.value
    if (searchStatus.value) params.status = searchStatus.value
    const res = await checkinApi.myList(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载考勤记录失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadList())
</script>

<style scoped>
.my-checkins { animation: fadeInUp 0.6s ease-out; }
.text-cyan { color: var(--neon-cyan); font-family: var(--font-mono); }
.text-purple { color: var(--neon-purple); font-family: var(--font-mono); }
</style>
