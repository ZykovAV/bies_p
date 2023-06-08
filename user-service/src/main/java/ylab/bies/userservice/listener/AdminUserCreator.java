package ylab.bies.userservice.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ylab.bies.userservice.config.ApplicationConfiguration;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.repository.UserRepository;
import ylab.bies.userservice.service.UserService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserCreator implements ApplicationListener<ContextRefreshedEvent> {
    private static final String ADMIN_EMAIL = "admin@mail.com";
    private static final String ADMIN_FIRST_NAME = "admin";
    private static final String ADMIN_LAST_NAME = "admin";
    private static final String ADMIN_ROLE = "ADMIN";
    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationConfiguration configuration;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        createAdminUser();
    }

    private void createAdminUser() {
        log.info("Creating an Admin user");
        if (isAdminExists()) {
            log.info("Failed to create Admin user. Admin user has already been created");
            return;
        }
        RegisterRequest request = getAdminRegisterRequest();
        UserResponse response = userService.register(request);
        UUID adminId = response.getId();
        userService.assignRole(String.valueOf(adminId), ADMIN_ROLE);

        log.info("Successfully created Admin user");
    }

    private boolean isAdminExists() {
        return userRepository.findByEmail(ADMIN_EMAIL).isPresent();
    }

    private RegisterRequest getAdminRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(configuration.getAdminUsername());
        request.setPassword(configuration.getAdminPassword());
        request.setEmail(ADMIN_EMAIL);
        request.setFirstName(ADMIN_FIRST_NAME);
        request.setLastName(ADMIN_LAST_NAME);
        return request;
    }
}
