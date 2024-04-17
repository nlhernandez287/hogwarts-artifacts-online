package edu.tcu.cs.hogwartsartifactsonline.wizard.converter;

import edu.tcu.cs.hogwartsartifactsonline.wizard.dto.WizardDto;
import edu.tcu.cs.hogwartsartifactsonline.wizard.Wizard;
import org.springframework.core.convert.converter.Converter;

public class WizardDtoToWizardConverter implements Converter<WizardDto, Wizard> {

    @Override
    public Wizard convert(WizardDto source){
        Wizard wizard = new Wizard();
        wizard.setId(source.id());
        wizard.setName(source.name());
        return wizard;
    }
}
