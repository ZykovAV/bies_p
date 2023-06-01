package ylab.bies.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContactsPagination {
    private List<ContactsResponse> contacts;
    private long totalElements;
    private int currentPage;
    private int totalPages;
}
