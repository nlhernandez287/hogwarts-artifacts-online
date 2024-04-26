package edu.tcu.cs.hogwartsartifactsonline.system;

import edu.tcu.cs.hogwartsartifactsonline.artifact.Artifact;
import edu.tcu.cs.hogwartsartifactsonline.artifact.ArtifactRepository;
import edu.tcu.cs.hogwartsartifactsonline.wizard.Wizard;
import edu.tcu.cs.hogwartsartifactsonline.wizard.WizardRepository;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.HogwartsUser;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DBDataInitializer implements CommandLineRunner {

    private final ArtifactRepository artifactRepository;

    private final WizardRepository wizardRepository;

    private final UserService userService;

    public DBDataInitializer(ArtifactRepository artifactRepository, WizardRepository wizardRepository, UserService userService) {
        this.artifactRepository = artifactRepository;
        this.wizardRepository = wizardRepository;
        this.userService = userService;
    }


    @Override
    public void run(String... args) throws Exception {


        var a1 = generateArtifact("123451",
                "Deluminator",
                "The Deluminator,[1] also known as the Put-Outer,[3] was a magical device used by Albus Dumbledore " +
                        "to remove light sources from the Deluminator's immediate surroundings",
                "imageUrl");
        var a2 = generateArtifact("123452",
                "Invisibility Cloak",
                "An Invisibility Cloak was a magical " +
                        "garment which rendered whomever or whatever it covered invisible.",
                "imageUrl");
        var a3 = generateArtifact("123453",
                "Elder Wand",
                "The Elder Wand was one of three magical objects that made up the fabled Deathly Hallows, " +
                        "along with the Resurrection Stone and the Cloak of Invisibility.",
                "imageUrl");
        var a4 = generateArtifact("123454",
                "The Marauder's Map",
                "The Marauder's Map was a magical document that revealed all of Hogwarts School of Witchcraft and Wizardry.",
                "imageUrl");
        var a5 = generateArtifact("123455",
                "The Sword Of Gryffindor",
                "The Sword of Gryffindor was a thousand-year-old, " +
                        "goblin-made magical sword owned by the famed wizard Godric Gryffindor, " +
                        "one of the four founders of Hogwarts School of Witchcraft and Wizardry", "imageUrl");
        var a6 = generateArtifact("123456",
                "The Resurrection Stone",
                "The Resurrection Stone was said to be the only object that would bring back the spirits of the holder's deceased loved ones", "imageUrl");

        var w1 = generateWizard(1, "Albus Dumbledore");
        w1.addArtifact(a1);
        w1.addArtifact(a3);

        var w2 = generateWizard(2, "Harry Potter");
        w2.addArtifact(a2);
        w2.addArtifact(a4);

        var w3 = generateWizard(3, "Nevile Longbottom");
        w3.addArtifact(a5);

        wizardRepository.save(w1);
        wizardRepository.save(w2);
        wizardRepository.save(w3);

        artifactRepository.save(a6);

        var user1 = createUser(1, "user1", "password1", true, "user");
        var user2 = createUser(2, "admin", "password2", true, "admin");
        var user3 = createUser(3, "user2", "password3", true, "user admin");

        userService.save(user1);
        userService.save(user2);
        userService.save(user3);
    }

    private static Artifact generateArtifact(String id, String name, String description, String imageUrl) {
        var a = new Artifact();
        a.setId(id);
        a.setName(name);
        a.setDescription(description);
        a.setImageUrl(imageUrl);
        return a;
    }

    private static Wizard generateWizard(Integer id, String name) {
        var w = new Wizard();
        w.setId(id);
        w.setName(name);
        return w;
    }
    private static HogwartsUser createUser(Integer id,
                                           String username,
                                           String password,
                                           boolean enable,
                                           String roles) {
        var newUser = new HogwartsUser();
        newUser.setId(id);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEnable(enable);
        newUser.setRoles(roles);
        return newUser;
    }
}