package com.booster.command.arguments.validator;

import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArguments;
import com.booster.service.VocabularyEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.booster.command.Command.LIST_VOCABULARY_ENTRIES;

@Component
@RequiredArgsConstructor
public class ListVocabularyEntriesArgValidator implements ArgValidator {

    private final VocabularyEntryService vocabularyEntryService;

    @Override
    public CommandWithArguments validate(CommandWithArguments commandWithArguments) {
        try {
            commandWithArguments.getId()
                    .ifPresent(this::checkIfVocabularyEntryExistsWithId);

            return commandWithArguments;
        } catch (ArgsValidationException e) {
            return getCommandBuilder().argErrors(e.getArgErrors()).build();
        }
    }

    private void checkIfVocabularyEntryExistsWithId(long id) {
        if (!vocabularyEntryService.existsWithId(id)) {
            throw new ArgsValidationException(List.of("Vocabulary entry does not exist with id: " + id));
        }
    }

    @Override
    public Command command() {
        return LIST_VOCABULARY_ENTRIES;
    }

}