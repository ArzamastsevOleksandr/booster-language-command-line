package cliclient.launcher;

import api.exception.HttpErrorResponse;
import cliclient.adapter.CommandLineAdapter;
import cliclient.command.Command;
import cliclient.command.arguments.CommandWithArgs;
import cliclient.command.service.CommandHandlerCollectionService;
import cliclient.config.PropertyHolder;
import cliclient.parser.CommandLineInputTransformer;
import cliclient.postprocessor.CommandWithArgsPostProcessor;
import cliclient.service.SessionTrackerService;
import cliclient.util.ColorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Launcher {

    private final CommandHandlerCollectionService commandHandlerCollectionService;
    private final CommandLineAdapter adapter;
    private final CommandWithArgsPostProcessor postProcessor;
    private final SessionTrackerService sessionTrackerService;
    private final CommandLineInputTransformer transformer;
    private final ObjectMapper objectMapper;
    private final PropertyHolder propertyHolder;

    public void launch() {
        adapter.writeLine(ColorCodes.cyan("Welcome to the " + propertyHolder.getAppName() + "!"));
        adapter.help();
        userInteractions();
        adapter.writeLine(sessionTrackerService.getStatistics());
        adapter.newLine();
        adapter.writeLine("See you next time!");
    }

    private void userInteractions() {
        CommandWithArgs commandWithArgs = readInputAndParseToCommandWithArgs();
        while (commandWithArgs.getCommand() != Command.EXIT) {
            handleCommandWithArgs(commandWithArgs);
            commandWithArgs = readInputAndParseToCommandWithArgs();
        }
    }

    private void handleCommandWithArgs(CommandWithArgs commandWithArgs) {
        try {
            commandHandlerCollectionService.handle(commandWithArgs);
        } catch (Throwable t) {
            if (t instanceof FeignException.FeignClientException) {
                try {
                    var e = (FeignException.FeignClientException) t;
                    var httpErrorResponse = objectMapper.readValue(e.contentUTF8().getBytes(), HttpErrorResponse.class);
                    adapter.error(httpErrorResponse.getMessage());
                } catch (IOException ioe) {
                    adapter.error(t.getMessage());
                    adapter.error(ioe.getMessage());
                }
            } else if (t instanceof RetryableException) {
                adapter.error(t.getCause().getMessage());
            } else {
                adapter.error(t.getMessage());
                adapter.newLine();
            }
        }
    }

    // todo: if settings service is down, process exception
    private CommandWithArgs readInputAndParseToCommandWithArgs() {
        adapter.write(ColorCodes.purple(">> "));
        String input = adapter.readLine();
        CommandWithArgs commandWithArgs = transformer.toCommandWithArgs(input);
        return commandWithArgs.hasNoErrors() ? postProcessor.process(commandWithArgs) : commandWithArgs;
    }

}
