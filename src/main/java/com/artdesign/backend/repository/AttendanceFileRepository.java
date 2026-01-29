package com.artdesign.backend.repository;

import com.artdesign.backend.entity.AttendanceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceFileRepository extends JpaRepository<AttendanceFile, Long> {

    // 根据上传人ID查询
    List<AttendanceFile> findByUploaderId(Long uploaderId);

    // 根据解析状态查询
    List<AttendanceFile> findByParseStatus(Integer parseStatus);

    // 根据上传人和解析状态查询
    List<AttendanceFile> findByUploaderIdAndParseStatus(Long uploaderId, Integer parseStatus);

}
