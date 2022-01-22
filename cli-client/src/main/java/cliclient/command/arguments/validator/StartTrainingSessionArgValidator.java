package cliclient.command.arguments.validator;

import cliclient.command.Command;
import cliclient.command.arguments.CommandWithArgs;
import cliclient.command.arguments.TrainingSessionMode;
import cliclient.service.VocabularyEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static cliclient.command.Command.START_TRAINING_SESSION;

@Component
@RequiredArgsConstructor
public class StartTrainingSessionArgValidator implements ArgValidator {

    private final VocabularyEntryService vocabularyEntryService;

    @Override
    public CommandWithArgs validateAndReturn(CommandWithArgs commandWithArgs) {
//        if (commandWithArgs.getMode().isEmpty()) {
//            checkIfEntriesExistForMode(TrainingSessionMode.getDefaultMode());
//            return commandWithArgs.toBuilder().mode(TrainingSessionMode.getDefaultMode()).build();
//        }
//        checkIfEntriesExistForMode(commandWithArgs.getMode().get());
        return commandWithArgs;
    }

    private void checkIfEntriesExistForMode(TrainingSessionMode mode) {
        if (!vocabularyEntryService.existAnyForTrainingMode(mode)) {
            throw new ArgsValidationException("No entries exist for mode: " + mode);
        }
    }

    @Override
    public Command command() {
        return START_TRAINING_SESSION;
    }

}
