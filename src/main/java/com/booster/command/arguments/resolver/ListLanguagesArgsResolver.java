package com.booster.command.arguments.resolver;

import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArguments;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.booster.command.Command.LIST_LANGUAGES;

@Component
public class ListLanguagesArgsResolver implements ArgsResolver {

    @Override
    public CommandWithArguments resolve(List<String> args) {
        // for now no args exist
        return getCommandBuilder().build();
    }

    @Override
    public Command command() {
        return LIST_LANGUAGES;
    }

}
