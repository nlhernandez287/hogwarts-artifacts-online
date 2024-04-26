package edu.tcu.cs.hogwartsartifactsonline.artifact;

import edu.tcu.cs.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwartsartifactsonline.artifact.utils.IdWorker;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.ChatClient;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.ChatRequest;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.ChatResponse;
import edu.tcu.cs.hogwartsartifactsonline.client.ia.chat.dto.Message;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ArtifactService {
    private final ArtifactRepository artifactRepository;

    private final ChatClient chatClient;

    private final IdWorker idWorker;

    public ArtifactService(ArtifactRepository artifactRepository, ChatClient chatClient, IdWorker idWorker) {
        this.artifactRepository = artifactRepository;
        this.chatClient = chatClient;
        this.idWorker = idWorker;
    }

    public Artifact findById(String artifactId){
        return this.artifactRepository.findById(artifactId)
                .orElseThrow(()->new ObjectNotFoundException("Artifact", artifactId));

    }
    public List<Artifact> findAll(){
        return this.artifactRepository.findAll();
    }

    public Artifact save(Artifact newArtifact){
        newArtifact.setId(idWorker.nextId() + "");
        return this.artifactRepository.save(newArtifact);
    }

    public Artifact update(String artifactId, Artifact update){
       return this.artifactRepository.findById(artifactId)
               .map(oldArtifact -> {
                   oldArtifact.setName(update.getName());
                   oldArtifact.setDescription(update.getDescription());
                   oldArtifact.setImageUrl(update.getImageUrl());
                   return this.artifactRepository.save(oldArtifact);
               })
               .orElseThrow(() -> new ObjectNotFoundException("Artifact", artifactId));
    }

    public void delete(String artifactId){
    Artifact artifact = this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("Artifact" ,artifactId));
    this.artifactRepository.deleteById(artifactId);
    }

    public String summarize(List<ArtifactDto> artifactDtos) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(artifactDtos);

        var prompt = "Your task is to generate a short summary of a given JSON array";

        var messages = List.of(new Message("system", prompt), new Message("user", jsonArray));
        var chatRequest = new ChatRequest("gpt-3.5-turbo", messages);

        ChatResponse chatResponse = this.chatClient.generate(chatRequest);

        return chatResponse.choices().get(0).message().content();
    }

}
