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
   * 状态选项
   */
  const statusOptions = ref([
    { label: '待审批', value: 0 },
    { label: '已审批', value: 1 },
    { label: '已拒绝', value: 2 }
  ])

  /**
   * 表单类型选项
   */
  const typeOptions = ref([
    { label: '补打卡', value: 1 },
    { label: '出差', value: 2 },
    { label: '外勤', value: 3 },
    { label: '请假', value: 4 }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '申请时间',
      key: 'dateRange',
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
    },
    {
      label: '表单类型',
      key: 'type',
      type: 'select',
      props: {
        placeholder: '全部',
        options: typeOptions.value,
        clearable: true
      }
    },
    {
      label: '审批状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '全部',
        options: statusOptions.value,
        clearable: true
      }
    }
  ])

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
</script>
