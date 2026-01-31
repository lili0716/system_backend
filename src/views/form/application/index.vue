<!-- 表单申请页面 -->
<template>
  <div class="art-full-height">
    <FormSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></FormSearch>

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
            <ElButton type="primary" @click="handleCreate" v-ripple>
              <el-icon><Plus /></el-icon>
              新增申请
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

    <!-- Dialog -->
    <FormDialog
      v-model="dialogVisible"
      :readonly="isView"
      :title="dialogTitle"
      :initial-data="currentForm"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { getFormList, createPunchCardForm, createBusinessTripForm, createFieldWorkForm, createLeaveForm, revokeForm } from '@/api/system-manage'
  import FormSearch from './modules/form-search.vue'
  import FormDialog from '@/components/business/FormDialog.vue'
  import { ElTag, ElMessage, ElMessageBox } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import ArtButtonMore from '@/components/core/forms/art-button-more/index.vue'
  import { ButtonMoreItem } from '@/components/core/forms/art-button-more/index.vue'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'FormApplication' })

  const userStore = useUserStore()

  // 搜索表单
  const searchForm = ref({
    status: undefined as number | undefined,
    type: undefined as number | undefined
  })

  const showSearchBar = ref(true)

  // 弹窗
  const dialogVisible = ref(false)
  const isView = ref(false)
  const currentForm = ref({})
  const dialogTitle = computed(() => isView.value ? '申请详情' : '新增申请')

  /**
   * 获取表单类型样式
   */
  const getFormTypeType = (type: number) => {
    switch (type) {
      case 1: return ''
      case 2: return 'warning'
      case 3: return 'info'
      case 4: return 'danger'
      default: return ''
    }
  }

  /**
   * 获取表单类型名称
   */
  const getFormTypeName = (type: number) => {
    switch (type) {
      case 1: return '补打卡'
      case 2: return '出差'
      case 3: return '外勤'
      case 4: return '请假'
      default: return '未知'
    }
  }

  /**
   * 获取状态样式
   */
  const getStatusType = (status: number) => {
    switch (status) {
      case 0: return 'warning'
      case 1: return 'success'
      case 2: return 'danger'
      default: return 'info'
    }
  }

  /**
   * 获取状态名称
   */
  const getStatusName = (status: number) => {
    switch (status) {
      case 0: return '待审批'
      case 1: return '已审批'
      case 2: return '已拒绝'
      default: return '未知'
    }
  }

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
      apiFn: async (params: any) => {
        return getFormList({
          ...params,
          applicantId: userStore.info.userId
        })
      },
      apiParams: {
        page: 1,
        pageSize: 20
      },
      paginationKey: {
        current: 'page',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'id',
          label: 'ID',
          minWidth: 80,
          align: 'center'
        },
        {
          prop: 'type',
          label: '表单类型',
          minWidth: 120,
          align: 'center',
          formatter: (row) => {
            return h(ElTag, { type: getFormTypeType(row.type) }, () => getFormTypeName(row.type))
          }
        },
        {
          prop: 'applyTime',
          label: '申请时间',
          minWidth: 180,
          align: 'center'
        },
        {
          prop: 'status',
          label: '状态',
          minWidth: 100,
          align: 'center',
          formatter: (row) => {
            return h(ElTag, { type: getStatusType(row.status) }, () => getStatusName(row.status))
          }
        },
        {
          prop: 'approver',
          label: '审批人',
          minWidth: 120,
          align: 'center',
          formatter: (row) => {
            return row.approver ? (row.approver.nickName || row.approver.username) : '-'
          }
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 150,
          align: 'center'
        },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          align: 'center',
          formatter: (row) => {
            const items: ButtonMoreItem[] = [
              {
                key: 'view',
                label: '查看详情',
                icon: 'ri:eye-line'
              }
            ]
            
            if (row.status === 0) {
              items.push({
                key: 'revoke',
                label: '撤销申请',
                icon: 'ri:close-circle-line',
                color: '#f56c6c'
              })
            }
            
            return h('div', [
              h(ArtButtonMore, {
                list: items,
                onClick: (item: ButtonMoreItem) => buttonMoreClick(item, row)
              })
            ])
          }
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
    Object.assign(searchParams, params)
    getData()
  }

  /**
   * 操作按钮点击
   */
  const buttonMoreClick = (item: ButtonMoreItem, row: any) => {
    switch (item.key) {
      case 'view':
        handleView(row)
        break
      case 'revoke':
        handleRevoke(row)
        break
    }
  }

  /**
   * 新增申请
   */
  const handleCreate = () => {
    isView.value = false
    currentForm.value = {}
    dialogVisible.value = true
  }

  /**
   * 查看详情
   */
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
    currentForm.value = data
    dialogVisible.value = true
  }

  /**
   * 撤销申请
   */
  const handleRevoke = (row: any) => {
    ElMessageBox.confirm('确定要撤销这条申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await revokeForm(row.id)
        ElMessage.success('撤销成功')
        refreshData()
      } catch (error) {
        console.error(error)
      }
    })
  }

  /**
   * 保存申请
   */
  const handleSave = async (data: any) => {
    try {
      data.applicant = { id: userStore.info.userId }
      
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
      refreshData()
    } catch (error) {
      console.error(error)
      ElMessage.error('提交失败')
    }
  }
</script>
