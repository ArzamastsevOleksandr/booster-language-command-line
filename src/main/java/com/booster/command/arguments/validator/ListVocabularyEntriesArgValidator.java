package com.booster.command.arguments.validator;

import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArgs;
import com.booster.service.VocabularyEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.booster.command.Command.LIST_VOCABULARY_ENTRIES;

@Component
@RequiredArgsConstructor
public class ListVocabularyEntriesArgValidator implements ArgValidator {

    private final VocabularyEntryService vocabularyEntryService;

    @Override
    public CommandWithArgs validateAndReturn(CommandWithArgs commandWithArgs) {
        commandWithArgs.getId().ifPresent(id -> {
            checkIfVocabularyEntryExistsWithId(id);
            checkIfSubstringFlagIsUsed(commandWithArgs);
        });

        return commandWithArgs;
    }

    private void checkIfVocabularyEntryExistsWithId(long id) {
        if (!vocabularyEntryService.existsWithId(id)) {
            throw new ArgsValidationException("Vocabulary entry does not exist with id: " + id);
        }
    }

    private void checkIfSubstringFlagIsUsed(CommandWithArgs commandWithArgs) {
        commandWithArgs.getSubstring().ifPresent(s -> {
            throw new ArgsValidationException("Only one of id or substring flags can be used.");
        });
    }

    @Override
    public Command command() {
        return LIST_VOCABULARY_ENTRIES;
    }

}
