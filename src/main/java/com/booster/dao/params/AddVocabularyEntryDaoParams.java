package com.booster.dao.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddVocabularyEntryDaoParams {

    long wordId;
    long languageId;
    String definition;

    @Builder.Default
    Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @Builder.Default
    int correctAnswersCount = 0;

    @Builder.Default
    Set<Long> synonymIds = Set.of();
    @Builder.Default
    Set<Long> antonymIds = Set.of();

}
