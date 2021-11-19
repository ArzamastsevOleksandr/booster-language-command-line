package archunit;

import com.booster.dao.NoteDao;
import com.booster.service.NoteService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class DaoLayerTests {

    @Test
    void daoClassesShouldHaveAllDependenciesFinal() {
        JavaClasses javaClasses = new ClassFileImporter().importPackages("com.booster.dao", "com.booster.service");

        ArchRule rule = ArchRuleDefinition.classes()
                .that()
                .resideInAPackage("com.booster.dao")
                .should()
                .haveOnlyFinalFields();

        rule.check(javaClasses);
    }

    // todo: same for lang, ve, tag etc
    @Test
    void noteDaoIsOnlyAccessedInNoteService() {
        JavaClasses javaClasses = new ClassFileImporter().importPackages("com.booster.dao", "com.booster.service");

        ArchRule rule = ArchRuleDefinition.classes()
                .that()
                .belongToAnyOf(NoteDao.class)
                .should()
                .onlyBeAccessed()
                .byClassesThat()
                .belongToAnyOf(NoteService.class, NoteDao.class);

        rule.check(javaClasses);
    }

}
