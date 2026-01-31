import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { systemRoutes } from './system'
import { resultRoutes } from './result'
import { exceptionRoutes } from './exception'
import { attendanceRoutes } from './application'

/**
 * 导出所有模块化路由
 */
import { formRoutes } from './form'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  attendanceRoutes,
  formRoutes,
  systemRoutes,
  resultRoutes,
  exceptionRoutes
]
