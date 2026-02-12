package com.artdesign.backend.dto;

import java.util.List;

public class DataSyncDTO {
    // 数据库配置
    private String dbType;
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
    private String tableName;
    
    // 字段映射
    private List<FieldMapping> fieldMappings;
    
    // 内部类：字段映射
    public static class FieldMapping {
        private String localField;
        private String remoteField;
        
        // Getters and Setters
        public String getLocalField() {
            return localField;
        }
        
        public void setLocalField(String localField) {
            this.localField = localField;
        }
        
        public String getRemoteField() {
            return remoteField;
        }
        
        public void setRemoteField(String remoteField) {
            this.remoteField = remoteField;
        }
    }
    
    // Getters and Setters
    public String getDbType() {
        return dbType;
    }
    
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }
    
    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }
}