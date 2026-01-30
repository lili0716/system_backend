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
      path: 'rule',
      name: 'AttendanceRule',
      component: '/attendance/rule',
      meta: {
        title: 'menus.attendance.rule',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    }
  ]
}
