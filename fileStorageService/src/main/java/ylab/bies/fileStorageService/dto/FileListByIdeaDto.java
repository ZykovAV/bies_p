package ylab.bies.fileStorageService.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class FileListByIdeaDto {
  private Long ideaId;
  private List<FileDto> files;
}
