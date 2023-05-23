package ylab.bies.fileStorageService.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "file")
public class FileModel {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
          name = "UUID",
          strategy = "org.hibernate.id.UUIDGenerator"
  )
  @Column(name = "id")
  private UUID id;
  @NotNull
  @Column(name = "idea_id")
  private Long ideaId;
  @Column(name = "content_type")
  private String contentType;
  @NotNull
  @NotBlank
  @Column(name = "file_name")
  private String fileName;
  @Column(name = "file_size")
  private Long fileSize;

  public FileModel(Long ideaId, MultipartFile file) {
    this.ideaId = ideaId;
    this.contentType = file.getContentType();
    this.fileName = file.getOriginalFilename();
    this.fileSize = file.getSize();
  }
}
