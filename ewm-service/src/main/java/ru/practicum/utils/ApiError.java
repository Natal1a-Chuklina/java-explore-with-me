package ru.practicum.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiError {
    @JsonIgnore
    private List<String> errors;
    private HttpStatus status;
    private String reason;
    private String message;
    private String timeStamp;
}
