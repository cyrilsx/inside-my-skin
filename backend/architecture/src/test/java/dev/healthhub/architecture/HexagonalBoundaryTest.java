package dev.healthhub.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class HexagonalBoundaryTest {

    private static final JavaClasses CLASSES = new ClassFileImporter().importPackages("dev.healthhub");

    private static final String[] PROVIDER_PACKAGES = {"dev.healthhub.withings..", "dev.healthhub.garmin.."};

    @Test
    void core_domain_has_no_dependencies_outside_the_jdk() {
        assertDoesNotThrow(() -> noClasses()
                .that().resideInAPackage("dev.healthhub.core..")
                .should().accessClassesThat().resideInAnyPackage(
                        "dev.healthhub.ingestion..", "dev.healthhub.api..", "dev.healthhub.companionapi..",
                        "dev.healthhub.insights..", "dev.healthhub.app..",
                        "dev.healthhub.withings..", "dev.healthhub.garmin..",
                        "org.springframework..", "jakarta..", "tools.jackson..", "org.slf4j..")
                .check(CLASSES));
    }

    @Test
    void ingestion_does_not_depend_on_providers_or_app() {
        assertDoesNotThrow(() -> noClasses()
                .that().resideInAPackage("dev.healthhub.ingestion..")
                .should().accessClassesThat().resideInAnyPackage(
                        "dev.healthhub.app..", "dev.healthhub.withings..", "dev.healthhub.garmin..")
                .check(CLASSES));
    }

    @Test
    void only_adapter_modules_touch_vendor_code() {
        assertDoesNotThrow(() -> noClasses()
                .that().resideOutsideOfPackages(PROVIDER_PACKAGES)
                .should().accessClassesThat().resideInAnyPackage(PROVIDER_PACKAGES)
                .check(CLASSES));
    }

    @Test
    void web_and_insight_layers_do_not_depend_on_app_or_providers() {
        assertDoesNotThrow(() -> noClasses()
                .that().resideInAnyPackage("dev.healthhub.api..", "dev.healthhub.companionapi..", "dev.healthhub.insights..")
                .should().accessClassesThat().resideInAnyPackage(
                        "dev.healthhub.app..", "dev.healthhub.withings..", "dev.healthhub.garmin..")
                .check(CLASSES));
    }

    @Test
    void no_vendor_named_classes_outside_adapter_modules() {
        assertDoesNotThrow(() -> noClasses()
                .that().resideOutsideOfPackages(PROVIDER_PACKAGES)
                .should().haveSimpleNameContaining("Withings")
                .orShould().haveSimpleNameContaining("Garmin")
                .check(CLASSES));
    }
}
