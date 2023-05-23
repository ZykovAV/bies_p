package ylab.bies.fileStorageService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file")
public class FileModel {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
          name = "UUID",
          strategy = "org.hibernate.id.UUIDGenerator"
  )
  private UUID id;
  @NotNull
  private Long ideaId;
  private String contentType;
  @NotNull
  @NotBlank
  private String fileName;
  private Long fileSize;

  public FileModel(Long ideaId, MultipartFile file) {
    this.ideaId = ideaId;
    this.contentType = file.getContentType();
    this.fileName = file.getOriginalFilename();
    this.fileSize = file.getSize();
  }
}
