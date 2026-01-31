<template>
  <el-calendar v-model="value" ref="calendar">
    <template #header="{ date }">
      <span>{{ date }}</span>
      <el-button-group>
        <el-button size="small" @click="selectDate('prev-month')" :disabled="button_status">
          {{ $t('calendar.previousMonth') }}
        </el-button>
        <el-button size="small" @click="selectDate('today')">{{ $t('calendar.today') }}</el-button>
      </el-button-group>
    </template>
    <template #date-cell="{ data }">
      <el-popover
        :width="300"
        popper-style="box-shadow: rgb(14 18 22 / 35%) 0px 10px 38px -10px, rgb(14 18 22 / 20%) 0px 10px 20px -15px; padding: 20px;"
      >
        <template #reference>
          <div
            class="flex flex-col items-center justify-center h-full rounded-md"
            @click="handleDateClick(data)"
            :class="getColor(data)"
          >
            <p :class="data.isSelected ? 'is-selected' : ''">
              {{ data.day.split('-').slice(2).join('-') }}
              {{ data.isSelected ? '✔️' : '' }}
            </p>
          </div>
        </template>
        <template #default>
          <div class="grid grid-cols-1 gap-4" style="">
            <el-avatar
              :size="90"
              src="https://avatars.githubusercontent.com/u/72015883?v=4"
              style="margin-bottom: 8px"
            />
            <div>
              <div class="grid grid-cols-4 gap-4" style="margin: 0; font-weight: 500">
                <div
                  class="my-auto"
                  :class="attendanceStatus[data.day]?.checkInTime ? 'col-span-4' : 'col-span-2'"
                >
                  {{ $t('calendar.checkIn')
                  }}{{ attendanceStatus[data.day]?.checkInTime || $t('calendar.noData') }}
                </div>
                <el-button
                  type="primary"
                  @click="handleButtonClick('check_in', data.day)"
                  class="col-span-2"
                  v-if="
                    !attendanceStatus[data.day]?.checkInTime &&
                    attendanceStatus[data.day]?.status !== 'absent' &&
                    isBeforeToday(data.day)
                  "
                >
                  {{ $t('calendar.Replacement') }}
                </el-button>
                <el-button
                  type="primary"
                  @click="handleButtonClick('personal_leave', data.day)"
                  class="col-span-2"
                  v-if="attendanceStatus[data.day]?.status === 'absent' && isBeforeToday(data.day)"
                >
                  {{ $t('calendar.Leave') }}
                </el-button>
              </div>
            </div>
            <div>
              <div class="grid grid-cols-4 gap-4" style="margin: 0; font-weight: 500">
                <!-- @element-plus -->
                <div
                  class="my-auto"
                  :class="attendanceStatus[data.day]?.checkOutTime ? 'col-span-4' : 'col-span-2'"
                >
                  {{ $t('calendar.checkOut')
                  }}{{ attendanceStatus[data.day]?.checkOutTime || $t('calendar.noData') }}
                </div>
                <el-button
                  type="primary"
                  @click="handleButtonClick('check_out', data.day)"
                  class="col-span-2"
                  v-if="
                    !attendanceStatus[data.day]?.checkOutTime &&
                    attendanceStatus[data.day]?.status !== 'absent' &&
                    isBeforeToday(data.day)
                  "
                >
                  {{ $t('calendar.Replacement') }}
                </el-button>
                <el-button
                  type="primary"
                  @click="handleButtonClick('check_all', data.day)"
                  class="col-span-2"
                  v-if="attendanceStatus[data.day]?.status === 'absent' && isBeforeToday(data.day)"
                >
                  {{ $t('calendar.Replacement') }}
                </el-button>
              </div>
            </div>
          </div>
        </template>
      </el-popover>
    </template>
  </el-calendar>
  <ElDialog v-model="dialogVisible" title="Attendance Details" width="600px" @closed="handleReset">
    <ElCard shadow="never" class="art-card-xs">
      <ArtForm
        ref="formRef"
        v-model="formData"
        :items="currentForms"
        :rules="formRules"
        :defaultExpanded="true"
        :labelWidth="labelWidth"
        :labelPosition="labelPosition"
        :span="span"
        :gutter="gutter"
        @reset="handleReset"
        @submit="handleSubmit"
      />
    </ElCard>
  </ElDialog>
</template>

<script lang="ts" setup>
  import { CalendarDateType, CalendarInstance, ElButton, ElDialog, ElPopover } from 'element-plus'
  import { ref } from 'vue'
  import { useSystemInfoStore } from '@/store/modules/systemInfo'
  import { FormItem } from '@/components/core/forms/art-form/index.vue'
  import { isBeforeToday } from '@/utils'
  type AttendanceStatus = Record<
    string,
    {
      status: 'present' | 'absent' | 'late' | 'early_leave' | 'no_check_in' | 'no_check_out'
      checkInTime?: string
      checkOutTime?: string
    }
  >
  type actionTypes = 'check_in' | 'check_out' | 'check_all' | 'personal_leave'
  interface FormData {
    type?: actionTypes
    time?: string
    date_time?: string[]
    remarks?: string
  }
  const formRef = ref()
  const calendar = ref<CalendarInstance>()
  const labelWidth = ref(100)
  const labelPosition = ref<'right' | 'left' | 'top'>('left')
  const span = ref(24)
  const gutter = ref(24)
  const value = ref(new Date())
  const button_status = ref<boolean>(false)
  // 'present' | 'absent' | 'late' | 'early_leave' | 'no_check_in' | 'no_check_out'
  const dialogVisible = ref(false)
  const systemInfoStore = useSystemInfoStore()
  const type_options = systemInfoStore.getLeaveOptions()
  const defaultTime: [Date, Date] = [new Date(2000, 1, 1, 8, 0, 0), new Date(2000, 2, 1, 17, 0, 0)] // '08:00:00', '17:00:00'
  const formData = ref<FormData>({
    type: undefined,
    time: '',
    date_time: [],
    remarks: ''
  })
  /**
   * 表单校验规则
   */
  const formRules = {
    type: [{ required: true, message: '请选择请假类别', trigger: 'change' }],
    time: [{ required: true, message: '请选择补卡时间', trigger: 'change' }],
    date_time: [{ required: true, message: '请选择请假日期', trigger: 'change' }]
  }
  // 表单配置
  const formItems_check = ref([
    {
      label: '请假类别',
      key: 'type',
      type: 'select',
      props: {
        placeholder: '请选择等级',
        options: type_options,
        disabled: true
      }
    },
    // 时间选择
    {
      label: '补卡时间',
      key: 'time',
      type: 'timeselect',
      props: {
        placeholder: '请选择时间',
        type: 'time',
        valueFormat: 'HH:mm:ss',
        start: '08:00',
        step: '00:30',
        end: '23:30'
      }
    },
    // 备注
    {
      label: '备注',
      key: 'remarks',
      type: 'input',
      props: {
        placeholder: '请输入备注',
        type: 'textarea',
        rows: 2
      }
    }
  ])
  const formItems_leave = ref([
    {
      label: '请假类别',
      key: 'type',
      type: 'select',
      props: {
        placeholder: '请选择等级',
        options: type_options
      }
    },
    // 日期时间范围
    {
      label: '请假日期',
      key: 'date_time',
      type: 'datetime',
      props: {
        type: 'datetimerange',
        valueFormat: 'YYYY-MM-DD HH:mm',
        rangeSeparator: '至',
        startPlaceholder: '开始日期时间',
        endPlaceholder: '结束日期时间',
        format: 'YYYY-MM-DD HH:mm',
        defaultTime, // 设置默认时间
        timeFormat: 'HH:mm',
        disableTime: (time: Date) => {
          // Disable times before 8:00 AM and after 5:00 PM
          return time < new Date(2000, 1, 1, 8, 0, 0) || time > new Date(2000, 1, 1, 17, 0, 0)
        }
      }
    },
    // 备注
    {
      label: '备注',
      key: 'remarks',
      type: 'input',
      props: {
        placeholder: '请输入备注',
        type: 'textarea',
        rows: 2
      }
    }
  ])
  const currentForms = ref<FormItem[]>([])
  const attendanceStatus = ref<AttendanceStatus>({
    '2025-12-01': {
      status: 'present',
      checkInTime: '09:00 AM',
      checkOutTime: '05:00 PM'
    },
    '2025-12-02': {
      status: 'late',
      checkInTime: '10:30 AM',
      checkOutTime: '05:00 PM'
    },
    '2025-12-03': {
      status: 'absent'
    },
    '2025-12-04': {
      status: 'early_leave',
      checkInTime: '09:00 AM',
      checkOutTime: '03:00 PM'
    },
    '2025-12-05': {
      status: 'no_check_in',
      checkOutTime: '05:00 PM'
    },
    '2025-12-06': {
      status: 'no_check_out',
      checkInTime: '09:00 AM'
    }
  })
  const handleDateClick = (data: any) => {
    // Toggle selection status
    data.isSelected = !data.isSelected
    // Here you can implement additional logic to mark attendance status
    // For example, you could open a modal to select the status
    console.log(`Date clicked: ${data.day}, Selected: ${data.isSelected}`)
    console.log(data)
    console.log(value.value)
  }
  const handleButtonClick = (type: actionTypes, data: string) => {
    formData.value.type = type
    if (type === 'check_all') {
      // 整天补卡
      formData.value.time = '17:00'
      currentForms.value = formItems_check.value
    } else if (type === 'check_in') {
      //上班补卡，统一补卡为08：:00
      formData.value.time = '08:00'
      currentForms.value = formItems_check.value
    } else if (type === 'check_out') {
      // 下班补卡，可选补卡时间，30分钟为单位
      formData.value.time = '17:00'
      currentForms.value = formItems_check.value
    } else if (type === 'personal_leave') {
      // 补假，默认补当天的卡
      formData.value.date_time = [data + ' 08:00', data + ' 17:00']
      currentForms.value = formItems_leave.value
    } else {
      const n: never = type
      throw new Error(`Unhandled type: ${n}`)
    }
    console.log('Button clicked', type, data)
    dialogVisible.value = true
  }
  const selectDate = (val: CalendarDateType) => {
    if (!calendar.value) return
    if (val === 'prev-month' || button_status.value === false) {
      // 获取上月的考勤信息
      button_status.value = true
    } else if (val === 'today') {
      // 获取本月的考勤信息
      button_status.value = false
    }
    calendar.value.selectDate(val)
  }
  const getColor = (data: any) => {
    const status = attendanceStatus.value[data.day]?.status
    return status ? status : ''
  }
  /**
   * 处理表单重置事件
   */
  const handleReset = (): void => {
    console.log('重置表单')
  }

  /**
   * 处理表单提交事件
   */
  const handleSubmit = async (): Promise<void> => {
    await formRef.value.validate()
    // 沟通后端，补卡或者请假信息上载
    console.log('表单数据', { ...formData.value })
  }
</script>

<style>
  .present {
    /* color: #000000; */
    background-color: var(--color-success);
  }

  .absent {
    background-color: #ff4d4f;
  }

  .late {
    background-color: #faad14;
  }

  .early_leave {
    background-color: #e7a92b;
  }

  .no_check_in {
    background-color: #f0696b;
  }

  .no_check_out {
    background-color: #ff4d4f;
  }
</style>
