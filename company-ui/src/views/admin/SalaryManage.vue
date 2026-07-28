<template>
  <div class="salary-manage fade-in-up">
    <h2 class="page-title">薪资管理</h2>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(0,240,255,0.1);color:var(--neon-cyan)">
          <el-icon :size="24"><Money /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ formatMoney(stats.totalSalary) }}</div>
          <div class="stat-label">发放总额</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:rgba(179,71,234,0.1);color:var(--neon-purple)">
          <el-icon :size="24"><TrendCharts /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ formatMoney(stats.avgSalary) }}</div>
          <div class="stat-label">平均薪资</div>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="search-bar">
      <el-input v-model="searchUserId" placeholder="员工ID" clearable style="width:150px" @keyup.enter="loadList" @clear="loadList" />
      <el-date-picker
        v-model="searchMonth"
        type="month"
        placeholder="选择月份"
        value-format="YYYY-MM"
        clearable
        style="width:160px"
        @change="loadList"
      />
      <el-button type="primary" class="cyber-btn-primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增薪资
      </el-button>
      <el-button class="cyber-btn-primary" style="border-color:var(--neon-purple)!important;color:var(--neon-purple)!important" @click="openBatch">
        <el-icon><DocumentAdd /></el-icon> 批量生成
      </el-button>
      <el-button @click="loadStats" :loading="statsLoading" style="background:transparent;border:1px solid var(--border-color);color:var(--text-secondary)">
        <el-icon><Refresh /></el-icon> 刷新统计
      </el-button>
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="salaryId" label="ID" width="70" />
        <el-table-column label="员工" width="100">
          <template #default="{ row }">{{ row.realName || row.userId }}</template>
        </el-table-column>
        <el-table-column prop="salaryMonth" label="薪资月份" width="120" />
        <el-table-column prop="baseSalary" label="基本工资" width="110">
          <template #default="{ row }">{{ formatMoney(row.baseSalary) }}</template>
        </el-table-column>
        <el-table-column prop="performance" label="绩效" width="100">
          <template #default="{ row }">{{ formatMoney(row.performance) }}</template>
        </el-table-column>
        <el-table-column prop="bonus" label="奖金" width="100">
          <template #default="{ row }">{{ formatMoney(row.bonus) }}</template>
        </el-table-column>
        <el-table-column prop="deduction" label="扣款" width="100">
          <template #default="{ row }">
            <span style="color:var(--neon-pink)">{{ formatMoney(row.deduction) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实发工资" width="120">
          <template #default="{ row }">
            <span style="color:var(--neon-green);font-weight:600;font-family:var(--font-mono)">
              {{ formatMoney(row.actualSalary) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审批" width="100">
          <template #default="{ row }">
            <el-tag :type="auditType(row.auditStatus)" size="small">
              {{ auditLabel(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 'pending'" text size="small" style="color:var(--neon-green)"
              @click="handleAudit(row)">
              <el-icon><Select /></el-icon> 审批
            </el-button>
            <el-button text size="small" style="color:var(--neon-cyan)" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text size="small" style="color:var(--neon-pink)" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
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

    <!-- 新增/编辑 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑薪资' : '新增薪资'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="员工ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" style="width:100%" placeholder="请输入员工ID" />
        </el-form-item>
        <el-form-item label="薪资月份" prop="salaryMonth">
          <el-date-picker v-model="form.salaryMonth" type="month" placeholder="选择月份"
            value-format="YYYY-MM" style="width:100%" />
        </el-form-item>
        <el-form-item label="基本工资" prop="baseSalary">
          <el-input-number v-model="form.baseSalary" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="绩效" prop="performance">
          <el-input-number v-model="form.performance" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="奖金" prop="bonus">
          <el-input-number v-model="form.bonus" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="扣款" prop="deduction">
          <el-input-number v-model="form.deduction" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="cyber-btn-primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量生成 -->
    <el-dialog v-model="batchVisible" title="批量生成薪资" width="450px">
      <el-form :model="batchForm" label-width="90px">
        <el-form-item label="薪资月份">
          <el-date-picker v-model="batchForm.salaryMonth" type="month" placeholder="选择月份"
            value-format="YYYY-MM" style="width:100%" />
        </el-form-item>
        <el-form-item label="基本工资">
          <el-input-number v-model="batchForm.baseSalary" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="绩效">
          <el-input-number v-model="batchForm.performance" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="奖金">
          <el-input-number v-model="batchForm.bonus" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="扣款">
          <el-input-number v-model="batchForm.deduction" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button class="cyber-btn-primary" @click="handleBatch" :loading="batchSubmitting">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { salaryApi } from '@/api/salary'

const tableData = ref([])
const loading = ref(false)
const statsLoading = ref(false)
const dialogVisible = ref(false)
const batchVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const batchSubmitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const searchUserId = ref(null)
const searchMonth = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const stats = reactive({ totalSalary: 0, avgSalary: 0 })

const form = reactive({
  userId: null, salaryMonth: '', baseSalary: 0, performance: 0, bonus: 0, deduction: 0, remark: ''
})

const batchForm = reactive({ salaryMonth: '', baseSalary: 5000, performance: 0, bonus: 0, deduction: 0, remark: '' })

const rules = {
  userId: [{ required: true, message: '请输入员工ID', trigger: 'blur' }],
  salaryMonth: [{ required: true, message: '请选择月份', trigger: 'change' }],
  baseSalary: [{ required: true, message: '请输入基本工资', trigger: 'blur' }]
}

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
    if (searchUserId.value) params.userId = searchUserId.value
    if (searchMonth.value) params.salaryMonth = searchMonth.value
    const res = await salaryApi.list(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载薪资列表失败', e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  statsLoading.value = true
  try {
    const params = {}
    if (searchMonth.value) params.salaryMonth = searchMonth.value
    const res = await salaryApi.stats(params)
    if (res) {
      stats.totalSalary = res.totalSalary || 0
      stats.avgSalary = res.avgSalary || 0
    }
  } catch (e) {
    console.error('加载统计失败', e)
  } finally {
    statsLoading.value = false
  }
}

const resetForm = () => {
  form.userId = null; form.salaryMonth = ''; form.baseSalary = 0
  form.performance = 0; form.bonus = 0; form.deduction = 0; form.remark = ''
  editingId.value = null
}

const openAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.salaryId
  form.userId = row.userId
  form.salaryMonth = row.salaryMonth
  form.baseSalary = row.baseSalary
  form.performance = row.performance || 0
  form.bonus = row.bonus || 0
  form.deduction = row.deduction || 0
  form.remark = row.remark || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await salaryApi.update(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await salaryApi.create({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
    loadStats()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const openBatch = () => {
  batchForm.salaryMonth = ''
  batchForm.baseSalary = 5000
  batchForm.performance = 0
  batchForm.bonus = 0
  batchForm.deduction = 0
  batchForm.remark = ''
  batchVisible.value = true
}

const handleBatch = async () => {
  if (!batchForm.salaryMonth) { ElMessage.warning('请选择月份'); return }
  batchSubmitting.value = true
  try {
    await salaryApi.batchGenerate({ ...batchForm })
    ElMessage.success('批量生成成功')
    batchVisible.value = false
    loadList()
    loadStats()
  } catch (e) {
    ElMessage.error(e.message || '批量生成失败')
  } finally {
    batchSubmitting.value = false
  }
}

const handleAudit = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认通过「${row.realName || row.userId}」的 ${row.salaryMonth} 薪资吗？`,
      '薪资审批',
      { confirmButtonText: '通过', cancelButtonText: '驳回', type: 'info',
        distinguishCancelAndClose: true }
    )
    await salaryApi.audit(row.salaryId, { auditStatus: 'approved' })
    ElMessage.success('已通过')
    loadList()
  } catch (e) {
    if (e === 'cancel') {
      try {
        await salaryApi.audit(row.salaryId, { auditStatus: 'rejected' })
        ElMessage.success('已驳回')
        loadList()
      } catch (err) {
        ElMessage.error('操作失败')
      }
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该薪资记录吗？`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await salaryApi.delete(row.salaryId)
    ElMessage.success('删除成功')
    loadList()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  loadList()
  loadStats()
})
</script>

<style scoped>
.salary-manage { animation: fadeInUp 0.6s ease-out; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
</style>
