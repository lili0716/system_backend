package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance_file")
@EntityListeners(AuditingEntityListener.class)
public class AttendanceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 文件名
    private String fileName;

    // 原始文件名
    private String originalFileName;

    // 文件路径
    private String filePath;

    // 文件大小
    private Long fileSize;

    // 文件类型
    private String fileType;

    // 上传人
    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;

    // 上传时间
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    // 解析状态：0-未解析，1-解析中，2-解析成功，3-解析失败
    private Integer parseStatus;

    // 解析结果
    private String parseResult;

    // 备注
    private String remark;

    public AttendanceFile() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Integer getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(Integer parseStatus) {
        this.parseStatus = parseStatus;
    }

    public String getParseResult() {
        return parseResult;
    }

    public void setParseResult(String parseResult) {
        this.parseResult = parseResult;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
