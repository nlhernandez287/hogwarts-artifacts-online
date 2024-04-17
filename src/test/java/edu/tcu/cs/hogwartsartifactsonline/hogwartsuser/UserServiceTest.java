package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private List<HogwartsUser> users;

    @BeforeEach
    void setUp() {
        users = List.of(
                UserUtils.createUser(1, "test1", "password1", true, "user"),
                UserUtils.createUser(2, "test2", "password2", true, "admin"),
                UserUtils.createUser(3, "test3", "password3", true, "user admin")
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSaveSuccess() {
        // Given
        var user = users.get(0);

        when(this.userRepository.save(user))
                .thenReturn(user);

        // When
        var newUser = this.userService.save(user);

        // Then
        assertThat(newUser).usingRecursiveAssertion().isEqualTo(user);
        verify(this.userRepository, times(1)).save(user);

    }

    @Test
    void testFindByIdSuccess() {
        // Given
        var user = users.get(0);

        when(this.userRepository.findById(anyInt()))
                .thenReturn(Optional.of(user));

        // When
        var existingUser = this.userService.findById(user.getId());

        // Then
        assertThat(existingUser).usingRecursiveAssertion().isEqualTo(user);
        verify(this.userRepository, times(1)).findById(user.getId());
    }

    @Test
    void testFindByIdErrorNonExistentUserId() {
        // Given
        var userId = 1;

        when(this.userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        // When - Then
        assertThatThrownBy(() -> this.userService.findById(userId))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find HogwartsUser with Id %d".formatted(userId));
        verify(this.userRepository, times(1)).findById(userId);
    }

    @Test
    void testFindAllSuccess() {
        // Given
        when(this.userRepository.findAll())
                .thenReturn(users);

        // When
        var usersList = this.userService.findAll();

        // Then
        assertThat(usersList.size()).isEqualTo(users.size());
        verify(this.userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateSuccess() {
        // Given
        var oldUser = users.get(0);

        when(this.userRepository.findById(oldUser.getId()))
                .thenReturn(Optional.of(oldUser));

        when(this.userRepository.save(any(HogwartsUser.class)))
                .thenReturn(oldUser);

        // When
        var updatedUser = this.userService.update(oldUser.getId(), oldUser);

        // Then
        assertThat(updatedUser).usingRecursiveAssertion().isEqualTo(oldUser);
        verify(this.userRepository, times(1)).findById(oldUser.getId());
        verify(this.userRepository, times(1)).save(oldUser);
    }

    @Test
    void testUpdateErrorNonExistentUserId() {
        // Given
        var userId = 1;

        when(this.userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        // When - Then
        assertThatThrownBy(() -> this.userService.update(userId, any(HogwartsUser.class)))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find HogwartsUser with Id %d".formatted(userId));
        verify(this.userRepository, times(1)).findById(userId);
        verify(this.userRepository, times(0)).save(any(HogwartsUser.class));
    }

    @Test
    void testDeleteSuccess() {
        // Given
        var user = users.get(0);

        when(this.userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        doNothing().when(this.userRepository)
                .deleteById(anyInt());

        // When
        this.userService.delete(user.getId());

        // Then
        verify(this.userRepository, times(1)).findById(user.getId());
        verify(this.userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void testDeleteErrorWithNonExistentUserId() {
        // Given
        var userId = 1;

        when(this.userRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> this.userService.delete(userId))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find %s with Id %d".formatted(HogwartsUser.class.getSimpleName(), userId));

        // Then
        verify(this.userRepository, times(1)).findById(userId);
        verify(this.userRepository, times(0)).deleteById(userId);
    }
}
