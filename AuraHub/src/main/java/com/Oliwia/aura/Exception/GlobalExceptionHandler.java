package com.Oliwia.aura.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidationErrors(MethodArgumentNotValidException ex){
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(fe-> fe.getField() + " : " + fe.getDefaultMessage()).toList();
        return buildErrorResponse(HttpStatus.BAD_REQUEST,"Input data validation",errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGenericException(Exception ex){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error",List.of(ex.getMessage()));
    }

    private  ResponseEntity<Map<String,Object>> buildErrorResponse(HttpStatus status,String message, List<String> details){
        Map<String,Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status",status.value());
        body.put("error",message);
        body.put("details",details);

        return ResponseEntity.status(status).body(body);
    }
}
