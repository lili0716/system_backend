<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="500px"
    @update:model-value="handleUpdateShow"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" :disabled="readonly">
      <!-- Common Fields -->
      <el-form-item label="表单类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择表单类型" @change="handleTypeChange">
          <el-option label="补打卡" :value="1" />
          <el-option label="出差" :value="2" />
          <el-option label="外勤" :value="3" />
          <el-option label="请假" :value="4" />
        </el-select>
      </el-form-item>

      <!-- Punch Card Fields -->
      <template v-if="formData.type === 1">
        <el-form-item label="打卡时间" prop="punchTime">
          <el-date-picker
            v-model="formData.punchTime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="打卡地点" prop="location">
          <el-input v-model="formData.location" placeholder="请输入打卡地点" />
        </el-form-item>
      </template>

      <!-- Business Trip Fields -->
      <template v-if="formData.type === 2">
        <el-form-item label="出差地点" prop="destination">
          <el-input v-model="formData.destination" placeholder="请输入出差地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="出差事由" prop="reason">
          <el-input v-model="formData.reason" type="textarea" placeholder="请输入出差事由" />
        </el-form-item>
      </template>

      <!-- Field Work Fields -->
      <template v-if="formData.type === 3">
        <el-form-item label="外勤地点" prop="location">
          <el-input v-model="formData.location" placeholder="请输入外勤地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="外勤事由" prop="reason">
          <el-input v-model="formData.reason" type="textarea" placeholder="请输入外勤事由" />
        </el-form-item>
      </template>

      <!-- Leave Fields -->
      <template v-if="formData.type === 4">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="formData.leaveType" placeholder="请选择请假类型">
             <!-- 1-事假，2-病假，3-产假，4-婚假，5-丧假，6-年假，7-调休 -->
            <el-option label="事假" :value="1" />
            <el-option label="病假" :value="2" />
            <el-option label="婚假" :value="4" />
            <el-option label="年假" :value="6" />
            <el-option label="调休" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
           <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
           <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="请假天数" prop="leaveDays">
          <el-input-number v-model="formData.leaveDays" :precision="1" :step="0.5" :min="0" disabled />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="formData.reason" type="textarea" placeholder="请输入请假原因" />
        </el-form-item>
      </template>
      
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    
    <!-- 审批流程/结果展示 -->
    <div v-if="readonly && formData.status !== 0" class="approval-info">
        <el-divider content-position="left">审批详情</el-divider>
        <el-descriptions :column="1" border>
            <el-descriptions-item label="审批状态">
                <el-tag :type="formData.status === 1 ? 'success' : 'danger'">
                    {{ formData.status === 1 ? '已通过' : '已拒绝' }}
                </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审批人">
                {{ formData.approver ? (formData.approver.nickName || formData.approver.username) : '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="审批时间">
                {{ formData.approveTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="审批意见">
                {{ formData.approveComment || '无' }}
            </el-descriptions-item>
        </el-descriptions>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">{{ readonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readonly" type="primary" @click="handleSave">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    default: '新增申请'
  },
  readonly: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'save'])

const formRef = ref<FormInstance>()
const formData = reactive<any>({
  type: undefined,
  reason: '',
  startTime: '',
  endTime: '',
  days: 1, // for leave
  location: '', // for punch/trip/field
  destination: '', // for trip
  punchTime: '', // for punch
  content: '', // for field
  leaveType: undefined, // for leave
  remark: '', // Common field
  // Approval fields for display
  status: 0,
  approver: null,
  approveTime: null,
  approveComment: ''
})

const resetForm = () => {
    formData.type = undefined
    formData.reason = ''
    formData.startTime = ''
    formData.endTime = ''
    formData.days = 1
    formData.location = ''
    formData.destination = ''
    formData.punchTime = ''
    formData.content = ''
    formData.leaveType = undefined
    formData.remark = ''
    formData.status = 0
    formData.approver = null
    formData.approveTime = null
    formData.approveComment = ''
}

watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.initialData && Object.keys(props.initialData).length > 0) {
        Object.assign(formData, JSON.parse(JSON.stringify(props.initialData)))
    } else {
        resetForm()
    }
  }
})

// Auto calculate leave days
watch([() => formData.startTime, () => formData.endTime], ([start, end]) => {
    if (formData.type === 4 && start && end) {
        const startDate = new Date(start)
        const endDate = new Date(end)
        const diff = endDate.getTime() - startDate.getTime()
        if (diff > 0) {
            // Calculate days (approximate usually 8 hours work day, but simple diff here)
            // User asked for day auto calc. Typically 1 day = 24h or 8h?
            // Assuming simple 24h based calc for now unless specific business rule exists.
            // Or usually: (diff hours) / 8?
            // Let's stick to standard 1 day = 24 hours for generic logic, or better:
            // Just diff / (1000 * 3600 * 24).
            const days = diff / (1000 * 3600 * 24)
            formData.leaveDays = parseFloat(days.toFixed(1))
        } else {
            formData.leaveDays = 0
        }
    }
})

const dialogTitle = computed(() => {
    if (props.readonly) return '申请详情'
    return props.title
})

const rules = reactive<FormRules>({
  type: [{ required: true, message: '请选择表单类型', trigger: 'change' }],
  punchTime: [{ required: true, message: '请选择打卡时间', trigger: 'change' }],
  destination: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入事由', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  days: [{ required: true, message: '请输入天数', trigger: 'blur' }]
})

const handleUpdateShow = (val: boolean) => {
  emit('update:modelValue', val)
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleTypeChange = () => {
    // Only clear type-specific fields, keep common
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid, fields) => {
    if (valid) {
      // Data Transformation
      const payload = { ...formData }
      if (payload.type === 1) { // PunchCard
          if (payload.punchTime) {
              const [date, time] = payload.punchTime.split(' ')
              payload.punchDate = date
              payload.punchTime = time
          }
      } else if (payload.type === 2) { // BusinessTrip
          payload.location = payload.destination
          payload.purpose = payload.reason
          if (payload.startTime) payload.startDate = payload.startTime.split(' ')[0]
          if (payload.endTime) payload.endDate = payload.endTime.split(' ')[0]
      } else if (payload.type === 3) { // FieldWork
          payload.content = payload.reason
          if (payload.startTime) {
              const [date, time] = payload.startTime.split(' ')
              payload.workDate = date
              payload.startTime = time
          }
          if (payload.endTime) {
              payload.endTime = payload.endTime.split(' ')[1]
          }
      } else if (payload.type === 4) { // Leave
          if (payload.startTime) {
              const [date, time] = payload.startTime.split(' ')
              payload.startDate = date
              payload.startTime = time
          }
           if (payload.endTime) {
              const [date, time] = payload.endTime.split(' ')
              payload.endDate = date
              payload.endTime = time
          }
      }
      
      emit('save', payload)
    }
  })
}
</script>
