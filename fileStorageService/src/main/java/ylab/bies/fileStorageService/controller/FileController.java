package ylab.bies.fileStorageService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.service.FileService;

@RestController
@RequestMapping("api/v1/files")
public class FileController {

  private final Logger logger = LoggerFactory.getLogger(FileController.class);
  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @Operation(
          summary = "Save file attachment for idea to S3",
          responses ={
                  @ApiResponse(
                        responseCode ="201",
                        description = "File successfully saved"),
                  @ApiResponse(
                         responseCode ="400",
                         description = "Data format is incorrect. Idea id and file name must not be empty."),
                  @ApiResponse(
                          responseCode ="403",
                          description = "User is not authorized for editing idea with id specified"),
                  @ApiResponse(
                          responseCode ="500",
                          description = "Internal server error")
          }
  )
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> saveFile(@RequestParam("idea_id") Long ideaId,
                                       @RequestParam MultipartFile file) throws Exception {
    logger.info("File received: " + file.getOriginalFilename());
    //TODO: token logic to be updated
    String dummyToken = "test";
    fileService.addFile(ideaId, file, dummyToken);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

}
