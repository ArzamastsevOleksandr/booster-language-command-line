package cliclient.command.handler;

import cliclient.adapter.CommandLineAdapter;
import cliclient.command.Command;
import cliclient.command.arguments.CommandArgs;
import cliclient.command.arguments.ListVocabularyEntriesCommandArgs;
import cliclient.model.VocabularyEntry;
import cliclient.service.ColorProcessor;
import cliclient.service.VocabularyEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class ListVocabularyEntriesCommandHandler implements CommandHandler {

    private final VocabularyEntryService vocabularyEntryService;
    private final CommandLineAdapter adapter;
    private final ColorProcessor colorProcessor;

    // todo: assert that pagination is present always
    @Override
    public void handle(CommandArgs commandArgs) {
        var args = (ListVocabularyEntriesCommandArgs) commandArgs;
        args.id().ifPresentOrElse(this::displayVocabularyEntryById, () -> displayVocabularyEntries(args));
    }

    @Override
    public Command getCommand() {
        return Command.LIST_VOCABULARY_ENTRIES;
    }

    private void displayVocabularyEntryById(Long id) {
        vocabularyEntryService.findById(id).ifPresent(entry -> {
            adapter.writeLine(colorProcessor.coloredEntry(entry));
            vocabularyEntryService.updateLastSeenAtById(entry.getId());
        });
    }

    private void displayVocabularyEntries(ListVocabularyEntriesCommandArgs args) {
        args.pagination().ifPresentOrElse(pagination -> {
            args.substring().ifPresentOrElse(substring -> {
                var p = new Paginator(pagination, vocabularyEntryService.countWithSubstring(substring));
                display(p, () -> vocabularyEntryService.findWithSubstringLimit(substring, p.limit()));
            }, () -> {
                var p = new Paginator(pagination, vocabularyEntryService.countTotal());
                display(p, () -> vocabularyEntryService.findAllLimit(p.limit()));
            });
        }, () -> {
            args.substring().ifPresentOrElse(
                    substring -> displayAllAtOnce(vocabularyEntryService.findAllWithSubstring(substring)),
                    () -> displayAllAtOnce(vocabularyEntryService.findAll()));
        });
    }

    private void displayAllAtOnce(List<VocabularyEntry> entries) {
        entries.stream().map(colorProcessor::coloredEntry).forEach(adapter::writeLine);
        vocabularyEntryService.updateLastSeenAtByIds(entries.stream().map(VocabularyEntry::getId).collect(toList()));
    }

    private static class Paginator {
        int pagination, count, endInclusive;
        int startInclusive = 1;

        Paginator(int pagination, int count) {
            this.pagination = pagination;
            this.count = count;
            this.endInclusive = Math.min(this.startInclusive + this.pagination - 1, this.count);
        }

        boolean isInRange() {
            return startInclusive <= count;
        }

        void updateRange() {
            startInclusive = endInclusive + 1;
            endInclusive = Math.min(endInclusive + pagination, count);
        }

        int limit() {
            int limit = endInclusive - startInclusive + 1;
            return Math.max(limit, 0);
        }
    }

    private void display(Paginator p, Supplier<List<VocabularyEntry>> supplier) {
        displayAndUpdateLastSeenAt(p, supplier);

        String line = readLineIfInRangeOrEnd(p);
        while (!line.equals("e") && p.isInRange()) {
            displayAndUpdateLastSeenAt(p, supplier);
            line = readLineIfInRangeOrEnd(p);
        }
    }

    private String readLineIfInRangeOrEnd(Paginator p) {
        return p.isInRange() ? adapter.readLine() : "e";
    }

    private void displayAndUpdateLastSeenAt(Paginator p, Supplier<List<VocabularyEntry>> supplier) {
        displayCounter(p);
        displayAllAtOnce(supplier.get());
        p.updateRange();
    }

    private void displayCounter(Paginator p) {
        adapter.writeLine(p.endInclusive + "/" + p.count);
    }

}