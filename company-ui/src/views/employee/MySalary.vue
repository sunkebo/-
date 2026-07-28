<template>
  <div class="my-salary fade-in-up">
    <h2 class="page-title">我的薪资</h2>

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
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="salaryMonth" label="薪资月份" width="130" />
        <el-table-column prop="baseSalary" label="基本工资" width="120">
          <template #default="{ row }">{{ formatMoney(row.baseSalary) }}</template>
        </el-table-column>
        <el-table-column prop="performance" label="绩效" width="110">
          <template #default="{ row }">{{ formatMoney(row.performance) }}</template>
        </el-table-column>
        <el-table-column prop="bonus" label="奖金" width="110">
          <template #default="{ row }">{{ formatMoney(row.bonus) }}</template>
        </el-table-column>
        <el-table-column prop="deduction" label="扣款" width="110">
          <template #default="{ row }">
            <span style="color:var(--neon-pink)">{{ formatMoney(row.deduction) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实发工资" width="140">
          <template #default="{ row }">
            <span class="actual-salary">{{ formatMoney(row.actualSalary) }}</span>
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
import { ref, onMounted } from 'vue'
import { salaryApi } from '@/api/salary'

const tableData = ref([])
const loading = ref(false)
const searchMonth = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const formatMoney = (v) => {
  if (v == null) return '¥0.00'
  return '¥' + Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
const auditLabel = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s)
const auditType = (s) => ({ pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info')

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchMonth.value) params.month = searchMonth.value
    const res = await salaryApi.myList(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载薪资记录失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadList())
</script>

<style scoped>
.my-salary { animation: fadeInUp 0.6s ease-out; }
.actual-salary {
  color: var(--neon-green);
  font-weight: 700;
  font-family: var(--font-mono);
  font-size: 16px;
}
</style>
