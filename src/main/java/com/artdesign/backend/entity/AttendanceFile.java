package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;

@Data
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

}
