package com.booster.command.arguments.validator;

import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArguments;
import com.booster.service.LanguageService;
import com.booster.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.booster.command.Command.ADD_SETTINGS;

// todo: functional style error processing with no exceptions (Option).
//  have a list of validators that return an Option[], collect all errors
@Component
@RequiredArgsConstructor
public class AddSettingsArgValidator implements ArgValidator {

    private final LanguageService languageService;
    private final SettingsService settingsService;

    @Override
    public CommandWithArguments validate(CommandWithArguments commandWithArguments) {
        try {
            checkIfSettingsAlreadyExist();
            // todo: implement lid flag
            commandWithArguments.getId()
                    .ifPresent(this::checkIfLanguageBeingLearnedExistsWithId);

            return commandWithArguments;
        } catch (ArgsValidationException e) {
            return getCommandBuilder().argErrors(e.getArgErrors()).build();
        }
    }

    @Override
    public Command command() {
        return ADD_SETTINGS;
    }

    private void checkIfSettingsAlreadyExist() {
        if (settingsService.existAny()) {
            throw new ArgsValidationException(List.of("Settings already exist."));
        }
    }

    private void checkIfLanguageBeingLearnedExistsWithId(long id) {
        if (!languageService.existsWithId(id)) {
            throw new ArgsValidationException(List.of("Language being learned with id: " + id + " does not exist."));
        }
    }

}