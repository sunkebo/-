<template>
  <div class="user-manage fade-in-up">
    <h2 class="page-title">员工管理</h2>

    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索姓名/手机号" clearable style="width:220px"
        @keyup.enter="loadList" @clear="loadList" />
      <el-select v-model="searchDeptId" placeholder="部门筛选" clearable style="width:180px" @change="loadList">
        <el-option v-for="d in deptList" :key="d.deptId" :label="d.name" :value="d.deptId" />
      </el-select>
      <el-button type="primary" class="cyber-btn-primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增员工
      </el-button>
    </div>

    <div class="holo-panel corner-decor" style="padding:20px">
      <el-table :data="tableData" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="userId" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="130">
          <template #default="{ row }">
            <span style="font-family:var(--font-mono);color:var(--neon-cyan)">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="deptName" label="部门" width="120">
          <template #default="{ row }">{{ row.deptName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="postName" label="岗位" width="120">
          <template #default="{ row }">{{ row.postName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="userType" label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.userType === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.userType === 'admin' ? '管理员' : '员工' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" style="color:var(--neon-cyan)" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button text size="small" style="color:var(--neon-orange)" @click="handleResetPwd(row)">
              <el-icon><Key /></el-icon>
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
      :title="isEdit ? '编辑员工' : '新增员工'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="电子邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-select v-model="form.deptId" placeholder="请选择部门" style="width:100%">
                <el-option v-for="d in deptList" :key="d.deptId" :label="d.name" :value="d.deptId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="postId">
              <el-select v-model="form.postId" placeholder="请选择岗位" style="width:100%">
                <el-option v-for="p in postList" :key="p.postId" :label="p.name" :value="p.postId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker v-model="form.hireDate" type="date" placeholder="选择日期" style="width:100%"
                value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="userType">
              <el-select v-model="form.userType" style="width:100%" :disabled="isEdit">
                <el-option label="员工" value="employee" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button class="cyber-btn-primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建（默认密码123456）' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { deptApi } from '@/api/dept'
import { postApi } from '@/api/post'

const tableData = ref([])
const deptList = ref([])
const postList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const searchKeyword = ref('')
const searchDeptId = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const form = reactive({
  realName: '',
  gender: 1,
  phone: '',
  email: '',
  deptId: null,
  postId: null,
  hireDate: '',
  userType: 'employee'
})

const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  postId: [{ required: true, message: '请选择岗位', trigger: 'change' }]
}

const loadDepts = async () => {
  try { deptList.value = await deptApi.list() || [] } catch (e) {}
}

const loadPosts = async () => {
  try {
    const res = await postApi.list({ page: 1, size: 100 })
    postList.value = res?.records || []
  } catch (e) {}
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchDeptId.value) params.deptId = searchDeptId.value
    const res = await userApi.list(params)
    tableData.value = res?.records || []
    total.value = res?.total || 0
  } catch (e) {
    console.error('加载员工列表失败', e)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.realName = ''
  form.gender = 1
  form.phone = ''
  form.email = ''
  form.deptId = null
  form.postId = null
  form.hireDate = ''
  form.userType = 'employee'
  editingId.value = null
}

const openAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const openEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.userId
  try {
    const res = await userApi.get(row.userId)
    form.realName = res.realName || ''
    form.gender = res.gender !== undefined ? res.gender : 1
    form.phone = res.phone || ''
    form.email = res.email || ''
    form.deptId = res.deptId
    form.postId = res.postId
    form.hireDate = res.hireDate || ''
    form.userType = res.userType || 'employee'
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取员工信息失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 过滤空字符串，避免 Jackson 反序列化 LocalDate 报错
    const payload = { ...form }
    if (!payload.hireDate) delete payload.hireDate
    if (!payload.email) delete payload.email
    if (!payload.phone) delete payload.phone

    if (isEdit.value) {
      await userApi.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(payload)
      ElMessage.success('创建成功，默认密码为 123456')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置「${row.realName}」的密码吗？密码将重置为 123456`,
      '重置密码',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
    await userApi.resetPassword(row.userId)
    ElMessage.success('密码已重置为 123456')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '重置失败')
  }
}

const handleStatus = async (row, val) => {
  try {
    await userApi.updateStatus(row.userId, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(val ? '已启用' : '已停用')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工「${row.realName}」吗？`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await userApi.delete(row.userId)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => {
  loadDepts()
  loadPosts()
  loadList()
})
</script>

<style scoped>
.user-manage { animation: fadeInUp 0.6s ease-out; }
</style>
