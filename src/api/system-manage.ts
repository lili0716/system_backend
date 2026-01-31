import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

// 获取用户列表
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserList>({
    url: '/api/user/list',
    params
  })
}


export function searchUsers(params: any) {
  return request.get({
    url: '/api/users/search',
    params
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params
  })
}

// 获取菜单列表
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/routes'
  })
}

// 考勤规则管理
export function getAttendanceRules() {
  return request.get({
    url: '/api/attendance/rules'
  })
}

export function getAttendanceRuleById(id: number) {
  return request.get({
    url: `/api/attendance/rules/${id}`
  })
}

export function createAttendanceRule(data: any) {
  return request.post({
    url: '/api/attendance/rules',
    data
  })
}

export function updateAttendanceRule(id: number, data: any) {
  return request.put({
    url: `/api/attendance/rules/${id}`,
    data
  })
}

export function deleteAttendanceRule(id: number) {
  return request.del({
    url: `/api/attendance/rules/${id}`
  });
}

export function queryAttendanceRules(params: any) {
  return request.post({
    url: '/api/attendance/rules/query',
    data: params
  })
}

// 表单管理
export function getFormList(params: any) {
  return request.post({
    url: '/api/forms/list',
    data: params
  })
}

export function createPunchCardForm(data: any) {
  return request.post({
    url: '/api/forms/punch-card',
    data
  })
}

export function createBusinessTripForm(data: any) {
  return request.post({
    url: '/api/forms/business-trip',
    data
  })
}

export function createFieldWorkForm(data: any) {
  return request.post({
    url: '/api/forms/field-work',
    data
  })
}

export function createLeaveForm(data: any) {
  return request.post({
    url: '/api/forms/leave',
    data
  })
}

export function approveForm(id: number, data: any) {
  return request.post({
    url: `/api/forms/${id}/approve`,
    data
  })
}
export function revokeForm(id: number) {
  return request.post({
    url: `/api/forms/${id}/revoke`
  })
}

// 部门管理
export function getDepartmentTree() {
  return request.get({
    url: '/api/departments/tree'
  })
}

export function createDepartment(data: any) {
  return request.post({
    url: '/api/departments',
    data
  })
}

export function updateDepartment(id: number, data: any) {
  return request.put({
    url: `/api/departments/${id}`,
    data
  })
}

export function deleteDepartment(id: number) {
  return request.del({
    url: `/api/departments/${id}`
  })
}

// 考勤文件上传和异常记录管理
export function uploadAttendanceFile(file: File, uploaderId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('uploaderId', uploaderId.toString())
  return request.post({
    url: '/api/attendance/files/upload',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getUncorrectedAbnormalRecords(userId: number) {
  return request.get({
    url: `/api/attendance/abnormal-records/uncorrected/${userId}`
  })
}

export function getAbnormalRecordsByUserId(userId: number) {
  return request.get({
    url: `/api/attendance/abnormal-records/user/${userId}`
  })
}

export function exportFailedRecords(failedRecords: any[]) {
  return request.post({
    url: '/api/attendance/files/failed-export',
    data: failedRecords,
    responseType: 'blob'
  })
}

// 考勤查询相关API
export function queryAttendanceRecords(params: any) {
  return request.post({
    url: '/api/attendance/records/query',
    data: params
  })
}

export function getAttendanceRecordDetail(id: number) {
  return request.get({
    url: `/api/attendance/records/detail/${id}`
  })
}

export function exportAttendanceRecords(params: any) {
  return request.post({
    url: '/api/attendance/records/export',
    data: params,
    responseType: 'blob'
  })
}

export function searchEmployees(keyword: string) {
  return request.get({
    url: '/api/attendance/users/search',
    params: { keyword }
  })
}
