<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
    width="500px"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <!-- 姓名 -->
      <ElFormItem label="姓名" prop="nickName">
        <ElInput v-model="formData.nickName" placeholder="请输入姓名" />
      </ElFormItem>

      <!-- 性别 -->
      <ElFormItem label="性别" prop="userGender">
        <ElSelect v-model="formData.userGender" placeholder="请选择性别" style="width: 100%">
          <ElOption label="男" value="男" />
          <ElOption label="女" value="女" />
        </ElSelect>
      </ElFormItem>

      <!-- 工号 -->
      <ElFormItem label="工号" prop="employeeId">
        <ElInput v-model="formData.employeeId" placeholder="请输入工号" />
      </ElFormItem>

      <!-- 新增时：入职日期（必填） -->
      <ElFormItem v-if="dialogType === 'add'" label="入职日期" prop="hireDate">
        <ElDatePicker
          v-model="formData.hireDate"
          type="date"
          placeholder="请选择入职日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </ElFormItem>

      <!-- 编辑时：在职状态 -->
      <ElFormItem v-if="dialogType === 'edit'" label="在职状态" prop="status">
        <ElSelect v-model="formData.status" placeholder="请选择在职状态" style="width: 100%">
          <ElOption label="在职" value="1" />
          <ElOption label="离职" value="2" />
        </ElSelect>
      </ElFormItem>

      <!-- 编辑时：离职日期（当状态为离职时显示，必填） -->
      <ElFormItem v-if="dialogType === 'edit' && formData.status === '2'" label="离职日期" prop="leaveDate">
        <ElDatePicker
          v-model="formData.leaveDate"
          type="date"
          placeholder="请选择离职日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </ElFormItem>

      <!-- 备注 -->
      <ElFormItem label="备注" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据
  const formData = reactive({
    id: undefined as number | undefined,
    nickName: '',
    userGender: '男',
    employeeId: '',
    status: '1',
    hireDate: '',
    leaveDate: '',
    remark: ''
  })

  // 表单验证规则
  const rules = computed<FormRules>(() => ({
    nickName: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    userGender: [{ required: true, message: '请选择性别', trigger: 'change' }],
    employeeId: [
      { required: true, message: '请输入工号', trigger: 'blur' }
    ],
    hireDate: dialogType.value === 'add' 
      ? [{ required: true, message: '请选择入职日期', trigger: 'change' }]
      : [],
    status: dialogType.value === 'edit'
      ? [{ required: true, message: '请选择在职状态', trigger: 'change' }]
      : [],
    leaveDate: dialogType.value === 'edit' && formData.status === '2'
      ? [{ required: true, message: '请选择离职日期', trigger: 'change' }]
      : []
  }))

  /**
   * 初始化表单数据
   */
  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.userData
    const row = props.userData

    Object.assign(formData, {
      id: isEdit && row ? row.id : undefined,
      nickName: isEdit && row ? row.nickName || '' : '',
      userGender: isEdit && row ? row.userGender || '男' : '男',
      employeeId: isEdit && row ? row.employeeId || '' : '',
      status: isEdit && row ? row.status || '1' : '1',
      hireDate: isEdit && row && row.hireDate ? row.hireDate : '',
      leaveDate: isEdit && row && row.leaveDate ? row.leaveDate : '',
      remark: isEdit && row ? row.remark || '' : ''
    })
  }

  /**
   * 关闭弹窗时重置表单
   */
  const handleClose = () => {
    formRef.value?.resetFields()
  }

  /**
   * 监听对话框状态变化
   */
  watch(
    () => [props.visible, props.type, props.userData],
    ([visible]) => {
      if (visible) {
        initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate((valid) => {
      if (valid) {
        // TODO: 调用 API 保存用户数据
        console.log('提交数据:', formData)
        ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
        dialogVisible.value = false
        emit('submit')
      }
    })
  }
</script>
