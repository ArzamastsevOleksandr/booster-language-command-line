package archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

// todo: move to a different module
public class TestExample {

    @Test
    void test() {
        JavaClasses javaClasses = new ClassFileImporter().importPackages("com.booster");

        // deliberately fail
        ArchRule rule = ArchRuleDefinition.classes()
                .that()
                .resideInAPackage("..handler..")
                .should().onlyBeAccessed()
                .byAnyPackage("..input..");

        rule.check(javaClasses);
    }

}
