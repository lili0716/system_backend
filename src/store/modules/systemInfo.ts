/**
 * 系统信息模块
 *
 * 系统信息相关的状态管理
 *
 * @module store/modules/systemInfo
 * @author Art Design Pro Team
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { optionsInterface } from '@/interface/system'
export const useSystemInfoStore = defineStore('systemInfoStore', () => {
  const leave_options = ref<{ label: string; value: string }[]>([
    { label: '上班补卡', value: 'check_in' },
    { label: '下班补卡', value: 'check_out' },
    { label: '整天补卡', value: 'check_all' },
    { label: '事假', value: 'personal_leave' },
    { label: '病假', value: 'sick_leave' },
    { label: '出差', value: 'business_trip' },
    { label: '婚假', value: 'marriage_leave' }
  ])
  const setLeaveOptions = (options: optionsInterface[]) => {
    leave_options.value = options.map((item) => ({ label: item.label, value: item.value }))
  }
  const getLeaveOptions = () => leave_options.value
  return {
    getLeaveOptions,
    setLeaveOptions
  }
})
