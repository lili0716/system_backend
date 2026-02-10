package com.artdesign.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统API调用日志实体
 */
@Entity
@Table(name = "system_logs", indexes = {
        @Index(name = "idx_request_time", columnList = "requestTime"),
        @Index(name = "idx_employee_id", columnList = "employeeId")
})
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 请求时间 */
    private LocalDateTime requestTime;

    /** 操作用户工号 */
    private String employeeId;

    /** 操作用户姓名 */
    private String nickName;

    /** 请求方法 GET/POST/PUT/DELETE */
    private String method;

    /** 请求URI */
    private String uri;

    /** 请求参数（JSON字符串，截断至2000字符） */
    @Column(columnDefinition = "TEXT")
    private String requestParams;

    /** 响应状态码 */
    private Integer responseCode;

    /** 响应体（截断至2000字符） */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /** 请求耗时（毫秒） */
    private Long duration;

    /** 客户端IP */
    private String ip;

    public SystemLog() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
