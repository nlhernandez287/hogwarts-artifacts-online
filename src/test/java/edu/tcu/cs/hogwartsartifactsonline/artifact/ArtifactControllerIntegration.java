package edu.tcu.cs.hogwartsartifactsonline.artifact;

import edu.tcu.cs.hogwartsartifactsonline.IntegrationTestConfig;
import edu.tcu.cs.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


public class ArtifactControllerIntegration extends IntegrationTestConfig {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;

    @Value("${api.endpoint.base-url}")
    String BASE_URL;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.BASE_URL + "/users/login")
                .with(httpBasic("admin", "password2")));

        MvcResult mvcResult = resultActions.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(contentAsString);
        this.token = "Bearer " + jsonObject.getJSONObject("data").getString("token");
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllArtifactsSuccess() throws Exception {
        this.mockMvc.perform(get(this.BASE_URL + "/artifacts").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find all success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
    }

    @Test
    void testFindArtifactByIdSuccess() throws Exception {
        var artifactId = "123451";
        this.mockMvc.perform(get(BASE_URL + "/artifacts/" + artifactId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value("123451"))
                .andExpect(jsonPath("$.data.name").value("Deluminator"))
                .andExpect(jsonPath("$.data.description").isNotEmpty())
                .andExpect(jsonPath("$.data.imageUrl").value("imageUrl"));
    }

    @Test
    void testFindArtifactByIdErrorWithNonExistentArtifactdId() throws Exception {
        var invalidId = "12";
        this.mockMvc.perform(get(BASE_URL + "/artifacts/" + invalidId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %s".formatted("Artifact", invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddArtifactSuccess() throws Exception {
        var artifactDto = new ArtifactDto(
                null,
                "Remembrall",
                "A Remembral was a magical large marble-sized glass ball",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        // When - Then
        this.mockMvc.perform(post(BASE_URL + "/artifacts").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", this.token).content(artifactDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value(artifactDto.name()))
                .andExpect(jsonPath("$.data.description").value(artifactDto.description()))
                .andExpect(jsonPath("$.data.imageUrl").value(artifactDto.imageUrl()));

        this.mockMvc.perform(get(this.BASE_URL + "/artifacts").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find all success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));
    }


    @Test
    @DisplayName("Check addArtifact with invalid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddArtifactErrorWithInvalidInput() throws Exception {
        Artifact a = new Artifact();
        a.setName(""); // Name is not provided.
        a.setDescription(""); // Description is not provided.
        a.setImageUrl(""); // ImageUrl is not provided.
        String json = this.objectMapper.writeValueAsString(a);
        this.mockMvc.perform(post(this.BASE_URL + "/artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.name").value("name is required."))
                .andExpect(jsonPath("$.data.description").value("description is required."))
                .andExpect(jsonPath("$.data.imageUrl").value("imageUrl is required."));
        this.mockMvc.perform(get(this.BASE_URL + "/artifacts").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
    }

    @Test
    void testUpdateArtifactSuccess() throws Exception {
        var artifactId = "123451";

        var artifactDto = new ArtifactDto(
                null,
                "Remembrall",
                "A Remembral was a magical large marble-sized glass ball",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        this.mockMvc.perform(put(BASE_URL + "/artifacts/" +  artifactId).header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.id").value(artifactId))
                .andExpect(jsonPath("$.data.name").value(artifactDto.name()))
                .andExpect(jsonPath("$.data.description").value(artifactDto.description()))
                .andExpect(jsonPath("$.data.imageUrl").value(artifactDto.imageUrl()));
    }

    @Test
    void testUpdateArtifactErrorWithNonExistentArtifactdId() throws Exception {
        var invalidId = "12";

        var artifactDto = new ArtifactDto(
                null,
                "Remembrall",
                "A Remembral was a magical large marble-sized glass ball",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        this.mockMvc.perform(put(BASE_URL + "/artifacts/" + invalidId).header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON).content(artifactDtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %s".formatted("Artifact", invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateArtifactErrorWhenInvalidTokenIdProvided() throws Exception {
        var artifactId = "123451";

        var artifactDto = new ArtifactDto(
                null,
                "Remembrall",
                "A Remembral was a magical large marble-sized glass ball",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        this.mockMvc.perform(put(BASE_URL + "/artifacts/" +  artifactId).header("Authorization", this.token + "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(artifactDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }

    @Test
    void testDeleteArtifactByIdSuccess() throws Exception {
        var artifactId = "123451";

        this.mockMvc.perform(delete(BASE_URL + "/artifacts/" + artifactId).header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"))
                .andExpect(jsonPath("$.data").isEmpty());

        this.mockMvc.perform(get(BASE_URL + "/artifacts/" + artifactId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %s".formatted("Artifact", artifactId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteArtifactByIdErrorWithNonExistentArtifactdId() throws Exception {
        var invalidId = "12";
        this.mockMvc.perform(delete(BASE_URL + "/artifacts/" + invalidId).header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %s".formatted("Artifact", invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindArtifactsByDescription() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "Hogwarts");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "2");
        requestParams.add("sort", "name,asc");

        // When and then
        this.mockMvc.perform(post(this.BASE_URL + "/artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    void testFindArtifactsByNameAndDescription() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("name", "Sword");
        searchCriteria.put("description", "Hogwarts");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "2");
        requestParams.add("sort", "name,asc");

        // When and then
        this.mockMvc.perform(post(this.BASE_URL + "/artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }


    @Test
    void testDeleteArtifactByIdErrorWhenInvalidTokenIdProvided() throws Exception {
        var artifactId = "123451";
        this.mockMvc.perform(delete(BASE_URL + "/artifacts/" + artifactId).header("Authorization", this.token + "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }

}