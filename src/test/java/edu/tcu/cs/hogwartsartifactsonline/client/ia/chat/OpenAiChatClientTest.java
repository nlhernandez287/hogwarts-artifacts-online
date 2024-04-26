package edu.tcu.cs.hogwartsartifactsonline.client.ia.chat;

import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.ChatRequest;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.ChatResponse;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.Choice;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(OpenAiChatClient.class)
class OpenAiChatClientTest {

    @Autowired
    private OpenAiChatClient openAiChatClient;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private ObjectMapper objectMapper;

    private String url;

    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        url = "https://api.openai.com/v1/chat/completions";

        chatRequest = new ChatRequest(
                "gpt-4", List.of(
                new Message("system", ""),
                new Message("user", "A json array"))
        );
    }

    @Test
    void testGenerateSuccess() throws JsonProcessingException {
        // Given
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Choice(0, new Message("assistant", "The summary includes..."))));

        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", startsWith("Bearer ")))
                .andExpect(content().json(this.objectMapper.writeValueAsString(this.chatRequest)))
                .andRespond(withSuccess(this.objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON));

        // When
        ChatResponse generateChatResponse = this.openAiChatClient.generate(this.chatRequest);

        // Then
        this.mockRestServiceServer.verify();
        assertThat(generateChatResponse.choices().get(0).message().content()).isEqualTo("The summary includes...");
    }

    @Test
    void testGenerateUnauthorizedRequest() {
        // Given
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withUnauthorizedRequest());

        // When
        Throwable throwable = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.openAiChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void testGenerateToManyRequest() {
        // Given
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withTooManyRequests());

        // When
        Throwable throwable = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.openAiChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    @Test
    void testGenerateInternalServerError() {
        // Given
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // When
        Throwable throwable = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.openAiChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify();
        assertThat(throwable).isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    @Test
    void testGenerateServiceUnavailable() {
        // Given
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServiceUnavailable());

        // When
        Throwable throwable = catchThrowable(() -> {
            ChatResponse generatedChatResponse = this.openAiChatClient.generate(this.chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify();
        assertThat(throwable).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }
}