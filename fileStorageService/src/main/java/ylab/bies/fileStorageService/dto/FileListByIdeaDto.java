package ylab.bies.fileStorageService.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileListByIdeaDto {
  private Long ideaId;
  private List<FileDto> files;
}
