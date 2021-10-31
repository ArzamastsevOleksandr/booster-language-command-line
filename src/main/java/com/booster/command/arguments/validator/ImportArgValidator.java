package com.booster.command.arguments.validator;

import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArguments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static com.booster.command.Command.IMPORT;

@Component
@RequiredArgsConstructor
public class ImportArgValidator implements ArgValidator {

    private static final String XLSX = ".xlsx";
    private static final String DEFAULT_IMPORT_FILE = "import" + XLSX;

    @Override
    public CommandWithArguments validate(CommandWithArguments commandWithArguments) {
        try {
            commandWithArguments.getFilename()
                    .ifPresentOrElse(this::checkCustomFileExists, this::checkDefaultImportFileExists);

            return commandWithArguments.toBuilder().filename(DEFAULT_IMPORT_FILE).build();
        } catch (ArgsValidationException e) {
            return getCommandBuilder().argErrors(e.getArgErrors()).build();
        }
    }

    private void checkDefaultImportFileExists() {
        File file = new File(DEFAULT_IMPORT_FILE);
        if (!file.exists() || file.isDirectory()) {
            throw new ArgsValidationException(List.of(
                    "Default import file not found: " + DEFAULT_IMPORT_FILE,
                    "Try specifying custom filename")
            );
        }
    }

    private void checkCustomFileExists(String filename) {
        File file = new File(filename);
        if (!file.exists() || file.isDirectory()) {
            throw new ArgsValidationException(List.of("Custom import file not found: " + DEFAULT_IMPORT_FILE));
        }
    }

    @Override
    public Command command() {
        return IMPORT;
    }

}
