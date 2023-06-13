package ylab.bies.ideaservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;

/**
 * Service for create, edit, like/dislike ideas and change status
 */
public interface IdeaService {
    /**
     * Get idea by id. Returns only idea with statuses UNDER_CONSIDERATION, ACCEPTER, REJECTED and
     * current user's DRAFT.
     * @param id of idea
     * @return an idea with required id
     */
    IdeaResponseDto findById(Long id);

    /**
     * Create a draft
     * @param request contains required parameters 'name' and 'text'
     * @return created draft (idea with status DRAFT)
     */
    IdeaDraftResponseDto createDraftIdea(IdeaDraftRequestDto request);

    /**
     * Change status of idea. Only available to experts.
     * It is possible to change the status only for ideas with a status UNDER_CONSIDERATION
     * @param id of idea
     * @param status (3 - ACCEPTED or 4 - REJECTED)
     */
    void changeStatus(Long id, Integer status);

    /**
     * Like or dislike an idea. It is possible to rate only other user's idea with status UNDER_CONSIDERATION
     * @param id of idea
     * @param isLike 'true' if like or 'false' if dislike
     */
    void rate(Long id, boolean isLike);

    /**
     * Check if current user is an author of idea
     * @param id of idea
     * @return 'true' if current user is an author of idea, or 'false' - if not
     */
    boolean isCurrentUserAuthor(Long id);

    /**
     * Get a list of ideas. Returns only ideas with statuses UNDER_CONSIDERATION, ACCEPTER, REJECTED.
     * @param pageable contains page number, how many results on page, sort options.
     * @return page with ideas
     */
    Page<IdeaResponseDto> getAllIdeas(Pageable pageable);

    /**
     * Get a list of current user's ideas.
     * @param pageable contains page number, how many results on page, sort options.
     * @return page with current user's ideas
     */
    Page<IdeaResponseDto> getAllUsersIdeas(Pageable pageable);

    /**
     * Edit and publish the idea. It is possible only edit user's own idea with DRAFT or UNDER_CONSIDERATION status.
     * If a draft is being edited, it will be automatically published (status changed to UNDER_CONSIDERATION)
     * @param editRequest contains required parameters 'name' and 'text'
     * @param id of idea
     * @return updated idea
     */
    IdeaResponseDto updateIdea(Long id, IdeaRequestDto editRequest);
}
