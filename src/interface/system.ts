export interface optionsInterface {
  label: string
  value: string
}

// 考勤规则接口
export interface AttendanceRule {
  id: number
  ruleName: string
  description?: string
  workInTime: string
  workOutTime: string
  flexibleTimeRange: number
  singleWeekOff: boolean
  departmentId?: number
  departmentName?: string
  enabled: boolean
  isDefault?: boolean
  createTime?: string
  updateTime?: string
}

// 部门接口
export interface Department {
  id: number
  name: string
  parentId: number
  children?: Department[]
}
