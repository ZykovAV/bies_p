package ylab.bies.fileStorageService.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FileListByIdeaDto {
  private Long ideaId;
  private List<FileDto> files;
}
