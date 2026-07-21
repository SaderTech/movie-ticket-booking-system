package com.movieticket.showtimeservice.exception;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(ShowtimeNotFoundException.class)
    public ResponseEntity<?> handleNotFound(
            ShowtimeNotFoundException ex
    ){

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        Map.of(
                                "timestamp",
                                LocalDateTime.now(),

                                "status",
                                404,

                                "message",
                                ex.getMessage()
                        )
                );

    }




    @ExceptionHandler(DuplicateShowtimeException.class)
    public ResponseEntity<?> handleDuplicate(
            DuplicateShowtimeException ex
    ){

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "timestamp",
                                LocalDateTime.now(),

                                "status",
                                409,

                                "message",
                                ex.getMessage()
                        )
                );

    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(
            Exception ex
    ){

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "timestamp",
                                LocalDateTime.now(),

                                "status",
                                500,

                                "message",
                                ex.getMessage()
                        )
                );

    }

}