package edu.tcu.cs.hogwartsartifactsonline.wizard;

public class WizardUtils {
    protected static Wizard generateWizard(Integer id, String name) {
        var wizard = new Wizard();
        wizard.setId(id);
        wizard.setName(name);

        return wizard;
    }
}
