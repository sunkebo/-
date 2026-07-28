<template>
  <div class="post-manage fade-in-up">
    <h2 class="page-title">岗位管理</h2>

    <div class="search-bar">
      <el-select v-model="searchDeptId" placeholder="选择部门筛选" clearable style="width:200px" @change="loadList">
        <el-option v-for="d in deptList" :key="d.deptId" :label="d.name" :value="d.deptId" />
      </el-select>
      <el-button type="primary" class="cyber-btn-primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增岗位
      </el-button>
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column prop="name" label="岗位名称" width="160">
          <template #default="{ row }">
            <span style="color:var(--neon-cyan);font-weight:500">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="salaryRange" label="薪资范围" width="140">
          <template #default="{ row }">{{ row.salaryRange || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑岗位' : '新增岗位'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width:100%">
            <el-option v-for="d in deptList" :key="d.deptId" :label="d.name" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="岗位描述" />
        </el-form-item>
        <el-form-item label="薪资范围" prop="salaryRange">
          <el-input v-model="form.salaryRange" placeholder="如：8K-15K" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="cyber-btn-primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { postApi } from '@/api/post'
import { deptApi } from '@/api/dept'

const tableData = ref([])
const deptList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const searchDeptId = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const form = reactive({
  deptId: null,
  name: '',
  description: '',
  salaryRange: '',
  sortOrder: 0
})

const rules = {
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }]
}

const loadDepts = async () => {
  try {
    const res = await deptApi.list()
    deptList.value = res || []
  } catch (e) { console.error('加载部门失败', e) }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchDeptId.value) params.deptId = searchDeptId.value
    const res = await postApi.list(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载岗位列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.deptId = null
  form.name = ''
  form.description = ''
  form.salaryRange = ''
  form.sortOrder = 0
  editingId.value = null
}

const openAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.postId
  try {
    const res = await postApi.get(row.postId)
    form.deptId = res.deptId
    form.name = res.name
    form.description = res.description || ''
    form.salaryRange = res.salaryRange || ''
    form.sortOrder = res.sortOrder || 0
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取岗位信息失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await postApi.update(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await postApi.create({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除岗位「${row.name}」吗？`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await postApi.delete(row.postId)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  loadDepts()
  loadList()
})
</script>

<style scoped>
.post-manage { animation: fadeInUp 0.6s ease-out; }
</style>
