package booster.parser;

import booster.command.Command;
import booster.command.FlagType;
import booster.command.arguments.CommandWithArgs;
import booster.command.arguments.TrainingSessionMode;
import booster.util.ObjectUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

// todo: analyzer util: smartToString. Get all fields via reflection, group them by null, not null.
//  Use that for analysis.
@Component
class TokenSequenceTransformer {

    CommandWithArgs transform(List<Token> tokens) {
        ObjectUtil.requireNonNullOrElseThrowIAE(tokens, "tokens can not be null");

        Token token = tokens.get(0);
        Command command = Command.fromString(token.getValue());
        if (tokens.size() == 1) {
            return CommandWithArgs.singleCommand(command);
        }

        var argumentsBuilder = CommandWithArgs.builder()
                .command(command);

        List<Token> copy = tokens.subList(1, tokens.size());

        for (int i = 0; i < copy.size(); i += 3) {
            Token flag = copy.get(i);
            Token value = copy.get(i + 2);

            FlagType flagType = FlagType.fromString(flag.getValue());
            String flagValue = value.getValue();
            argumentsBuilder = switch (flagType) {
                case ID -> argumentsBuilder.id(Long.parseLong(flagValue));
                case LANGUAGE_ID -> argumentsBuilder.languageId(Long.parseLong(flagValue));
                case NOTE_ID -> argumentsBuilder.noteId(Long.parseLong(flagValue));
                case VOCABULARY_ENTRY_ID -> argumentsBuilder.vocabularyEntryId(Long.parseLong(flagValue));
                case NAME -> argumentsBuilder.name(flagValue);
                case TAG -> argumentsBuilder.tag(flagValue);
                case DESCRIPTION -> argumentsBuilder.definition(flagValue);
                case FILE -> argumentsBuilder.filename(flagValue);
                case MODE -> argumentsBuilder.mode(TrainingSessionMode.fromString(flagValue));
                case SYNONYMS -> argumentsBuilder.synonyms(getWordEquivalentNames(flagValue));
                case ANTONYMS -> argumentsBuilder.antonyms(getWordEquivalentNames(flagValue));
                case CONTENT -> argumentsBuilder.content(flagValue);
                case CORRECT_ANSWERS_COUNT -> argumentsBuilder.correctAnswersCount(Integer.parseInt(flagValue));
                case ADD_ANTONYMS -> argumentsBuilder.addAntonyms(getWordEquivalentNames(flagValue));
                case ADD_SYNONYMS -> argumentsBuilder.addSynonyms(getWordEquivalentNames(flagValue));
                case REMOVE_ANTONYMS -> argumentsBuilder.removeAntonyms(getWordEquivalentNames(flagValue));
                case REMOVE_SYNONYMS -> argumentsBuilder.removeSynonyms(getWordEquivalentNames(flagValue));
                case PAGINATION -> argumentsBuilder.pagination(Integer.parseInt(flagValue));
                case SUBSTRING -> argumentsBuilder.substring(flagValue);
                case CONTEXTS -> argumentsBuilder.contexts(getContexts(flagValue));
                default -> throw new RuntimeException("Flag does not have a handler: " + flagType);
            };
        }
        return argumentsBuilder.build();
    }

    private Set<String> getContexts(String value) {
        return Arrays.stream(value.split(Token.CONTEXT_DELIMITER))
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .collect(toSet());
    }

    private Set<String> getWordEquivalentNames(String value) {
        return Arrays.stream(value.split(Token.WORD_EQUIVALENT_DELIMITER))
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .collect(toSet());
    }

}