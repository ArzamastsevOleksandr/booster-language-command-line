package cliclient.command.handler;

import cliclient.command.Command;
import cliclient.command.arguments.CommandArgs;
import cliclient.command.arguments.DownloadCommandArgs;
import cliclient.feign.upload.DownloadServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DownloadCommandHandler implements CommandHandler {

    private final DownloadServiceClient downloadServiceClient;

    // todo: if the download file already exists - warn and ask for confirmation
    @Override
    public void handle(CommandArgs commandArgs) {
        var args = (DownloadCommandArgs) commandArgs;
        byte[] bytes = downloadServiceClient.download();
        // todo: settings + properties
        try (var out = new FileOutputStream(args.filename())) {
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Command getCommand() {
        return Command.DOWNLOAD;
    }

}
