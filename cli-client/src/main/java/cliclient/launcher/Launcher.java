package cliclient.launcher;

import api.exception.HttpErrorResponse;
import cliclient.adapter.CommandLineAdapter;
import cliclient.command.Command;
import cliclient.command.arguments.CommandWithArgs;
import cliclient.command.service.CommandHandlerCollectionService;
import cliclient.parser.CommandLineInputTransformer;
import cliclient.postprocessor.CommandWithArgsPostProcessor;
import cliclient.service.SessionTrackerService;
import cliclient.util.ColorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static cliclient.command.Command.HELP;

@Component
@RequiredArgsConstructor
public class Launcher {

    private final CommandHandlerCollectionService commandHandlerCollectionService;
    private final CommandLineAdapter adapter;
    private final CommandWithArgsPostProcessor postProcessor;
    private final SessionTrackerService sessionTrackerService;
    private final CommandLineInputTransformer transformer;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String appName;

    public void launch() {
        adapter.writeLine(ColorCodes.cyan("Welcome to the " + appName + "!"));
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
            } else {
                adapter.error(t.getMessage());
            }
        }
    }

    private CommandWithArgs readInputAndParseToCommandWithArgs() {
        adapter.write(ColorCodes.purple(">> "));
        String input = adapter.readLine();
        CommandWithArgs commandWithArgs = transformer.toCommandWithArgs(input);
        return commandWithArgs.hasNoErrors() ? postProcessor.process(commandWithArgs) : commandWithArgs;
    }

}
