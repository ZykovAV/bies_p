package ylab.bies.ideaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.service.IdeaService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


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
    public ResponseEntity<IdeaDraftResponseDto> createDraftIdea(@Valid @RequestBody IdeaDraftRequestDto request) {
        IdeaDraftResponseDto response = ideaService.createDraftIdea(request);
        log.info("Response with created idea: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping(value = "/{id}")
    @Operation(summary = "Request for a info about idea")
    public ResponseEntity<IdeaResponseDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(ideaService.findById(id), HttpStatus.OK);
    }


    @PatchMapping("/{id}/status")
    @Operation(summary = "Change status of idea")
    public ResponseEntity<HttpStatus> changeStatus(@PathVariable Long id, @RequestParam Integer statusId) {
        ideaService.changeStatus(id, statusId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/{id}/like")
    @Operation(summary = "Like idea")
    public ResponseEntity<HttpStatus> like(@PathVariable Long id) {
        ideaService.rate(id, true);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/{id}/dislike")
    @Operation(summary = "Dislike idea")
    public ResponseEntity<HttpStatus> dislike(@PathVariable Long id) {
        ideaService.rate(id, false);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Getting a list of ideas", description = "List of all users ideas")
    public ResponseEntity<Page<IdeaResponseDto>> getAllIdeas(@NotNull final Pageable pageable) {
        Page<IdeaResponseDto> ideas = ideaService.getAllIdeas(pageable);
        log.info(String.format("Ideas %s received successfully", ideas));
        return new ResponseEntity<>(ideas, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Saving or updating idea",
            responses = {@ApiResponse(description = "Updated Idea",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = IdeaResponseDto.class)))})
    public ResponseEntity<IdeaResponseDto> updateIdea(@Valid @RequestBody IdeaRequestDto editRequest, @PathVariable Long id) {
        IdeaResponseDto updatedIdea = ideaService.updateIdea(id, editRequest);
        log.info("Idea updated successfully");
        return new ResponseEntity<>(updatedIdea, HttpStatus.OK);
    }


    @GetMapping(value = "/drafts")
    @Operation(summary = "Getting a list of drafts",
            responses =
                    {@ApiResponse(description = "List of all users drafts",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Page.class)))})
    public ResponseEntity<Page<IdeaDraftResponseDto>> getAllUsersDrafts(@NotNull final Pageable pageable) {
        Page<IdeaDraftResponseDto> drafts = ideaService.getAllUsersDrafts(pageable);
        log.info(String.format(" User's drafts %s received successfully", drafts));
        return new ResponseEntity<>(drafts, HttpStatus.OK);
    }
}
