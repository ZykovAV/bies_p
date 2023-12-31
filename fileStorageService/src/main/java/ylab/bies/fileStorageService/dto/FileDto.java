package ylab.bies.fileStorageService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class FileDto {
  private UUID fileId;
  private String fileName;
}
