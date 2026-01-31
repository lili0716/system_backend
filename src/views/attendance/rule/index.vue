<!-- 考勤规则管理页面 -->
<template>
  <div class="art-full-height">
    <RuleSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></RuleSearch>

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
            <ElButton type="primary" @click="handleAddRule" v-ripple>
              <el-icon><Plus /></el-icon>
              新增规则
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

    <!-- 编辑弹窗 -->
    <RuleDialog
      v-model="dialogVisible"
      :rule-data="currentRule"
      @save="handleSaveRule"
    />
  </div>
</template>

<script setup lang="ts">
  import { useTable } from '@/hooks/core/useTable'
  import { getAttendanceRules, deleteAttendanceRule } from '@/api/system-manage'
  import RuleSearch from './modules/rule-search.vue'
  import RuleDialog from './modules/rule-dialog.vue'
  import { ElTag, ElMessage, ElMessageBox } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import ArtButtonMore from '@/components/core/forms/art-button-more/index.vue'
  import { ButtonMoreItem } from '@/components/core/forms/art-button-more/index.vue'
  import type { AttendanceRule } from '@/interface/system'

  defineOptions({ name: 'AttendanceRule' })

  // 搜索表单
  const searchForm = ref({
    ruleName: undefined as string | undefined,
    singleWeekOff: undefined as boolean | undefined
  })

  const showSearchBar = ref(true)

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

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    if (!time) return ''
    if (time.length <= 8) return time
    return time.substring(11, 19)
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
      apiFn: getAttendanceRules,
      apiParams: {
        current: 1,
        size: 20
      },
      columnsFactory: () => [
        {
          prop: 'ruleName',
          label: '规则名称',
          minWidth: 120,
          align: 'center'
        },
        {
          prop: 'workInTime',
          label: '上班时间',
          minWidth: 100,
          align: 'center',
          formatter: (row) => formatTime(row.workInTime)
        },
        {
          prop: 'workOutTime',
          label: '下班时间',
          minWidth: 100,
          align: 'center',
          formatter: (row) => formatTime(row.workOutTime)
        },
        {
          prop: 'flexibleTimeRange',
          label: '弹性时间',
          minWidth: 100,
          align: 'center',
          formatter: (row) => `${row.flexibleTimeRange || 0} 分钟`
        },
        {
          prop: 'singleWeekOff',
          label: '单双休',
          minWidth: 80,
          align: 'center',
          formatter: (row) => {
            return h(
              ElTag, 
              { type: row.singleWeekOff ? 'warning' : 'success' }, 
              () => row.singleWeekOff ? '单休' : '双休'
            )
          }
        },
        {
          prop: 'departmentName',
          label: '部门',
          minWidth: 120,
          align: 'center',
          formatter: (row) => row.departmentName || '全局规则'
        },
        {
          prop: 'enabled',
          label: '状态',
          minWidth: 80,
          align: 'center',
          formatter: (row) => {
            return h(
              ElTag, 
              { type: row.enabled ? 'success' : 'info' }, 
              () => row.enabled ? '启用' : '禁用'
            )
          }
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
                key: 'edit',
                label: '编辑规则',
                icon: 'ri:edit-2-line'
              }
            ]
            
            if (!row.isDefault) {
              items.push({
                key: 'delete',
                label: '删除规则',
                icon: 'ri:delete-bin-4-line',
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
        // 考勤规则接口返回的是数组形式
        const list = Array.isArray(response) ? response : (response.data || [])
        return {
          data: list,
          total: list.length
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
  const buttonMoreClick = (item: ButtonMoreItem, row: AttendanceRule) => {
    switch (item.key) {
      case 'edit':
        handleEditRule(row)
        break
      case 'delete':
        handleDeleteRule(row.id!)
        break
    }
  }

  /**
   * 新增规则
   */
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

  /**
   * 编辑规则
   */
  const handleEditRule = (row: AttendanceRule) => {
    currentRule.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除规则
   */
  const handleDeleteRule = (id: number) => {
    ElMessageBox.confirm('确定要删除该规则吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        await deleteAttendanceRule(id)
        ElMessage.success('删除成功')
        refreshData()
      } catch (error) {
        console.error('删除规则失败:', error)
        ElMessage.error('删除失败')
      }
    }).catch(() => {})
  }

  /**
   * 保存规则
   */
  const handleSaveRule = () => {
    dialogVisible.value = false
    refreshData()
  }
</script>