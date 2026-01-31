export function isBeforeToday(targetDate: Date | string): boolean {
  // 1. 处理入参：统一转为 Date 对象
  const date = typeof targetDate === 'string' ? new Date(targetDate) : new Date(targetDate)

  // 2. 校验日期有效性（避免无效日期如 "2025-13-01"）
  if (isNaN(date.getTime())) {
    throw new Error('传入的日期格式无效，请检查！')
  }

  // 3. 重置当前日期和目标日期的时分秒为 0（仅保留年月日）
  const today = new Date()
  today.setHours(0, 0, 0, 0) // 今天 00:00:00.000
  const normalizedTargetDate = new Date(date)
  normalizedTargetDate.setHours(0, 0, 0, 0) // 目标日期 00:00:00.000

  // 4. 核心比较：目标日期 < 今天 → 返回 true
  return normalizedTargetDate < today
}
