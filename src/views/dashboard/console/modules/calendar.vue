<template>
  <el-calendar v-model="value">
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
                  >{{ $t('calendar.checkIn')
                  }}{{ attendanceStatus[data.day]?.checkInTime || $t('calendar.noData') }}</div
                >
                <el-button
                  type="primary"
                  @click="handleButtonClick('check_in', data.day)"
                  class="col-span-2"
                  v-if="
                    !attendanceStatus[data.day]?.checkInTime &&
                    attendanceStatus[data.day]?.status !== 'absent'
                  "
                >
                  {{ $t('calendar.Replacement') }}
                </el-button>
                <el-button
                  type="primary"
                  @click="handleButtonClick('leave', data.day)"
                  class="col-span-2"
                  v-if="attendanceStatus[data.day]?.status === 'absent'"
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
                  >{{ $t('calendar.checkOut')
                  }}{{ attendanceStatus[data.day]?.checkOutTime || $t('calendar.noData') }}</div
                >
                <el-button
                  type="primary"
                  @click="handleButtonClick('check_out', data.day)"
                  class="col-span-2"
                  v-if="
                    !attendanceStatus[data.day]?.checkOutTime &&
                    attendanceStatus[data.day]?.status !== 'absent'
                  "
                >
                  {{ $t('calendar.Replacement') }}
                </el-button>
                <el-button
                  type="primary"
                  @click="handleButtonClick('check_all', data.day)"
                  class="col-span-2"
                  v-if="attendanceStatus[data.day]?.status === 'absent'"
                >
                  {{ $t('calendar.Replacement') }}
                </el-button>
              </div>
            </div>
          </div>
          <ElDialog v-model="dialogVisible" title="Attendance Details" width="500px"> </ElDialog>
        </template>
      </el-popover>
    </template>
  </el-calendar>
</template>

<script lang="ts" setup>
  import { ElButton, ElDialog, ElPopover } from 'element-plus'
  import { ref } from 'vue'
  //   interface AttendanceStatus {
  //     [key: string]: {
  //       status: 'present' | 'absent' | 'late' | 'early_leave' | 'no_check_in' | 'no_check_out'
  //       checkInTime?: string
  //       checkOutTime?: string
  //     }
  //   }
  type AttendanceStatus = Record<
    string,
    {
      status: 'present' | 'absent' | 'late' | 'early_leave' | 'no_check_in' | 'no_check_out'
      checkInTime?: string
      checkOutTime?: string
    }
  >
  type actionTypes = 'check_in' | 'check_out' | 'check_all' | 'leave'
  const value = ref(new Date())
  // 'present' | 'absent' | 'late' | 'early_leave' | 'no_check_in' | 'no_check_out'
  const dialogVisible = ref(false)
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
    dialogVisible.value = true
    console.log('Button clicked', type, data)
  }

  const getColor = (data: any) => {
    const status = attendanceStatus.value[data.day]?.status
    return status ? status : ''
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
