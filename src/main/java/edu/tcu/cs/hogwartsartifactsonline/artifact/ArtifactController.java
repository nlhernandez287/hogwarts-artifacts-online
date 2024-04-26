package edu.tcu.cs.hogwartsartifactsonline.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.Authenticator;
import edu.tcu.cs.hogwartsartifactsonline.artifact.converter.ArtifactDtoToArtifactConverter;
import edu.tcu.cs.hogwartsartifactsonline.artifact.converter.ArtifactToArtifactDtoConverter;
import edu.tcu.cs.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwartsartifactsonline.system.Result;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import jdk.jshell.Snippet;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {
    private final ArtifactService artifactService;
    private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;

    private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;

    public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter, ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter) {
        this.artifactService = artifactService;
        this.artifactToArtifactDtoConverter = artifactToArtifactDtoConverter;
        this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
    }

    @GetMapping("/{artifactId}")
    public Result findArtifactById(@PathVariable String artifactId){
        Artifact foundArtifact = this.artifactService.findById(artifactId);
        ArtifactDto artifactDto = this.artifactToArtifactDtoConverter.convert(foundArtifact);
        return new Result(true, StatusCode.SUCCESS, "Find one success", artifactDto);
    }
    @GetMapping
    public Result findAllArtifacts(){
        List<Artifact> foundArtifacts = this.artifactService.findAll();
        //Convert foundArtifacts to a list of artifactDtos
        List <ArtifactDto> artifactDtos = foundArtifacts.stream()
                .map(foundArtifact ->
                        this.artifactToArtifactDtoConverter.convert(foundArtifact))
                .collect(Collectors.toList());
        return new Result(true, StatusCode.SUCCESS, "Find all success", artifactDtos);
    }

    @PostMapping
    public Result addArtifact(@RequestBody ArtifactDto artifactDto){
       //Convert artifactDto to artifact
       Artifact newArtifact = this.artifactDtoToArtifactConverter.convert(artifactDto);
       Artifact savedArtifact = this.artifactService.save(newArtifact);
       ArtifactDto savedArtifactDto = this.artifactToArtifactDtoConverter.convert(savedArtifact);
       return new Result(true, StatusCode.SUCCESS, "Add success", savedArtifactDto);
    }
    @PutMapping("/{artifactId}")
    public Result updateArtifact(@PathVariable String artifactId, @Validated @RequestBody ArtifactDto artifactDto){
        Artifact update = this.artifactDtoToArtifactConverter.convert(artifactDto);
        Artifact updatedArtifact = this.artifactService.update(artifactId, Objects.requireNonNull(update));
        ArtifactDto updatedArtifactDto = this.artifactToArtifactDtoConverter.convert(updatedArtifact);
        return new Result(true, StatusCode.SUCCESS, "Update success", updatedArtifactDto);
    }
    @DeleteMapping("/{artifactId}")
    public Result deleteArtifact(@PathVariable String artifactId){
        this.artifactService.delete(artifactId);
        return new Result(true, StatusCode.SUCCESS, "Delete success");
    }

    @GetMapping("/summary")
    public Result summarizeArtifacts() throws JsonProcessingException{
        var foundArtifacts = this.artifactService.findAll();
        var artifactsDtos = foundArtifacts.stream().map(artifactToArtifactDtoConverter::convert).toList();

        String artifactsSummary = this.artifactService.summarize(artifactsDtos);
        return new Result(true, StatusCode.SUCCESS, "Summarize success", artifactsSummary);
    }
}
