import { AppRouteRecord } from '@/types/router'

export const attendanceRoutes: AppRouteRecord = {
  path: '/attendance',
  name: 'Attendance',
  component: '/index/index',
  meta: {
    title: 'menus.attendance.title',
    icon: 'ri:user-3-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'upload',
      name: 'AttendanceUpload',
      component: '/attendance/upload',
      meta: {
        title: 'menus.attendance.upload',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'rule',
      name: 'AttendanceRule',
      component: '/attendance/rule',
      meta: {
        title: 'menus.attendance.rule',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'query',
      name: 'AttendanceQuery',
      component: '/attendance/query',
      meta: {
        title: 'menus.attendance.query',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    }
  ]
}
