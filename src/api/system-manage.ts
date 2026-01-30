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
