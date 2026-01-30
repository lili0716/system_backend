<template>
  <div class="form-application-container">
    <div class="page-header">
      <h1>表单申请</h1>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增申请
      </el-button>
    </div>

    <!-- Query -->
    <el-card class="query-card">
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <el-form-item label="通过状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 220px;">
            <el-option label="待审批" :value="0" />
            <el-option label="已审批" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="表单类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 220px;">
            <el-option label="补打卡" :value="1" />
            <el-option label="出差" :value="2" />
            <el-option label="外勤" :value="3" />
            <el-option label="请假" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- List -->
    <el-card class="list-card">
      <el-table v-loading="loading" :data="formList" style="width: 100%; height: 100%" border>
        <el-table-column prop="id" label="ID" min-width="80" align="center" />
        <el-table-column label="表单类型" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getFormTypeType(row.type)">{{ getFormTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" min-width="180" align="center" />
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
             <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" min-width="120" align="center">
           <template #default="{ row }">
             {{ row.approver ? (row.approver.nickName || row.approver.username) : '-' }}
           </template>
        </el-table-column>
         <el-table-column prop="remark" label="备注" min-width="150" align="center" />
         <el-table-column label="操作" min-width="200" align="center" fixed="right">
           <template #default="{ row }">
             <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
             <el-button v-if="row.status === 0" type="danger" size="small" @click="handleRevoke(row)">撤销</el-button>
           </template>
         </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Dialog -->
    <form-dialog
      v-model="dialogVisible"
      :readonly="isView"
      :title="dialogTitle"
      :initial-data="currentForm"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import FormDialog from '@/components/business/FormDialog.vue'
import { getFormList, createPunchCardForm, createBusinessTripForm, createFieldWorkForm, createLeaveForm, revokeForm } from '@/api/system-manage'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const queryForm = ref({
  status: undefined,
  type: undefined
})

const formList = ref([])
const loading = ref(false)
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const isView = ref(false)
const currentForm = ref({})
const dialogTitle = computed(() => isView.value ? '申请详情' : '新增申请')

const getFormTypeType = (type: number) => {
    switch (type) {
        case 1: return ''
        case 2: return 'warning'
        case 3: return 'info'
        case 4: return 'danger'
        default: return ''
    }
}

const getFormTypeName = (type: number) => {
    switch (type) {
        case 1: return '补打卡'
        case 2: return '出差'
        case 3: return '外勤'
        case 4: return '请假'
        default: return '未知'
    }
}

const getStatusType = (status: number) => {
    switch (status) {
        case 0: return 'warning'
        case 1: return 'success'
        case 2: return 'danger'
        default: return 'info'
    }
}

const getStatusName = (status: number) => {
    switch (status) {
        case 0: return '待审批'
        case 1: return '已审批'
        case 2: return '已拒绝'
        default: return '未知'
    }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      applicantId: userStore.info.userId,
      status: queryForm.value.status,
      type: queryForm.value.type
    }
    const res: any = await getFormList(params)
    formList.value = res.rows || []
    pagination.value.total = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.value.currentPage = 1
  fetchList()
}

const resetQuery = () => {
  queryForm.value = {
    status: undefined,
    type: undefined
  }
  fetchList()
}

const handleCreate = () => {
  isView.value = false
  currentForm.value = {}
  dialogVisible.value = true
}

const handleView = (row: any) => {
  isView.value = true
  const data = { ...row }
  // Data Mapping for Display
  if (row.type === 1 && row.punchDate && row.punchTime) {
      data.punchTime = `${row.punchDate} ${row.punchTime}`
  } else if (row.type === 2) {
      data.destination = row.location
      data.startTime = row.startDate 
      data.endTime = row.endDate
      data.reason = row.purpose
  } else if (row.type === 3) {
      data.reason = row.content
      if (row.workDate && row.startTime) data.startTime = `${row.workDate} ${row.startTime}`
      if (row.workDate && row.endTime) data.endTime = `${row.workDate} ${row.endTime}`
  } else if (row.type === 4) {
      if (row.startDate && row.startTime) data.startTime = `${row.startDate} ${row.startTime}`
      if (row.endDate && row.endTime) data.endTime = `${row.endDate} ${row.endTime}`
  }
  // Leave type fields usually match directly
  currentForm.value = data
  dialogVisible.value = true
}

const handleRevoke = (row: any) => {
  ElMessageBox.confirm('确定要撤销这条申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await revokeForm(row.id)
      ElMessage.success('撤销成功')
      fetchList()
    } catch (error) {
      console.error(error)
      // ElMessage handled by http interceptor usually, but safe to log
    }
  })
}

const handleSave = async (data: any) => {
  try {
    data.applicant = { id: userStore.info.userId } // Set applicant
    
    if (data.type === 1) {
      await createPunchCardForm(data)
    } else if (data.type === 2) {
      await createBusinessTripForm(data)
    } else if (data.type === 3) {
      await createFieldWorkForm(data)
    } else if (data.type === 4) {
      await createLeaveForm(data)
    }
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error(error)
    ElMessage.error('提交失败')
  }
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  fetchList()
}

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.form-application-container {
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

  .query-card {
    margin-bottom: 20px;
    flex-shrink: 0;
    
    .demo-form-inline {
        display: flex;
        flex-wrap: wrap;
        .el-form-item:last-child {
            margin-left: auto;
        }
    }
  }

  .list-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding-bottom: 0;
      overflow: hidden;
    }

    .el-table {
      flex: 1;
      height: 0; 
    }

    .pagination-container {
      padding: 15px 0;
      flex-shrink: 0;
      display: flex;
      justify-content: center;
      background-color: var(--el-bg-color-overlay);
      border-top: 1px solid var(--el-border-color-lighter);
    }
  }
}
</style>
