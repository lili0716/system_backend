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
      label: '通过状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '全部',
        options: statusOptions.value,
        clearable: true
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
