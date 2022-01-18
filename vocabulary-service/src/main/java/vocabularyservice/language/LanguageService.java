package vocabularyservice.language;

import api.vocabulary.LanguageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Slf4j
@Service
record LanguageService(LanguageRepository languageRepository) {

    @Transactional
    Collection<LanguageDto> getAll() {
        return languageRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private LanguageDto toDto(LanguageEntity languageEntity) {
        return new LanguageDto(languageEntity.getId(), languageEntity.getName());
    }

}