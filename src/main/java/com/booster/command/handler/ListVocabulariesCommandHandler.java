package com.booster.command.handler;

import com.booster.dao.VocabularyDao;
import com.booster.model.Vocabulary;
import com.booster.output.CommandLineWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListVocabulariesCommandHandler {

    private final VocabularyDao vocabularyDao;

    private final CommandLineWriter commandLineWriter;

    public void handle() {
        List<Vocabulary> vocabularies = vocabularyDao.findAll();

        if (vocabularies.isEmpty()) {
            commandLineWriter.writeLine("You don't have any vocabularies.");
        } else {
            commandLineWriter.writeLine("All vocabularies:");
            for (var vocabulary : vocabularies) {
                commandLineWriter.writeLine(vocabulary.toString());
            }
        }
    }

}
