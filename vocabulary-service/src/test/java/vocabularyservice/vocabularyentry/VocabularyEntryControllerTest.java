package vocabularyservice.vocabularyentry;

import api.vocabulary.AddVocabularyEntryInput;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import net.minidev.json.JSONArray;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import vocabularyservice.language.TestLanguageService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD)
class VocabularyEntryControllerTest {

    @Autowired
    VocabularyEntryRepository vocabularyEntryRepository;
    @Autowired
    TestVocabularyEntryService testVocabularyEntryService;
    @Autowired
    TestLanguageService testLanguageService;
    @Autowired
    TestWordService testWordService;
    @Autowired
    WebTestClient webTestClient;

    @Test
    void shouldReturnAllVocabularyEntries() {
        // given
        var name = "entry";
        var english = "English";
        Long wordId = testWordService.createWord(name);
        Long languageId = testLanguageService.createLanguage(english);
        // when
        Long entryId = testVocabularyEntryService.createVocabularyEntry(wordId, languageId);
        // then
        webTestClient.get()
                .uri("/vocabulary-entries/")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(entryId)
                .jsonPath("$[0].name").isEqualTo(name)
                .jsonPath("$[0].correctAnswersCount").isEqualTo(0)
                .jsonPath("$[0].definition").isEqualTo(name)
                .jsonPath("$[0].synonyms.length()").isEqualTo(1)
                .jsonPath("$[0].synonyms[0]").isEqualTo(name)
                .jsonPath("$[0].language.id").isEqualTo(languageId)
                .jsonPath("$[0].language.name").isEqualTo(english);
    }

    @Test
    void shouldCreateVocabularyEntry() {
        // given
        assertThat(vocabularyEntryRepository.findAll()).isEmpty();
        var english = "English";
        Long languageId = testLanguageService.createLanguage(english);
        // when
        var name = "wade";
        var definition = "walk with effort";
        var synonyms = Set.of("ford", "paddle");

        var input = AddVocabularyEntryInput.builder()
                .name(name)
                .languageId(languageId)
                .definition(definition)
                .synonyms(synonyms)
                .build();

        webTestClient.post()
                .uri("/vocabulary-entries/")
                .accept(APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo(name)
                .jsonPath("$.definition").isEqualTo(definition)
                .jsonPath("$.correctAnswersCount").isEqualTo(0)
                .jsonPath("$.synonyms.length()").isEqualTo(synonyms.size())
                .jsonPath("$.language.id").isEqualTo(languageId)
                .jsonPath("$.language.name").isEqualTo(english)
                .jsonPath("$.synonyms").value(new BaseMatcher<HashSet<String>>() {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Expected a set containing all of: " + synonyms);
                    }

                    @Override
                    public boolean matches(Object actual) {
                        JSONArray jsonArray = (JSONArray) actual;
                        return new HashSet<>(jsonArray).equals(synonyms);
                    }
                });
    }

    // todo: test add ve fails with no languageId

    @Test
    void shouldFindVocabularyEntryById() {
        // given
        var name = "wade";
        var synonyms = Set.of(name);
        var english = "English";

        Long wordId = testWordService.createWord(name);
        Long languageId = testLanguageService.createLanguage(english);
        Long vocabularyEntryId = testVocabularyEntryService.createVocabularyEntry(wordId, languageId);
        // when + then
        webTestClient.get()
                .uri("/vocabulary-entries/" + vocabularyEntryId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(vocabularyEntryId)
                .jsonPath("$.name").isEqualTo(name)
                .jsonPath("$.definition").isEqualTo(name)
                .jsonPath("$.correctAnswersCount").isEqualTo(0)
                .jsonPath("$.synonyms.length()").isEqualTo(synonyms.size())
                .jsonPath("$.language.id").isEqualTo(languageId)
                .jsonPath("$.language.name").isEqualTo(english)
                .jsonPath("$.synonyms").value(new BaseMatcher<HashSet<String>>() {
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Expected a set containing all of: " + synonyms);
                    }

                    @Override
                    public boolean matches(Object actual) {
                        JSONArray jsonArray = (JSONArray) actual;
                        return new HashSet<>(jsonArray).equals(synonyms);
                    }
                });
    }

}