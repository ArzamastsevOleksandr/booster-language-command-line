package cliclient.command.arguments.validator;

import cliclient.command.Command;
import cliclient.command.arguments.CommandWithArgs;
import org.springframework.stereotype.Component;

import static cliclient.command.Command.LIST_TAGS;

@Component
public class ListTagsArgValidator implements ArgValidator {

    @Override
    public CommandWithArgs validateAndReturn(CommandWithArgs commandWithArgs) {
        return commandWithArgs;
    }

    @Override
    public Command command() {
        return LIST_TAGS;
    }

}