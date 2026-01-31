<!-- 表单审批页面 -->
<template>
  <div class="art-full-height">
    <ApprovalSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></ApprovalSearch>

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
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { getFormList, approveForm } from '@/api/system-manage'
  import ApprovalSearch from './modules/approval-search.vue'
  import { ElTag, ElMessage, ElMessageBox } from 'element-plus'
  import ArtButtonMore from '@/components/core/forms/art-button-more/index.vue'
  import { ButtonMoreItem } from '@/components/core/forms/art-button-more/index.vue'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'FormApproval' })

  const userStore = useUserStore()

  // 搜索表单
  const searchForm = ref({
    dateRange: [] as string[],
    status: 0 as number | undefined,  // Default to Pending
    type: undefined as number | undefined
  })

  const showSearchBar = ref(true)

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
        const { dateRange, ...rest } = params
        const requestParams: any = {
          ...rest,
          approverId: userStore.info.userId
        }
        if (dateRange && dateRange.length === 2) {
          requestParams.startDate = dateRange[0]
          requestParams.endDate = dateRange[1]
        }
        return getFormList(requestParams)
      },
      apiParams: {
        page: 1,
        pageSize: 20,
        status: 0
      },
      excludeParams: ['dateRange'],
      paginationKey: {
        current: 'page',
        size: 'pageSize'
      },
      columnsFactory: () => [
        {
          prop: 'id',
          label: 'ID',
          width: 80,
          align: 'center'
        },
        {
          prop: 'applicant',
          label: '申请人',
          width: 120,
          align: 'center',
          formatter: (row) => {
            return row.applicant ? (row.applicant.nickName || row.applicant.username) : '-'
          }
        },
        {
          prop: 'type',
          label: '表单类型',
          width: 120,
          align: 'center',
          formatter: (row) => {
            return h(ElTag, { type: getFormTypeType(row.type) }, () => getFormTypeName(row.type))
          }
        },
        {
          prop: 'applyTime',
          label: '申请时间',
          width: 180,
          align: 'center'
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 150,
          align: 'center'
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          formatter: (row) => {
            return h(ElTag, { type: getStatusType(row.status) }, () => getStatusName(row.status))
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right',
          align: 'center',
          formatter: (row) => {
            if (row.status === 0) {
              const items: ButtonMoreItem[] = [
                {
                  key: 'approve',
                  label: '通过',
                  icon: 'ri:check-line',
                  color: '#67c23a'
                },
                {
                  key: 'reject',
                  label: '拒绝',
                  icon: 'ri:close-line',
                  color: '#f56c6c'
                }
              ]
              
              return h('div', [
                h(ArtButtonMore, {
                  list: items,
                  onClick: (item: ButtonMoreItem) => buttonMoreClick(item, row)
                })
              ])
            }
            return row.approveComment || '-'
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
      case 'approve':
        handleApprove(row)
        break
      case 'reject':
        handleReject(row)
        break
    }
  }

  /**
   * 审批通过
   */
  const handleApprove = (row: any) => {
    ElMessageBox.prompt('请输入审批意见', '审批通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.*/
    }).then(async ({ value }) => {
      await processApproval(row.id, 1, value || '同意')
    }).catch(() => {})
  }

  /**
   * 审批拒绝
   */
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

  /**
   * 处理审批
   */
  const processApproval = async (id: number, status: number, comment: string) => {
    try {
      await approveForm(id, {
        status,
        comment,
        approverId: userStore.info.userId
      })
      ElMessage.success('操作成功')
      refreshData()
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
</script>
