package com.carpool.backend.user;

import com.carpool.backend.exception.DupPhoneNumberExp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    @Test
    void createShouldRejectDuplicatePhoneNumber() {
        CreateUserReq req = new CreateUserReq(
                "Bob",
                "+84901234567"
        );

        when(userRepo.findByPhoneNumber(req.phoneNumber()))
                .thenReturn(Optional.of(
                        new User("Alice", req.phoneNumber())
                ));

        assertThrows(
                DupPhoneNumberExp.class,
                () -> userService.create(req)
        );

        verify(userRepo, never()).save(any(User.class));
    }
    @Test
    void createShouldSaveNewUser() {
        CreateUserReq req = new CreateUserReq(
                "Alice",
                "+84987654321"
        );

        when(userRepo.findByPhoneNumber(req.phoneNumber()))
                .thenReturn(Optional.empty());

        when(userRepo.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.create(req);

        assertEquals("Alice", result.getName());
        assertEquals("+84987654321", result.getPhoneNumber());

        verify(userRepo).save(any(User.class));
    }
}
