package com.booster.command.arguments.validator;

import com.booster.command.Command;
import com.booster.command.arguments.CommandWithArguments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.booster.command.Command.HELP;

@Component
@RequiredArgsConstructor
public class HelpArgValidator implements ArgValidator {

    @Override
    public CommandWithArguments validate(CommandWithArguments commandWithArguments) {
        return commandWithArguments;
    }

    @Override
    public Command command() {
        return HELP;
    }

}
