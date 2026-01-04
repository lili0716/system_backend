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
      path: 'form_application',
      name: 'Form_Application',
      component: '/attendance/form_application',
      meta: {
        title: 'menus.attendance.form_application',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'approval',
      name: 'Approval',
      component: '/attendance/approval',
      meta: {
        title: 'menus.attendance.approval',
        keepAlive: true,
        roles: ['R_SUPER']
      }
    },
    {
      path: 'user-center',
      name: 'UserCenter',
      component: '/system/user-center',
      meta: {
        title: 'menus.system.userCenter',
        isHide: true,
        keepAlive: true,
        isHideTab: true
      }
    },
    {
      path: 'menu',
      name: 'Menus',
      component: '/system/menu',
      meta: {
        title: 'menus.system.menu',
        keepAlive: true,
        roles: ['R_SUPER'],
        isHide: true,
        authList: [
          { title: '新增', authMark: 'add' },
          { title: '编辑', authMark: 'edit' },
          { title: '删除', authMark: 'delete' }
        ]
      }
    }
  ]
}
