package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.IntegrationTestConfig;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserSaveDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class UserControllerIntegration extends IntegrationTestConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String tokenUser;

    String tokenAdmin;


    @Value("${api.endpoint.base-url}")
    String BASE_URL;

    @BeforeEach
    void setUp() throws Exception {
        this.tokenAdmin = generateToken("admin", "password2");
        this.tokenUser = generateToken("user1", "password1");
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {
        this.mockMvc.perform(get(BASE_URL + "/users").header("Authorization", this.tokenAdmin)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find all success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    void testFindAllUsersSErrorWithNotAuthorizedRole() throws Exception {
        this.mockMvc.perform(get(BASE_URL + "/users").header("Authorization", this.tokenUser)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testSaveNewUserSuccess() throws Exception {
        var userDto = new UserSaveDto(null, "test1", "password1", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(post(BASE_URL + "/users").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", this.tokenAdmin)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(userDto.username()))
                .andExpect(jsonPath("$.data.enabled").value(userDto.enable()))
                .andExpect(jsonPath("$.data.roles").value(userDto.roles()))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        this.mockMvc.perform(get(BASE_URL + "/users").header("Authorization", this.tokenAdmin)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testSaveNewUserErrorWithNotAuthorizedRole() throws Exception {
        var userDto = new UserSaveDto(1, "test1", "password1", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(post(BASE_URL + "/users").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", this.tokenUser)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        var userId = 1;
        this.mockMvc.perform(get(BASE_URL + "/users/" + userId)
                        .header("Authorization", this.tokenAdmin).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("user"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testFindUserByIdErrorWithNonExistentUserId() throws Exception {
        var invalidId = 5;
        this.mockMvc.perform(get(BASE_URL + "/users/" + invalidId)
                        .header("Authorization", this.tokenAdmin).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %d".formatted("HogwartsUser" ,invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindUserByIdErrorWithNotAuthorizedRole() throws Exception {
        var userId = 1;
        this.mockMvc.perform(get(BASE_URL + "/users/" + userId)
                        .header("Authorization", this.tokenUser).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        var userId = 3;
        var userDto = new UserDto(null, "test", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(BASE_URL + "/users/" + userId).header("Authorization", this.tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(userDto.username()))
                .andExpect(jsonPath("$.data.enabled").value(userDto.enabled()))
                .andExpect(jsonPath("$.data.roles").value(userDto.roles()))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testUpdateUserErrorWithNonExistentUserId() throws Exception {
        var invalidId = 5;
        var userDto = new UserDto(null, "test", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(BASE_URL + "/users/" + invalidId).header("Authorization", this.tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %d".formatted("HogwartsUser" ,invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateUserErrorWithNotAuthorizedRole() throws Exception {
        var userId = 3;
        var userDto = new UserDto(null, "test", true, "user");
        var userDtoJson = objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(BASE_URL + "/users/" + userId)
                        .header("Authorization", this.tokenUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType(userDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testDeleteUserByIdSuccess() throws Exception {
        var userId = 3;
        this.mockMvc.perform(delete(BASE_URL + "/users/" + userId)
                        .header("Authorization", this.tokenAdmin)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"))
                .andExpect(jsonPath("$.data").isEmpty());

        this.mockMvc.perform(get(BASE_URL + "/users").header("Authorization", this.tokenAdmin)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", Matchers.hasSize(2)));
    }

    @Test
    void testDeleteUserByIdErrorWithNonExistentUserId() throws Exception {
        var invalidId = 5;
        this.mockMvc.perform(delete(BASE_URL + "/users/" + invalidId)
                        .header("Authorization", this.tokenAdmin)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %d".formatted("HogwartsUser" ,invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserByIdErrorWithNotAuthorizedRole() throws Exception {
        var userId = 3;
        this.mockMvc.perform(delete(BASE_URL + "/users/" + userId)
                        .header("Authorization", this.tokenUser)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }



    private String generateToken(String username, String password) throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.BASE_URL + "/users/login")
                .with(httpBasic(username, password)));

        MvcResult mvcResult = resultActions.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(contentAsString);

        return "Bearer " + jsonObject.getJSONObject("data").getString("token");
    }
}