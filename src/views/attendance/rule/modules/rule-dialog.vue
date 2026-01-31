<template>
  <el-dialog
    v-model="dialogVisible"
    :title="ruleData.id ? '编辑考勤规则' : '新增考勤规则'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="ruleFormRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="vertical-form"
    >
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
      </el-form-item>

      <el-form-item label="上班时间" prop="workInTime">
        <el-time-picker
          v-model="formData.workInTime"
          type="time"
          format="HH:mm"
          value-format="HH:mm:ss"
          placeholder="请选择上班时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="下班时间" prop="workOutTime">
        <el-time-picker
          v-model="formData.workOutTime"
          type="time"
          format="HH:mm"
          value-format="HH:mm:ss"
          placeholder="请选择下班时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="弹性时间" prop="flexibleTimeRange">
        <el-input-number
          v-model="formData.flexibleTimeRange"
          :min="0"
          :max="999"
          placeholder="请输入弹性打卡时间范围"
          style="width: 100%"
        />
        <div class="form-hint">单位：分钟</div>
      </el-form-item>

      <el-form-item label="单双休" prop="singleWeekOff">
        <el-switch
          v-model="formData.singleWeekOff"
          active-text="单休"
          inactive-text="双休"
          active-value="true"
          inactive-value="false"
        />
      </el-form-item>

      <el-form-item label="部门" prop="departmentId">
        <el-select
          v-model="formData.departmentId"
          placeholder="请选择部门（为空表示全局规则）"
          style="width: 100%"
          clearable
        >
          <el-option
            v-for="dept in departmentList"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="enabled">
        <el-switch
          v-model="formData.enabled"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>

      <el-form-item label="规则描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          placeholder="请输入规则描述"
          :rows="3"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { createAttendanceRule, updateAttendanceRule } from '@/api/system-manage'
import type { AttendanceRule, Department } from '@/interface/system'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  ruleData: {
    type: Object as () => AttendanceRule,
    default: () => ({
      id: 0,
      ruleName: '',
      workInTime: '',
      workOutTime: '',
      flexibleTimeRange: 0,
      singleWeekOff: false,
      departmentId: undefined,
      enabled: true
    })
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'save'])

// 弹窗可见性
const dialogVisible = ref(props.modelValue)

// 表单数据
const formData = reactive<AttendanceRule>({
  id: 0,
  ruleName: '',
  description: '',
  workInTime: '',
  workOutTime: '',
  flexibleTimeRange: 0,
  singleWeekOff: false,
  departmentId: undefined,
  enabled: true
})

// 表单验证规则
const rules = reactive({
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  workInTime: [
    { required: true, message: '请选择上班时间', trigger: 'change' }
  ],
  workOutTime: [
    { required: true, message: '请选择下班时间', trigger: 'change' }
  ],
  flexibleTimeRange: [
    { required: true, message: '请输入弹性时间', trigger: 'blur' }
  ]
})

// 表单引用
const ruleFormRef = ref()

// 部门列表
const departmentList = ref<Department[]>([])

// 监听弹窗可见性
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
})

// 监听规则数据变化
watch(() => props.ruleData, (newVal) => {
  if (newVal) {
    Object.assign(formData, newVal)
  }
}, { deep: true })

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
}

// 保存规则
const handleSave = async () => {
  if (!ruleFormRef.value) return

  try {
    await ruleFormRef.value.validate()
    
    if (formData.id) {
      // 更新规则
      await updateAttendanceRule(formData.id, formData)
    } else {
      // 新增规则
      await createAttendanceRule(formData)
    }
    
    ElMessage.success('保存成功')
    emit('save')
    emit('update:modelValue', false)
  } catch (error) {
    console.error('保存规则失败:', error)
    ElMessage.error('保存失败')
  }
}

// 获取部门列表
const getDepartments = async () => {
  try {
    // 这里需要调用实际的部门API
    // 暂时使用模拟数据
    departmentList.value = [
      { id: 1, name: '技术部', parentId: 0 },
      { id: 2, name: '产品部', parentId: 0 },
      { id: 3, name: '设计部', parentId: 0 },
      { id: 4, name: '运营部', parentId: 0 }
    ]
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  getDepartments()
})
</script>

<style scoped>
.vertical-form {
  margin-top: 20px;
}

.form-hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>