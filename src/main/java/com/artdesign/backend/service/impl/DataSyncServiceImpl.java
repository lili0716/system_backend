package com.artdesign.backend.service.impl;

import com.artdesign.backend.dto.DataSyncDTO;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.service.DataSyncService;
import com.artdesign.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

@Service
public class DataSyncServiceImpl implements DataSyncService {
    
    @Autowired
    private UserService userService;
    
    /**
     * 从远程数据库同步员工数据
     * @param syncDTO 数据同步配置
     * @return 同步结果
     */
    @Override
    public Map<String, Object> syncEmployeeData(DataSyncDTO syncDTO) {
        Map<String, Object> result = new HashMap<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            // 1. 建立数据库连接
            connection = getConnection(syncDTO);
            if (connection == null) {
                result.put("code", 500);
                result.put("msg", "数据库连接失败");
                return result;
            }
            
            // 2. 构建查询SQL
            String sql = buildQuerySql(syncDTO);
            preparedStatement = connection.prepareStatement(sql);
            
            // 3. 执行查询
            resultSet = preparedStatement.executeQuery();
            
            // 4. 处理结果集
            int successCount = 0;
            int failCount = 0;
            List<String> failReasons = new ArrayList<>();
            
            while (resultSet.next()) {
                try {
                    // 创建或更新用户
                    User user = createOrUpdateUser(resultSet, syncDTO);
                    if (user != null) {
                        userService.save(user);
                        successCount++;
                    } else {
                        failCount++;
                        failReasons.add("用户数据无效");
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("处理用户数据失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 5. 构建结果
            result.put("code", 200);
            result.put("msg", "数据同步完成");
            result.put("data", Map.of(
                "successCount", successCount,
                "failCount", failCount,
                "failReasons", failReasons
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "数据同步失败: " + e.getMessage());
        } finally {
            // 关闭资源
            closeResources(resultSet, preparedStatement, connection);
        }
        
        return result;
    }
    
    /**
     * 测试远程数据库连接
     * @param syncDTO 数据库连接配置
     * @return 连接测试结果
     */
    @Override
    public Map<String, Object> testConnection(DataSyncDTO syncDTO) {
        Map<String, Object> result = new HashMap<>();
        Connection connection = null;
        
        try {
            // 建立数据库连接
            connection = getConnection(syncDTO);
            if (connection != null && !connection.isClosed()) {
                result.put("code", 200);
                result.put("msg", "数据库连接成功");
            } else {
                result.put("code", 500);
                result.put("msg", "数据库连接失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "数据库连接失败: " + e.getMessage());
        } finally {
            // 关闭资源
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return result;
    }
    
    /**
     * 建立数据库连接
     * @param syncDTO 数据库连接配置
     * @return 数据库连接
     */
    private Connection getConnection(DataSyncDTO syncDTO) throws SQLException, ClassNotFoundException {
        String dbType = syncDTO.getDbType();
        String host = syncDTO.getHost();
        Integer port = syncDTO.getPort();
        String database = syncDTO.getDatabase();
        String username = syncDTO.getUsername();
        String password = syncDTO.getPassword();
        
        // 根据数据库类型加载驱动并构建连接URL
        String driverClassName = getDriverClassName(dbType);
        String url = buildConnectionUrl(dbType, host, port, database);
        
        // 加载驱动
        Class.forName(driverClassName);
        
        // 建立连接
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * 获取数据库驱动类名
     * @param dbType 数据库类型
     * @return 驱动类名
     */
    private String getDriverClassName(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgres":
                return "org.postgresql.Driver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
    }
    
    /**
     * 构建数据库连接URL
     * @param dbType 数据库类型
     * @param host 主机地址
     * @param port 端口
     * @param database 数据库名
     * @return 连接URL
     */
    private String buildConnectionUrl(String dbType, String host, Integer port, String database) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
            case "postgres":
                return "jdbc:postgresql://" + host + ":" + port + "/" + database;
            case "oracle":
                return "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
            case "sqlserver":
                return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + database;
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
    }
    
    /**
     * 构建查询SQL
     * @param syncDTO 数据同步配置
     * @return 查询SQL
     */
    private String buildQuerySql(DataSyncDTO syncDTO) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        
        // 构建SELECT子句
        List<DataSyncDTO.FieldMapping> fieldMappings = syncDTO.getFieldMappings();
        for (int i = 0; i < fieldMappings.size(); i++) {
            DataSyncDTO.FieldMapping mapping = fieldMappings.get(i);
            sql.append(mapping.getRemoteField());
            if (i < fieldMappings.size() - 1) {
                sql.append(", ");
            }
        }
        
        // 构建FROM子句
        sql.append(" FROM " + syncDTO.getTableName());
        
        return sql.toString();
    }
    
    /**
     * 根据结果集创建或更新用户
     * @param resultSet 查询结果集
     * @param syncDTO 数据同步配置
     * @return 用户对象
     */
    private User createOrUpdateUser(ResultSet resultSet, DataSyncDTO syncDTO) throws SQLException {
        User user = null;
        Map<String, Object> fieldValues = new HashMap<>();
        
        // 读取字段值
        List<DataSyncDTO.FieldMapping> fieldMappings = syncDTO.getFieldMappings();
        for (DataSyncDTO.FieldMapping mapping : fieldMappings) {
            String remoteField = mapping.getRemoteField();
            Object value = resultSet.getObject(remoteField);
            fieldValues.put(mapping.getLocalField(), value);
        }
        
        // 获取工号
        String employeeId = (fieldValues.get("employeeId") != null) ? fieldValues.get("employeeId").toString() : null;
        if (employeeId == null || employeeId.isEmpty()) {
            return null;
        }
        
        // 查找现有用户
        user = userService.findByEmployeeId(employeeId);
        if (user == null) {
            user = new User();
            user.setCreateTime(new java.util.Date());
            user.setStatus("1"); // 默认为在职状态
        } else {
            user.setUpdateTime(new java.util.Date());
        }
        
        // 设置用户属性
        setUserProperties(user, fieldValues);
        
        return user;
    }
    
    /**
     * 设置用户属性
     * @param user 用户对象
     * @param fieldValues 字段值映射
     */
    private void setUserProperties(User user, Map<String, Object> fieldValues) {
        // 姓名
        if (fieldValues.containsKey("nickName")) {
            user.setNickName((fieldValues.get("nickName") != null) ? fieldValues.get("nickName").toString() : null);
        }
        
        // 性别
        if (fieldValues.containsKey("userGender")) {
            user.setUserGender((fieldValues.get("userGender") != null) ? fieldValues.get("userGender").toString() : null);
        }
        
        // 工号
        if (fieldValues.containsKey("employeeId")) {
            user.setEmployeeId((fieldValues.get("employeeId") != null) ? fieldValues.get("employeeId").toString() : null);
        }
        
        // 身份证号
        if (fieldValues.containsKey("idCard")) {
            user.setIdCard((fieldValues.get("idCard") != null) ? fieldValues.get("idCard").toString() : null);
        }
        
        // 邮箱
        if (fieldValues.containsKey("email")) {
            user.setEmail((fieldValues.get("email") != null) ? fieldValues.get("email").toString() : null);
        }
        
        // 入职日期
        if (fieldValues.containsKey("hireDate")) {
            Object hireDateObj = fieldValues.get("hireDate");
            if (hireDateObj != null) {
                if (hireDateObj instanceof java.util.Date) {
                    user.setHireDate((java.util.Date) hireDateObj);
                } else if (hireDateObj instanceof java.sql.Date) {
                    user.setHireDate(new java.util.Date(((java.sql.Date) hireDateObj).getTime()));
                } else if (hireDateObj instanceof String) {
                    try {
                        // 尝试解析日期字符串
                        java.util.Date hireDate = new SimpleDateFormat("yyyy-MM-dd").parse(hireDateObj.toString());
                        user.setHireDate(hireDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * 关闭数据库资源
     * @param resultSet 结果集
     * @param preparedStatement 预处理语句
     * @param connection 连接
     */
    private void closeResources(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}