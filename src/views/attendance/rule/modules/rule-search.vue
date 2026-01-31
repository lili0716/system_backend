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
   * 单双休选项
   */
  const weekOffOptions = ref([
    { label: '单休', value: true },
    { label: '双休', value: false }
  ])

  /**
   * 搜索表单配置项
   */
  const formItems = computed(() => [
    {
      label: '规则名称',
      key: 'ruleName',
      type: 'input',
      placeholder: '请输入规则名称',
      clearable: true
    },
    {
      label: '单双休',
      key: 'singleWeekOff',
      type: 'select',
      props: {
        placeholder: '请选择',
        options: weekOffOptions.value,
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
