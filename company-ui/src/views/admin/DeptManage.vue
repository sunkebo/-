<template>
  <div class="dept-manage fade-in-up">
    <h2 class="page-title">部门管理</h2>

    <div class="search-bar">
      <el-button type="primary" class="cyber-btn-primary" @click="openAdd(null)">
        <el-icon><Plus /></el-icon> 新增部门
      </el-button>
      <el-button @click="loadTree" style="background:transparent;border:1px solid var(--border-color);color:var(--text-secondary)">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table
        :data="treeData"
        row-key="deptId"
        border
        stripe
        style="width:100%"
        default-expand-all
        v-loading="loading"
      >
        <el-table-column prop="name" label="部门名称" min-width="180">
          <template #default="{ row }">
            <span style="color:var(--neon-cyan);font-weight:500">
              <el-icon v-if="row.children && row.children.length"><Folder /></el-icon>
              <el-icon v-else><Document /></el-icon>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="deptId" label="ID" width="80" />
        <el-table-column prop="leader" label="负责人" width="120">
          <template #default="{ row }">{{ row.leader || '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openAdd(row)">
              <el-icon><Plus /></el-icon> 子部门
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
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑部门' : '新增部门'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptOptions"
            :props="{ label: 'name', value: 'deptId', children: 'children' }"
            placeholder="不选则为顶级部门"
            check-strictly
            clearable
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="部门描述" />
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
import { deptApi } from '@/api/dept'

const treeData = ref([])
const deptOptions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const form = reactive({
  parentId: null,
  name: '',
  leader: '',
  description: '',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const loadTree = async () => {
  loading.value = true
  try {
    const res = await deptApi.tree()
    treeData.value = res || []
    deptOptions.value = res || []
  } catch (e) {
    console.error('加载部门树失败', e)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.parentId = null
  form.name = ''
  form.leader = ''
  form.description = ''
  form.sortOrder = 0
  editingId.value = null
}

const openAdd = (parent) => {
  resetForm()
  isEdit.value = false
  if (parent) form.parentId = parent.deptId
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.deptId
  try {
    const res = await deptApi.get(row.deptId)
    form.parentId = res.parentId
    form.name = res.name
    form.leader = res.leader || ''
    form.description = res.description || ''
    form.sortOrder = res.sortOrder || 0
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取部门信息失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await deptApi.update(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await deptApi.create({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTree()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门「${row.name}」吗？若存在子部门或员工则无法删除。`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deptApi.delete(row.deptId)
    ElMessage.success('删除成功')
    loadTree()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => loadTree())
</script>

<style scoped>
.dept-manage { animation: fadeInUp 0.6s ease-out; }
</style>
