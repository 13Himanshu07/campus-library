package com.library.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
 private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
 @ExceptionHandler(ApiException.class) ResponseEntity<?> api(ApiException e){return ResponseEntity.status(e.getStatus()).body(Map.of("success",false,"message",e.getMessage(),"timestamp",Instant.now()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Invalid request");return ResponseEntity.badRequest().body(Map.of("success",false,"message",m,"timestamp",Instant.now()));}
 @ExceptionHandler(Exception.class) ResponseEntity<?> unexpected(Exception e){log.error("Unhandled API error",e);return ResponseEntity.status(500).body(Map.of("success",false,"message","Request could not be completed","timestamp",Instant.now()));}
}
