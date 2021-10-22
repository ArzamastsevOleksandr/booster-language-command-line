package com.booster.command.arguments;

import com.booster.command.Command;
import com.booster.command.arguments.resolver.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommandArgumentsResolver {

    private static final CommandWithArguments UNRECOGNIZED = CommandWithArguments.builder()
            .command(Command.UNRECOGNIZED)
            .build();

    private final ListLanguagesBeingLearnedArgsResolver listLanguagesBeingLearnedArgsResolver;
    private final AddLanguageBeingLearnedArgsResolver addLanguageBeingLearnedArgsResolver;
    private final DeleteLanguageBeingLearnedArgsResolver deleteLanguageBeingLearnedArgsResolver;

    private final ListVocabulariesArgsResolver listVocabulariesArgsResolver;
    private final AddVocabularyArgsResolver addVocabularyArgsResolver;
    private final DeleteVocabularyArgsResolver deleteVocabularyArgsResolver;

    private final ListVocabularyEntriesArgsResolver listVocabularyEntriesArgsResolver;
    private final AddVocabularyEntryArgsResolver addVocabularyEntryArgsResolver;
    private final DeleteVocabularyEntryArgsResolver deleteVocabularyEntryArgsResolver;

    private final StartTrainingSessionArgsResolver startTrainingSessionArgsResolver;

    // todo: custom annotation ForHandler(.class) with reflection
    public CommandWithArguments resolve(String line) {
        List<String> commandWithArguments = parseCommandAndArguments(line);

        if (commandWithArguments.isEmpty()) {
            return UNRECOGNIZED;
        } else {
            Command command = getCommand(commandWithArguments);
            List<String> args = getArgs(commandWithArguments);

            switch (command) {
                case LIST_LANGUAGES_BEING_LEARNED:
                    return listLanguagesBeingLearnedArgsResolver.resolve(args);
                case ADD_LANGUAGE_BEING_LEARNED:
                    return addLanguageBeingLearnedArgsResolver.resolve(args);
                case DELETE_LANGUAGE_BEING_LEARNED:
                    return deleteLanguageBeingLearnedArgsResolver.resolve(args);

                case LIST_VOCABULARIES:
                    return listVocabulariesArgsResolver.resolve(args);
                case ADD_VOCABULARY:
                    return addVocabularyArgsResolver.resolve(args);
                case DELETE_VOCABULARY:
                    return deleteVocabularyArgsResolver.resolve(args);

                case LIST_VOCABULARY_ENTRIES:
                    return listVocabularyEntriesArgsResolver.resolve(args);
                case ADD_VOCABULARY_ENTRY:
                    return addVocabularyEntryArgsResolver.resolve(args);
                case DELETE_VOCABULARY_ENTRY:
                    return deleteVocabularyEntryArgsResolver.resolve(args);

                case START_TRAINING_SESSION:
                    return startTrainingSessionArgsResolver.resolve(args);

                case HELP:

                case EXIT:

                case LIST_LANGUAGES:

                case LIST_WORDS:
                    return CommandWithArguments.builder()
                            .command(command)
                            .build();
                default:
                    return UNRECOGNIZED;
            }
        }
    }

    private List<String> parseCommandAndArguments(String line) {
        return Arrays.stream(line.split(" "))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private Command getCommand(List<String> commandWithArguments) {
        String commandString = commandWithArguments.get(0);
        return Command.fromString(commandString);
    }

    private List<String> getArgs(List<String> commandWithArguments) {
        return commandWithArguments.subList(1, commandWithArguments.size());
    }

}
