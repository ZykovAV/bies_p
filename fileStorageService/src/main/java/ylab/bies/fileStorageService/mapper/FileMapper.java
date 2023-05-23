package ylab.bies.fileStorageService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ylab.bies.fileStorageService.dto.FileDto;
import ylab.bies.fileStorageService.entity.FileModel;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileMapper {
  @Mapping(target = "fileId", source = "id")
  FileDto toFileDto(FileModel fileModel);

  List<FileDto> toFileDtoList(List<FileModel> fileModelList);
}
