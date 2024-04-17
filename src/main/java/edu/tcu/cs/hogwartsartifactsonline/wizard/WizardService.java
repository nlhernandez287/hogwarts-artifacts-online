package edu.tcu.cs.hogwartsartifactsonline.wizard;

import edu.tcu.cs.hogwartsartifactsonline.artifact.Artifact;
import edu.tcu.cs.hogwartsartifactsonline.artifact.ArtifactRepository;
import edu.tcu.cs.hogwartsartifactsonline.system.Result;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import edu.tcu.cs.hogwartsartifactsonline.wizard.converter.WizardDtoToWizardConverter;
import edu.tcu.cs.hogwartsartifactsonline.wizard.converter.WizardToWizardDtoConverter;
import edu.tcu.cs.hogwartsartifactsonline.wizard.dto.WizardDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class WizardService {
    private final WizardRepository wizardRepository;

    private final ArtifactRepository artifactRepository;

    public WizardService(WizardRepository wizardRepository, ArtifactRepository artifactRepository) {
        this.wizardRepository = wizardRepository;
        this.artifactRepository = artifactRepository;
    }

    public List<Wizard> findAll(){
        return this.wizardRepository.findAll();
    }

    public Wizard findById(Integer wizardId){
        return this.wizardRepository.findById(wizardId)
                .orElseThrow(() -> new ObjectNotFoundException("wizard",wizardId));
    }

    public Wizard save(Wizard newWizard){
        return this.wizardRepository.save(newWizard);
    }

    public Wizard update(Integer wizardId, Wizard update){
        var oldWizard = this.findById(wizardId);
        oldWizard.setName(update.getName());
        return this.wizardRepository.save(oldWizard);
    }

    public void delete(Integer wizardId){
        Wizard wizardToBeDeleted = this.wizardRepository.findById(wizardId)
                .orElseThrow(()->new ObjectNotFoundException("wizard", wizardId));

        wizardToBeDeleted.removeAllArtifact();
        this.wizardRepository.deleteById(wizardId);
    }

    public void assignArtifact(Integer wizardId, String artifactId){
        // Find this artifact by Id from DB.
        Artifact artifactToBeAssigned = this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

        // Find this wizard by Id from DB.
        Wizard wizard = this.wizardRepository.findById(wizardId)
                .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

        // Artifact assignment
        // We need to see if the artifact is already owned by some wizard.
        if (artifactToBeAssigned.getOwner() != null) {
            artifactToBeAssigned.getOwner().removeArtifact(artifactToBeAssigned);
        }
        wizard.addArtifact(artifactToBeAssigned);
    }
}
