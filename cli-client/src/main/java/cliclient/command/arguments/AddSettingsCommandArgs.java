package cliclient.command.arguments;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddSettingsCommandArgs implements CommandArgs {
    String defaultLanguageName;

    Integer entriesPerVocabularyTrainingSession;

    Integer vocabularyPagination;
    Integer notesPagination;
    Integer languagesPagination;
    Integer tagsPagination;
}
