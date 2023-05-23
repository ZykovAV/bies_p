package ylab.bies.fileStorageService.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
  private String path;
  private String message;
  private int statusCode;
  private LocalDateTime localDateTime;
}
