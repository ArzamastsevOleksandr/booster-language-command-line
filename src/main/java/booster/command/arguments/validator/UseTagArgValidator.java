package booster.command.arguments.validator;

import booster.command.Command;
import booster.command.FlagType;
import booster.command.arguments.CommandWithArgs;
import booster.service.NoteService;
import booster.service.TagService;
import booster.service.VocabularyEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

import static booster.command.Command.USE_TAG;
import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class UseTagArgValidator implements ArgValidator {

    private final TagService tagService;
    private final NoteService noteService;
    private final VocabularyEntryService vocabularyEntryService;

    @Override
    public CommandWithArgs validateAndReturn(CommandWithArgs commandWithArgs) {
        commandWithArgs.getTag().ifPresentOrElse(this::checkIfTagIsPresent, () -> {
            throw new ArgsValidationException("Tag is missing");
        });
        checkThatAnyTargetIsPresent(commandWithArgs);
        commandWithArgs.getNoteId().ifPresent(this::checkIfNoteExistsWithId);
        commandWithArgs.getVocabularyEntryId().ifPresent(this::checkIfVocabularyEntryExistsWithId);
        return commandWithArgs;
    }

    private void checkThatAnyTargetIsPresent(CommandWithArgs commandWithArgs) {
        boolean noTargetsArePresent = Stream.of(commandWithArgs.getVocabularyEntryId(), commandWithArgs.getNoteId())
                .allMatch(Optional::isEmpty);

        if (noTargetsArePresent) {
            String flagTypes = Stream.of(FlagType.NOTE_ID, FlagType.VOCABULARY_ENTRY_ID)
                    .map(f -> f + "(" + f.value + ")")
                    .collect(joining(", "));
            throw new ArgsValidationException("At least one target must be specified when using tags: " + flagTypes);
        }
    }

    private void checkIfVocabularyEntryExistsWithId(Long id) {
        if (!vocabularyEntryService.existsWithId(id)) {
            throw new ArgsValidationException("Vocabulary entry does not exist with id: " + id);
        }
    }

    private void checkIfNoteExistsWithId(Long id) {
        if (!noteService.existsWithId(id)) {
            throw new ArgsValidationException("Note does not exist with id: " + id);
        }
    }

    private void checkIfTagIsPresent(String tag) {
        if (!tagService.existsWithName(tag)) {
            throw new ArgsValidationException("Tag does not exist: " + tag);
        }
    }

    @Override
    public Command command() {
        return USE_TAG;
    }

}