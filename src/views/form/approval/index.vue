<template>
  <div class="form-approval-container">
    <div class="page-header">
      <h1>表单审批</h1>
    </div>

    <!-- Query -->
    <el-card class="query-card">
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <el-form-item label="申请时间">
           <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
          />
        </el-form-item>
        <el-form-item label="表单类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 180px;">
            <el-option label="补打卡" :value="1" />
            <el-option label="出差" :value="2" />
            <el-option label="外勤" :value="3" />
            <el-option label="请假" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 180px;">
            <el-option label="待审批" :value="0" />
            <el-option label="已审批" :value="1" />
            <el-option label="已拒绝" :value="2" />
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
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="申请人" width="120" align="center">
           <template #default="{ row }">
             {{ row.applicant ? (row.applicant.nickName || row.applicant.username) : '-' }}
           </template>
        </el-table-column>
        <el-table-column label="表单类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getFormTypeType(row.type)">{{ getFormTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" align="center" />
        <el-table-column prop="remark" label="备注" min-width="150" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
             <el-tag :type="getStatusType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
                <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <span v-else>{{ row.approveComment || '-' }}</span>
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

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFormList, approveForm } from '@/api/system-manage'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const queryForm = ref({
  dateRange: [],
  status: 0, // Default to Pending
  type: undefined
})

const formList = ref([])
const loading = ref(false)
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

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
    const params: any = {
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      approverId: userStore.info.userId, // Only show forms assigned to me
      status: queryForm.value.status,
      type: queryForm.value.type
    }
    
    if (queryForm.value.dateRange && queryForm.value.dateRange.length === 2) {
        params.startDate = queryForm.value.dateRange[0]
        params.endDate = queryForm.value.dateRange[1]
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
    dateRange: [],
    status: undefined,
    type: undefined
  }
  fetchList()
}

const handleApprove = (row: any) => {
    ElMessageBox.prompt('请输入审批意见', '审批通过', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.*/, 
  }).then(async ({ value }) => {
     await processApproval(row.id, 1, value || '同意')
  }).catch(() => {})
}

const handleReject = (row: any) => {
   ElMessageBox.prompt('请输入拒绝原因', '审批拒绝', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S/, 
    inputErrorMessage: '请输入拒绝原因'
  }).then(async ({ value }) => {
     await processApproval(row.id, 2, value)
  }).catch(() => {})
}

const processApproval = async (id: number, status: number, comment: string) => {
    try {
        await approveForm(id, {
            status,
            comment,
            approverId: userStore.info.userId
        })
        ElMessage.success('操作成功')
        fetchList()
    } catch (error) {
        console.error(error)
        ElMessage.error('操作失败')
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
.form-approval-container {
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
