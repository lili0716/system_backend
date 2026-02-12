package com.artdesign.backend.service;

import com.artdesign.backend.dto.DataSyncDTO;
import java.util.Map;

public interface DataSyncService {
    /**
     * 从远程数据库同步员工数据
     * @param syncDTO 数据同步配置
     * @return 同步结果
     */
    Map<String, Object> syncEmployeeData(DataSyncDTO syncDTO);
    
    /**
     * 测试远程数据库连接
     * @param syncDTO 数据库连接配置
     * @return 连接测试结果
     */
    Map<String, Object> testConnection(DataSyncDTO syncDTO);
}