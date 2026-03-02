package com.artdesign.backend.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import com.artdesign.backend.common.Result;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<?> handleAllExceptions(Exception ex,
            jakarta.servlet.http.HttpServletRequest request) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        // 如果客户端是 SSE 请求 (EventSource)，需要把报错转化为 String 以兼容 text/event-stream
        String acceptContext = request.getHeader("Accept");
        if (acceptContext != null && acceptContext.contains("text/event-stream")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String jsonResult = mapper.writeValueAsString(Result.error(500, ex.getMessage()));
                return org.springframework.http.ResponseEntity.status(500)
                        .header("Content-Type", "text/event-stream;charset=UTF-8")
                        .body("event: error\ndata: " + jsonResult + "\n\n");
            } catch (Exception jsonEx) {
                return org.springframework.http.ResponseEntity.status(500)
                        .header("Content-Type", "text/event-stream;charset=UTF-8")
                        .body("event: error\ndata: {\"code\":500,\"msg\":\"System error\"}\n\n");
            }
        }

        return org.springframework.http.ResponseEntity.status(500).body(Result.error(500, ex.getMessage()));
    }
}
