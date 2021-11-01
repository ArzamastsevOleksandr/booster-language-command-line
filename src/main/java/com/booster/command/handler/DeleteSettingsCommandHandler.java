package com.booster.command.handler;

import com.booster.adapter.CommandLineAdapter;
import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArgs;
import com.booster.dao.SettingsDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteSettingsCommandHandler implements CommandHandler {

    private final SettingsDao settingsDao;

    private final CommandLineAdapter adapter;

    @Override
    public void handle(CommandWithArgs commandWithArgs) {
        if (commandWithArgs.hasNoErrors()) {
            settingsDao.deleteAll();
            adapter.writeLine("Done.");
        } else {
            adapter.writeLine("Errors: ");
            adapter.newLine();
            commandWithArgs.getErrors()
                    .forEach(adapter::writeLine);
        }
        adapter.newLine();
    }

    @Override
    public Command getCommand() {
        return Command.DELETE_SETTINGS;
    }

}
