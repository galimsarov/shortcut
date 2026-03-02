package com.example.c_advanced_core.d_exceptions.c_spring;

/**
 * Что это даёт:
 * - единый формат ошибок;
 * - понятные HTTP-статусы;
 * - меньше try/catch в контроллерах.
 */

//@RestControllerAdvice
public class DGlobalExceptionHandler {
//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                "timestamp", Instant.now().toString(),
//                "error", "USER_NOT_FOUND",
//                "message", ex.getMessage()
//        ));
//    }

//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                "timestamp", Instant.now().toString(),
//                "error", "BAD_REQUEST",
//                "message", ex.getMessage()
//        ));
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "timestamp", Instant.now().toString(),
//                "error", "INTERNAL_ERROR",
//                "message", "Внутренняя ошибка сервера"
//        ));
//    }
}
