<template>
  <div class="system-dept-container">
    <div class="page-header">
      <h1>部门管理</h1>
      <el-button type="primary" @click="handleCreate(null)">
        <el-icon><Plus /></el-icon>
        新增一级部门
      </el-button>
    </div>

    <!-- Tree Table -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="deptTree"
        style="width: 100%; height: 100%"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="code" label="部门编码" min-width="120" align="center" />
        <el-table-column prop="leaderName" label="负责人" width="120" align="center">
            <template #default="{ row }">
                {{ row.leaderName || '-' }}
            </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleCreate(row)">新增下级</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="上级部门">
             <el-tree-select
                v-model="formData.parentId"
                :data="deptTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                value-key="id"
                placeholder="作为一级部门"
                check-strictly
                clearable
                style="width: 100%"
              />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入部门编码" />
        </el-form-item>
         <el-form-item label="负责人">
             <el-select
                v-model="formData.leaderId"
                filterable
                remote
                placeholder="请输入并选择负责人"
                :remote-method="searchLeaders"
                :loading="leaderLoading"
                @change="handleLeaderChange"
                style="width: 100%"
                v-infinite-scroll="loadMoreLeaders"
                :infinite-scroll-disabled="!leaderQuery.hasMore"
             >
                <el-option
                    v-for="item in leaderOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
             </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="formData.enabled" active-text="启用" inactive-text="禁用" /> 
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getDepartmentTree, createDepartment, updateDepartment, deleteDepartment, searchUsers } from '@/api/system-manage'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const deptTree = ref([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

// Leader Search
const leaderOptions = ref<any[]>([])
const leaderLoading = ref(false)
const leaderQuery = reactive({
    keyword: '',
    page: 1,
    size: 50,
    hasMore: true
})

const searchLeaders = async (keyword: string) => {
    leaderLoading.value = true
    leaderQuery.keyword = keyword
    leaderQuery.page = 1
    leaderQuery.hasMore = true
    leaderOptions.value = []
    
    try {
        const res: any = await searchUsers({
            keyword: leaderQuery.keyword,
            page: leaderQuery.page,
            size: leaderQuery.size
        })
        const records = res.records || []
        leaderOptions.value = records.map((u: any) => ({
            value: u.id, // Store ID directly
            label: `${u.nickName} (${u.username})`, // Show Name + Account
            ...u
        }))
        if (records.length < leaderQuery.size) {
            leaderQuery.hasMore = false
        }
    } catch (e) {
        console.error(e)
    } finally {
        leaderLoading.value = false
    }
}

const loadMoreLeaders = async () => {
    if (!leaderQuery.hasMore || leaderLoading.value) return
    leaderLoading.value = true
    leaderQuery.page++
    try {
         const res: any = await searchUsers({
            keyword: leaderQuery.keyword,
            page: leaderQuery.page,
            size: leaderQuery.size
        })
        const records = res.records || []
        const newOptions = records.map((u: any) => ({
            value: u.id,
            label: `${u.nickName} (${u.username})`,
            ...u
        }))
        leaderOptions.value.push(...newOptions)
        if (records.length < leaderQuery.size) {
            leaderQuery.hasMore = false
        }
    } catch (e) {
        console.error(e)
    } finally {
        leaderLoading.value = false
    }
}

const handleLeaderChange = (val: any) => {
    const selected = leaderOptions.value.find(item => item.value === val)
    if (selected) {
        formData.leaderId = selected.id
        formData.leaderName = selected.nickName
    } else {
        formData.leaderId = undefined
        formData.leaderName = ''
    }
}

const formData = reactive({
  id: undefined,
  parentId: undefined,
  name: '',
  code: '',
  sort: 0,
  enabled: true,
  description: '',
  leaderName: '',
  leaderId: undefined
})

const isEdit = computed(() => !!formData.id)
const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
})

const fetchTree = async () => {
  loading.value = true
  try {
    const res: any = await getDepartmentTree()
    deptTree.value = res.nodes || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
    formData.id = undefined
    formData.parentId = undefined
    formData.name = ''
    formData.code = ''
    formData.sort = 0
    formData.enabled = true
    formData.description = ''
    formData.leaderName = ''
    formData.leaderId = undefined
    formRef.value?.resetFields()
    leaderOptions.value = []
}

const handleCreate = (row: any) => {
  resetForm()
  searchLeaders('') // Preload first 50
  if (row) {
      formData.parentId = row.id
  }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  resetForm()
  searchLeaders('') // Preload first 50 - ideally user's current leader also should be there.
  Object.assign(formData, row)
  // If row has leader info, push it to options if not present ??
  if (row.leaderId && row.leaderName) {
      // Just mock push it so it shows up? Or reload search with keyword?
      // Pushing manually to ensure it displays correctly even if not in first 50
      if (!leaderOptions.value.find(o => o.value === row.leaderId)) {
        leaderOptions.value.unshift({
            value: row.leaderId,
            label: `${row.leaderName} (ID:${row.leaderId})`, // Fallback format
            id: row.leaderId,
            nickName: row.leaderName
        })
      }
  }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该部门吗? 如果包含下级部门将无法删除。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
      fetchTree()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload = { ...formData }
        // Provide parent object structure for backend JPA if creating/updating
        if (payload.parentId) {
             // @ts-ignore
             payload.parent = { id: payload.parentId } 
        } else {
             // @ts-ignore
             payload.parent = null
        }
        
        if (isEdit.value) {
          await updateDepartment(payload.id!, payload)
        } else {
          await createDepartment(payload)
        }
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchTree()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  fetchTree()
})
</script>

<style scoped lang="scss">
.system-dept-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      font-size: 24px;
      font-weight: bold;
      margin: 0;
    }
  }

  .list-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-card__body) {
      flex: 1;
      height: 100%;
      padding: 0;
    }
  }
}
</style>
