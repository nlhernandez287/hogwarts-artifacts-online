package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;


import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserSaveDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.HogwartsUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Value("${api.endpoint.base-url}/users")
    String BASE_URL;

    @Autowired
    ObjectMapper objectMapper;

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
    void testSaveUserSuccess() throws Exception {
        // Given
        var userDto = new UserSaveDto(1, "test1", "password1", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        var user = users.get(0);

        when(this.userService.save(any(HogwartsUser.class)))
                .thenReturn(user);

        // When - Then
        this.mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add success"))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.enabled").value(user.isEnable()))
                .andExpect(jsonPath("$.data.roles").value(user.getRoles()))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testSaveUserErrorWhenIsProvidedEmptyFiles() throws Exception {
        // Given
        var userDto = new UserSaveDto(1, "", "", true, "");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        // When - Then
        this.mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details"))
                .andExpect(jsonPath("$.data.username").value("Username is required."))
                .andExpect(jsonPath("$.data.password").value("Password is required."))
                .andExpect(jsonPath("$.data.roles").value("Roles are required."));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        // Given
        var user = users.get(0);

        when(this.userService.findById(user.getId()))
                .thenReturn(user);

        // When - Then
        this.mockMvc.perform(get(BASE_URL + "/" + user.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.enabled").value(user.isEnable()))
                .andExpect(jsonPath("$.data.roles").value(user.getRoles()))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testFindUserByIdErrorWithNonExistentUserId() throws Exception {
        // Given
        var userId = 1;

        doThrow(new ObjectNotFoundException(HogwartsUser.class.getSimpleName(), userId))
                .when(this.userService).findById(userId);

        // When - Then
        this.mockMvc.perform(get(BASE_URL + "/" + userId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find HogwartsUser with Id %d".formatted(userId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {
        // Given
        when(this.userService.findAll())
                .thenReturn(users);

        // When - Then
        this.mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find all success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.users.size())))
                .andExpect(jsonPath("$.data[0].id").value(this.users.get(0).getId()))
                .andExpect(jsonPath("$.data[0].username").value(this.users.get(0).getUsername()))
                .andExpect(jsonPath("$.data[0].enabled").value(this.users.get(0).isEnable()))
                .andExpect(jsonPath("$.data[0].roles").value(this.users.get(0).getRoles()))
                .andExpect(jsonPath("$.data[0].password").doesNotExist());
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        // Given
        var userId = 1;
        var userDto = new UserDto(1, "test", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        var oldUser = users.get(0);

        when(this.userService.update(anyInt(), any(HogwartsUser.class)))
                .thenReturn(oldUser);

        // When - Then
        this.mockMvc.perform(put(BASE_URL + "/" + userId).contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.id").value(oldUser.getId()))
                .andExpect(jsonPath("$.data.username").value(oldUser.getUsername()))
                .andExpect(jsonPath("$.data.enabled").value(oldUser.isEnable()))
                .andExpect(jsonPath("$.data.roles").value(oldUser.getRoles()))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testUpdateUserErrorWithNonExistentUserId() throws Exception {
        // Given
        var userId = 1;

        var userDto = new UserDto(1, "test", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        doThrow(new ObjectNotFoundException(HogwartsUser.class.getSimpleName(), userId))
                .when(this.userService).update(anyInt(), any(HogwartsUser.class));

        // When - Then
        this.mockMvc.perform(put(BASE_URL + "/" + userId).contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find HogwartsUser with Id %d".formatted(userId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateUserErrorWhenIsProvidedEmptyFiles() throws Exception {
        // Given
        var userId = 1;

        var userDto = new UserDto(1, "", true, "");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        // When - Then
        this.mockMvc.perform(put(BASE_URL + "/" + userId).contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details"))
                .andExpect(jsonPath("$.data.username").value("Username is required."))
                .andExpect(jsonPath("$.data.roles").value("Roles are required."));
    }

    @Test
    void testDeleteUserByIdSuccess() throws Exception {
        // Given
        var userId = 1;

        doNothing().when(this.userService)
                .delete(anyInt());

        // When - Then
        this.mockMvc.perform(delete(BASE_URL + "/" + userId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void testDeleteUserByIdErrorWithNonExistentUserId() throws Exception {
        // Given
        var userId = 1;

        doThrow(new ObjectNotFoundException(HogwartsUser.class.getSimpleName(), userId))
                .when(this.userService).delete(anyInt());

        // When - Then
        this.mockMvc.perform(delete(BASE_URL + "/" + userId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find HogwartsUser with Id %d".formatted(userId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}