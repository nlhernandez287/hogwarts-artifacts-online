package edu.tcu.cs.hogwartsartifactsonline.client.ia.chat;

import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.ChatRequest;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.ChatResponse;

public interface ChatClient {

    ChatResponse generate(ChatRequest chatRequest);
}