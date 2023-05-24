package ylab.bies.ideaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.service.IdeaService;

import javax.validation.Valid;


@RestController
@Tag(name = "Idea Controller", description = "REST operations with ideas")
@RequestMapping(value = "/api/v1/ideas")
@RequiredArgsConstructor
@Slf4j
public class IdeaController {

    private final IdeaService ideaService;

    @PostMapping(value = "/draft")
    @Operation(summary = "Request for a draft idea",
            responses =
                    {@ApiResponse(description = "draft idea",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = IdeaDraftResponseDto.class)))})
    public ResponseEntity<IdeaDraftResponseDto> createDraftIdea(@RequestHeader("Authorization") String token, @Valid @RequestBody IdeaDraftRequestDto request) {
        IdeaDraftResponseDto response = ideaService.createDraftIdea(token, request);
        log.info("Response with created idea: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
