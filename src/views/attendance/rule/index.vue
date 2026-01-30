<template>
  <div class="attendance-rule-container">
    <div class="page-header">
      <h1>考勤规则管理</h1>
      <el-button type="primary" @click="handleAddRule">
        <el-icon><Plus /></el-icon>
        新增规则
      </el-button>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <el-form-item label="规则名称">
          <el-input v-model="queryForm.ruleName" placeholder="请输入规则名称" style="width: 220px;" />
        </el-form-item>
        <el-form-item label="单双休">
          <el-select v-model="queryForm.singleWeekOff" placeholder="请选择" style="width: 220px;">
            <el-option label="单休" :value="true" />
            <el-option label="双休" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 规则列表 -->
    <el-card class="rule-list-card">
      <el-table 
        v-loading="loading" 
        :data="ruleList" 
        style="width: 100%; height: 100%" 
        border
      >
        <el-table-column prop="ruleName" label="规则名称" min-width="120" align="center" />
        <el-table-column label="上班时间" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatTime(row.workInTime) }}
          </template>
        </el-table-column>
        <el-table-column label="下班时间" min-width="100" align="center">
          <template #default="{ row }">
            {{ formatTime(row.workOutTime) }}
          </template>
        </el-table-column>
        <el-table-column label="弹性时间" min-width="100" align="center">
          <template #default="{ row }">
            {{ row.flexibleTimeRange || 0 }} 分钟
          </template>
        </el-table-column>
        <el-table-column label="单双休" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.singleWeekOff ? 'warning' : 'success'">
              {{ row.singleWeekOff ? '单休' : '双休' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="部门" min-width="120" align="center">
          <template #default="{ row }">
            {{ row.departmentName || '全局规则' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditRule(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteRule(row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <rule-dialog
      v-model="dialogVisible"
      :rule-data="currentRule"
      @save="handleSaveRule"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import RuleDialog from './modules/rule-dialog.vue'
import { getAttendanceRules, queryAttendanceRules, deleteAttendanceRule } from '@/api/system-manage'
import type { AttendanceRule } from '@/interface/system'
import { ElMessage, ElMessageBox } from 'element-plus'

// 查询表单
const queryForm = ref({
  ruleName: '',
  singleWeekOff: undefined as boolean | undefined
})

// 规则列表
const ruleList = ref<AttendanceRule[]>([])
const loading = ref(false)

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 弹窗
const dialogVisible = ref(false)
const currentRule = ref<AttendanceRule>({
  id: 0,
  ruleName: '',
  workInTime: '',
  workOutTime: '',
  flexibleTimeRange: 0,
  singleWeekOff: false,
  departmentId: undefined,
  enabled: true
})

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  if (time.length <= 8) return time
  return time.substring(11, 19)
}

// 获取规则列表
const getRuleList = async () => {
  loading.value = true
  try {
    const response: any = await getAttendanceRules()
    ruleList.value = response || []
    pagination.value.total = ruleList.value.length
  } catch (error) {
    console.error('获取规则列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询规则
const handleQuery = async () => {
  loading.value = true
  try {
    const response: any = await queryAttendanceRules({
      ruleName: queryForm.value.ruleName,
      singleWeekOff: queryForm.value.singleWeekOff
    })
    ruleList.value = response || []
    pagination.value.total = ruleList.value.length
  } catch (error) {
    console.error('查询规则失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryForm.value = {
    ruleName: '',
    singleWeekOff: undefined
  }
  getRuleList()
}

// 新增规则
const handleAddRule = () => {
  currentRule.value = {
    id: 0,
    ruleName: '',
    workInTime: '',
    workOutTime: '',
    flexibleTimeRange: 0,
    singleWeekOff: false,
    departmentId: undefined,
    enabled: true
  }
  dialogVisible.value = true
}

// 编辑规则
const handleEditRule = (row: AttendanceRule) => {
  currentRule.value = { ...row }
  dialogVisible.value = true
}

// 删除规则
const handleDeleteRule = (id: number) => {
  ElMessageBox.confirm('确定要删除该规则吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAttendanceRule(id)
      ElMessage.success('删除成功')
      getRuleList()
    } catch (error) {
      console.error('删除规则失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 保存规则
const handleSaveRule = () => {
  dialogVisible.value = false
  getRuleList()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
}

const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
}

// 初始化
onMounted(() => {
  getRuleList()
})
</script>

<style scoped lang="scss">
.attendance-rule-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-shrink: 0;

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

  .rule-list-card {
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
      height: 0; // Prevent flex item from overflowing
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