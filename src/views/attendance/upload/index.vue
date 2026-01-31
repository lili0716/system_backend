<template>
  <div class="attendance-upload">
    <h1 class="page-title">上传考勤</h1>
    
    <!-- 上传区域 -->
    <ElCard class="upload-card">
      <template #header>
        <div class="card-header">
          <span>考勤文件上传</span>
          <ElButton type="primary" size="small" @click="downloadTemplate">
            <ElIcon class="mr-1"><Download /></ElIcon>
            下载模板
          </ElButton>
        </div>
      </template>
      
      <ElUpload
        ref="uploadRef"
        class="upload-dragger"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
      >
        <ElIcon class="upload-icon"><UploadFilled /></ElIcon>
        <div class="upload-text">将Excel文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="upload-tip">
            仅支持 .xlsx、.xls 格式文件，文件需包含：工号、日期、打卡时间
          </div>
        </template>
      </ElUpload>

      <div v-if="selectedFile" class="selected-file">
        <ElIcon><Document /></ElIcon>
        <span>{{ selectedFile.name }}</span>
        <ElButton type="danger" size="small" text @click="removeFile">
          <ElIcon><Close /></ElIcon>
        </ElButton>
      </div>

      <div class="upload-actions">
        <ElButton 
          type="primary" 
          size="large"
          :loading="uploading"
          :disabled="!selectedFile"
          @click="handleUpload"
        >
          <ElIcon class="mr-1"><Upload /></ElIcon>
          {{ uploading ? '上传解析中...' : '开始上传' }}
        </ElButton>
      </div>
    </ElCard>

    <!-- 解析结果 -->
    <ElCard v-if="parseResult" class="result-card">
      <template #header>
        <div class="card-header">
          <span>解析结果</span>
        </div>
      </template>

      <div class="result-summary">
        <div class="result-item success">
          <ElIcon><CircleCheck /></ElIcon>
          <span class="count">{{ parseResult.successCount || 0 }}</span>
          <span class="label">成功</span>
        </div>
        <div class="result-item error">
          <ElIcon><CircleClose /></ElIcon>
          <span class="count">{{ parseResult.failedCount || 0 }}</span>
          <span class="label">失败</span>
        </div>
      </div>

      <!-- 失败记录表格 -->
      <div v-if="parseResult.failedRecords && parseResult.failedRecords.length > 0" class="failed-records">
        <div class="failed-header">
          <span>失败记录详情</span>
          <ElButton type="warning" size="small" @click="exportFailed">
            <ElIcon class="mr-1"><Download /></ElIcon>
            导出失败记录
          </ElButton>
        </div>
        
        <ElTable :data="parseResult.failedRecords" stripe border max-height="400">
          <ElTableColumn prop="rowNum" label="行号" width="80" />
          <ElTableColumn prop="employeeId" label="工号" width="120" />
          <ElTableColumn prop="date" label="日期" width="120" />
          <ElTableColumn prop="reason" label="失败原因" />
        </ElTable>
      </div>

      <!-- 成功记录表格 -->
      <div v-if="parseResult.successRecords && parseResult.successRecords.length > 0" class="success-records">
        <div class="success-header">
          <span>成功导入记录</span>
        </div>
        
        <ElTable :data="parseResult.successRecords" stripe border max-height="400">
          <ElTableColumn prop="employeeId" label="工号" width="120" />
          <ElTableColumn prop="date" label="日期" width="120" />
          <ElTableColumn prop="workHours" label="工时(h)" width="100">
            <template #default="{ row }">
              {{ row.workHours ? row.workHours.toFixed(2) : '-' }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" width="100">
            <template #default="{ row }">
              <ElTag :type="getStatusType(row.status) as any">{{ getStatusText(row.status) }}</ElTag>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElCard>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  UploadFilled, 
  Upload, 
  Download, 
  Document, 
  Close, 
  CircleCheck, 
  CircleClose 
} from '@element-plus/icons-vue'
import { uploadAttendanceFile, exportFailedRecords } from '@/api/system-manage'
import { useUserStore } from '@/store/modules/user'
import type { UploadFile } from 'element-plus'

const userStore = useUserStore()
const uploadRef = ref()
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const parseResult = ref<any>(null)

const handleFileChange = (file: UploadFile) => {
  selectedFile.value = file.raw as File
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先移除当前文件')
}

const removeFile = () => {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  parseResult.value = null

  try {
    const userId = userStore.getUserInfo?.userId
    if (!userId) {
      ElMessage.error('用户信息获取失败，请重新登录')
      return
    }

    const res: any = await uploadAttendanceFile(selectedFile.value, userId)
    if (res.data) {
      parseResult.value = res.data
      if (res.data.success !== false) {
        ElMessage.success(`解析完成！成功: ${res.data.successCount || 0} 条，失败: ${res.data.failedCount || 0} 条`)
      } else {
        ElMessage.error(res.data.message || '解析失败')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

const exportFailed = async () => {
  if (!parseResult.value?.failedRecords?.length) {
    ElMessage.warning('没有失败记录可导出')
    return
  }

  try {
    const res = await exportFailedRecords(parseResult.value.failedRecords)
    const blob = new Blob([res as any], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '导入失败记录.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

const downloadTemplate = () => {
  // 生成简单的模板说明
  const templateContent = `考勤文件模板说明：
  
Excel文件格式要求：
- 第一行为表头（将被跳过）
- 第一列：工号
- 第二列：日期（格式：yyyy-MM-dd）
- 第三列：打卡时间（格式：HH:mm:ss）

示例：
工号        日期         打卡时间
20950      2026-01-15   08:55:23
20950      2026-01-15   18:05:12
20001      2026-01-15   09:02:45
20001      2026-01-15   17:58:30

注意：
1. 每个员工每天可以有多条打卡记录
2. 系统会自动选取上班时间前最早的打卡作为上班打卡
3. 系统会自动选取下班时间后最早的打卡作为下班打卡`

  const blob = new Blob([templateContent], { type: 'text/plain;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '考勤文件模板说明.txt'
  link.click()
  window.URL.revokeObjectURL(url)
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'warning',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '正常',
    1: '迟到',
    2: '早退',
    3: '缺勤'
  }
  return texts[status] || '未知'
}
</script>

<style lang="scss" scoped>
.attendance-upload {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 600;
}

.upload-card,
.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-dragger {
  width: 100%;
  
  :deep(.el-upload-dragger) {
    padding: 40px 20px;
  }
}

.upload-icon {
  font-size: 48px;
  color: var(--el-color-primary);
  margin-bottom: 16px;
}

.upload-text {
  color: #606266;
  em {
    color: var(--el-color-primary);
    font-style: normal;
  }
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.upload-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.result-summary {
  display: flex;
  gap: 40px;
  justify-content: center;
  margin-bottom: 24px;
  
  .result-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px 40px;
    border-radius: 8px;
    
    .el-icon {
      font-size: 32px;
    }
    
    .count {
      font-size: 28px;
      font-weight: 600;
    }
    
    .label {
      color: #909399;
    }
    
    &.success {
      background: #f0f9eb;
      .el-icon, .count { color: #67c23a; }
    }
    
    &.error {
      background: #fef0f0;
      .el-icon, .count { color: #f56c6c; }
    }
  }
}

.failed-records,
.success-records {
  margin-top: 20px;
}

.failed-header,
.success-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
}

.mr-1 {
  margin-right: 4px;
}
</style>
