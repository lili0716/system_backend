<!-- 考勤查询页面 -->
<template>
  <div class="art-full-height">
    <AttendanceSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></AttendanceSearch>

    <ElCard
      class="art-table-card"
      shadow="never"
      :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
    >
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton type="success" @click="handleExport" :loading="exportLoading" v-ripple>
              <el-icon><Download /></el-icon>
              导出
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>
    </ElCard>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考勤详情"
      width="600px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="员工姓名">{{ detailData.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ detailData.employeeId }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailData.departmentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ detailData.recordDate }}</el-descriptions-item>
          <el-descriptions-item label="上班时间">{{ detailData.workInTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下班时间">{{ detailData.workOutTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际工时">{{ detailData.actualWorkHours?.toFixed(1) || '0' }} 小时</el-descriptions-item>
          <el-descriptions-item label="考勤状态">
            <el-tag :type="getStatusType(detailData.status)">{{ detailData.statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="迟到分钟数">{{ detailData.lateMinutes || 0 }} 分钟</el-descriptions-item>
          <el-descriptions-item label="早退分钟数">{{ detailData.earlyLeaveMinutes || 0 }} 分钟</el-descriptions-item>
          <el-descriptions-item label="是否已补卡">
            <el-tag :type="detailData.isCorrected ? 'success' : 'info'">
              {{ detailData.isCorrected ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="补卡次数">{{ detailData.correctionCount || 0 }} 次</el-descriptions-item>
          <el-descriptions-item :span="2" label="备注">{{ detailData.displayRemark || detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 异常记录 -->
        <div v-if="detailData.abnormalRecords?.length" class="abnormal-section">
          <h4>异常记录</h4>
          <el-table :data="detailData.abnormalRecords" border size="small">
            <el-table-column prop="abnormalTypeText" label="异常类型" align="center" />
            <el-table-column prop="expectedTime" label="规定时间" align="center" />
            <el-table-column prop="originalTime" label="实际时间" align="center">
              <template #default="{ row }">{{ row.originalTime || '-' }}</template>
            </el-table-column>
            <el-table-column prop="diffMinutes" label="偏差(分钟)" align="center">
              <template #default="{ row }">{{ row.diffMinutes || '-' }}</template>
            </el-table-column>
            <el-table-column label="是否已修正" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isCorrected ? 'success' : 'danger'" size="small">
                  {{ row.isCorrected ? '已修正' : '未修正' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { 
    queryAttendanceRecords, 
    getAttendanceRecordDetail, 
    exportAttendanceRecords 
  } from '@/api/system-manage'
  import AttendanceSearch from './modules/attendance-search.vue'
  import { ElTag, ElMessage } from 'element-plus'
  import { Download } from '@element-plus/icons-vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'

  defineOptions({ name: 'AttendanceQuery' })

  // 搜索表单
  const searchForm = ref({
    employeeIds: [] as string[],
    departmentId: undefined as number | undefined,
    daterange: undefined as string[] | undefined
  })

  const showSearchBar = ref(true)

  // 详情对话框
  const detailDialogVisible = ref(false)
  const detailLoading = ref(false)
  const detailData = ref<any>({})

  // 导出
  const exportLoading = ref(false)

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: queryAttendanceRecords,
      apiParams: {
        page: 1,
        pageSize: 20
      },
      excludeParams: ['daterange'],
      paginationKey: {
        current: 'page',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'employeeName',
          label: '员工姓名',
          minWidth: 100,
          align: 'center'
        },
        {
          prop: 'employeeId',
          label: '工号',
          minWidth: 100,
          align: 'center'
        },
        {
          prop: 'recordDate',
          label: '日期',
          minWidth: 110,
          align: 'center'
        },
        {
          prop: 'status',
          label: '考勤状态',
          minWidth: 90,
          align: 'center',
          formatter: (row) => {
            return h(ElTag, { type: getStatusType(row.status) }, () => row.statusText)
          }
        },
        {
          prop: 'workInTime',
          label: '上班时间',
          minWidth: 100,
          align: 'center',
          formatter: (row) => row.workInTime || '-'
        },
        {
          prop: 'workOutTime',
          label: '下班时间',
          minWidth: 100,
          align: 'center',
          formatter: (row) => row.workOutTime || '-'
        },
        {
          prop: 'actualWorkHours',
          label: '工时',
          minWidth: 80,
          align: 'center',
          formatter: (row) => `${row.actualWorkHours?.toFixed(1) || '0'}h`
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 100,
          align: 'center',
          formatter: (row) => {
            if (row.remark === '补打卡') {
              return h(ElTag, { type: 'warning' }, () => '补打卡')
            }
            return row.remark || '-'
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 100,
          fixed: 'right',
          align: 'center',
          formatter: (row) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'view',
                onClick: () => handleViewDetail(row)
              })
            ])
        }
      ]
    },
    transform: {
      responseAdapter: (response: any) => {
        return {
          data: response.rows || [],
          total: response.total || 0
        }
      }
    }
  })

  /**
   * 搜索处理
   */
  const handleSearch = (params: Record<string, any>) => {
    const { daterange, ...filtersParams } = params
    const [startDate, endDate] = Array.isArray(daterange) ? daterange : [null, null]
    Object.assign(searchParams, { ...filtersParams, startDate, endDate })
    getData()
  }

  /**
   * 状态样式
   */
  function getStatusType(status: number): 'success' | 'warning' | 'danger' | 'primary' | 'info' {
    switch (status) {
      case 0: return 'success'   // 正常
      case 1: return 'warning'   // 迟到
      case 2: return 'warning'   // 早退
      case 3: return 'danger'    // 缺勤
      case 4: return 'primary'   // 加班
      default: return 'info'
    }
  }

  /**
   * 查看详情
   */
  async function handleViewDetail(row: any) {
    detailDialogVisible.value = true
    detailLoading.value = true
    
    try {
      const res: any = await getAttendanceRecordDetail(row.id)
      detailData.value = res
    } catch (error) {
      console.error('获取详情失败', error)
      ElMessage.error('获取详情失败')
    } finally {
      detailLoading.value = false
    }
  }

  /**
   * 导出
   */
  async function handleExport() {
    exportLoading.value = true
    try {
      const { daterange, ...filtersParams } = searchForm.value
      const [startDate, endDate] = Array.isArray(daterange) ? daterange : [null, null]
      
      const params: any = { ...filtersParams, startDate, endDate }
      // 过滤空值
      Object.keys(params).forEach(key => {
        if (params[key] === null || params[key] === '' || params[key] === undefined) {
          delete params[key]
        }
        if (Array.isArray(params[key]) && params[key].length === 0) {
          delete params[key]
        }
      })
      
      const res = await exportAttendanceRecords(params)
      
      // 创建下载链接
      const blob = new Blob([res as BlobPart], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `考勤记录_${new Date().getTime()}.xlsx`
      link.click()
      URL.revokeObjectURL(link.href)
      
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出失败', error)
      ElMessage.error('导出失败，请稍后重试')
    } finally {
      exportLoading.value = false
    }
  }
</script>

<style scoped lang="scss">
.detail-content {
  .abnormal-section {
    margin-top: 20px;

    h4 {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: #303133;
    }
  }
}
</style>
