import { AppRouteRecord } from '@/types/router'

export const formRoutes: AppRouteRecord = {
    path: '/form',
    name: 'Form',
    component: '/index/index',
    meta: {
        title: '表单管理',
        icon: 'ri:file-list-3-line',
        roles: ['R_SUPER', 'R_ADMIN', 'R_USER']
    },
    children: [
        {
            path: 'application',
            name: 'FormApplication',
            component: '/form/application',
            meta: {
                title: '表单申请',
                keepAlive: true
            }
        },
        {
            path: 'approval',
            name: 'FormApproval',
            component: '/form/approval',
            meta: {
                title: '表单审批',
                keepAlive: true
            }
        }
    ]
}
