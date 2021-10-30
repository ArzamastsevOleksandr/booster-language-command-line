package com.booster.service;

import com.booster.dao.WordDao;
import com.booster.model.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordDao wordDao;

    public Word findByNameOrCreateAndGet(String name) {
        return findByName(name)
                .orElseGet(() -> wordDao.createWordWithName(name));
    }

    public Optional<Word> findByName(String name) {
        if (wordDao.countWithName(name) == 1) {
            return Optional.of(wordDao.findByName(name));
        }
        return Optional.empty();
    }

}
