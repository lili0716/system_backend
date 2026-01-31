<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { getDepartmentTree, searchEmployees } from '@/api/system-manage'

  interface Props {
    modelValue: Record<string, any>
  }

  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const searchBarRef = ref()

  // 部门树数据
  const departmentTree = ref<any[]>([])
  
  // 员工选项
  const employeeOptions = ref<Array<{label: string, value: string}>>([])
  const employeeLoading = ref(false)

  /**
   * 表单数据双向绑定
   */
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  /**
   * 表单校验规则
   */
  const rules = {}

  /**
   * 异步获取员工列表
   */
  const handleEmployeeSearch = async (query: string) => {
    if (query.length < 1) {
      employeeOptions.value = []
      return
    }
    
    employeeLoading.value = true
    try {
      const res: any = await searchEmployees(query)
      employeeOptions.value = res || []
    } catch (error) {
      console.error('搜索员工失败', error)
    } finally {
      employeeLoading.value = false
    }
  }

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '员工',
      key: 'employeeIds',
      type: 'select',
      props: {
        placeholder: '输入工号或姓名搜索',
        multiple: true,
        filterable: true,
        remote: true,
        reserveKeyword: true,
        remoteMethod: handleEmployeeSearch,
        loading: employeeLoading.value,
        options: employeeOptions.value,
        clearable: true,
        style: { width: '280px' },
        max: 3
      }
    },
    {
      label: '部门',
      key: 'departmentId',
      type: 'tree-select',
      props: {
        placeholder: '请选择部门',
        data: departmentTree.value,
        nodeKey: 'id',
        props: { label: 'name', value: 'id', children: 'children' },
        checkStrictly: true,
        clearable: true,
        style: { width: '200px' }
      }
    },
    {
      label: '日期范围',
      key: 'daterange',
      type: 'datetime',
      props: {
        style: { width: '260px' },
        placeholder: '请选择日期范围',
        type: 'daterange',
        rangeSeparator: '至',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        valueFormat: 'YYYY-MM-DD',
        shortcuts: [
          { text: '今日', value: [new Date(), new Date()] },
          { text: '最近一周', value: [new Date(Date.now() - 604800000), new Date()] },
          { text: '最近一个月', value: [new Date(Date.now() - 2592000000), new Date()] }
        ]
      }
    }
  ])

  /**
   * 加载部门树
   */
  async function loadDepartments() {
    try {
      const res: any = await getDepartmentTree()
      departmentTree.value = res?.nodes || res || []
    } catch (error) {
      console.error('加载部门失败', error)
    }
  }

  /**
   * 处理重置事件
   */
  const handleReset = () => {
    emit('reset')
  }

  /**
   * 处理搜索事件
   * 验证表单后触发搜索
   */
  const handleSearch = async () => {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }

  onMounted(() => {
    loadDepartments()
  })
</script>
