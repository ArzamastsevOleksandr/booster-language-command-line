package cliclient.command.handler;

import api.settings.CreateSettingsInput;
import api.settings.SettingsApi;
import api.settings.SettingsDto;
import cliclient.adapter.CommandLineAdapter;
import cliclient.command.Command;
import cliclient.command.arguments.AddSettingsCommandArgs;
import cliclient.command.arguments.CommandArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddSettingsCommandHandler implements CommandHandler {

    private final CommandLineAdapter adapter;
    private final SettingsApi settingsApi;

    @Override
    public void handle(CommandArgs commandArgs) {
        var args = (AddSettingsCommandArgs) commandArgs;
        SettingsDto settingsDto = settingsApi.create(CreateSettingsInput.builder()

                .defaultLanguageName(args.getDefaultLanguageName())

                .entriesPerVocabularyTrainingSession(args.getEntriesPerVocabularyTrainingSession())

                .tagsPagination(args.getTagsPagination())
                .notesPagination(args.getNotesPagination())
                .vocabularyPagination(args.getVocabularyPagination())
                .languagesPagination(args.getLanguagesPagination())

                .build());
        adapter.writeLine(settingsDto);
    }

    @Override
    public Command getCommand() {
        return Command.ADD_SETTINGS;
    }

}
