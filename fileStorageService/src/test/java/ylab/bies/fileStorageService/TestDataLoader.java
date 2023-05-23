package ylab.bies.fileStorageService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.repository.FileRepository;

import java.util.UUID;

@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

  private final FileRepository fileRepository;

  public TestDataLoader(FileRepository fileRepository) {
    this.fileRepository = fileRepository;
  }

  @Override
  public void run(String... args) {
    Long ideaId = 111L;
    fileRepository.save(new FileModel(UUID.randomUUID(), ideaId, MediaType.APPLICATION_PDF_VALUE, "Test.pdf", 100L));
    fileRepository.save(new FileModel(UUID.randomUUID(), ideaId, MediaType.IMAGE_JPEG_VALUE, "Test.jpg", 100L));
    ideaId = 112L;
    fileRepository.save(new FileModel(UUID.randomUUID(), ideaId, MediaType.APPLICATION_PDF_VALUE, "Test.pdf", 100L));
  }
}
