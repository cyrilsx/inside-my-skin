package dev.healthhub.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.healthhub")
class HexagonalBoundaryTest {

    private static final String[] PROVIDER_PACKAGES = {"dev.healthhub.withings..", "dev.healthhub.garmin.."};

    @ArchTest
    static final ArchRule core_domain_has_no_dependencies_outside_the_jdk = noClasses()
            .that().resideInAPackage("dev.healthhub.core..")
            .should().accessClassesThat().resideInAnyPackage(
                    "dev.healthhub.ingestion..", "dev.healthhub.api..", "dev.healthhub.companionapi..",
                    "dev.healthhub.insights..", "dev.healthhub.app..",
                    "dev.healthhub.withings..", "dev.healthhub.garmin..",
                    "org.springframework..", "jakarta..", "tools.jackson..", "org.slf4j..");

    @ArchTest
    static final ArchRule ingestion_does_not_depend_on_providers_or_app = noClasses()
            .that().resideInAPackage("dev.healthhub.ingestion..")
            .should().accessClassesThat().resideInAnyPackage(
                    "dev.healthhub.app..", "dev.healthhub.withings..", "dev.healthhub.garmin..");

    @ArchTest
    static final ArchRule only_adapter_modules_touch_vendor_code = noClasses()
            .that().resideOutsideOfPackages(PROVIDER_PACKAGES)
            .should().accessClassesThat().resideInAnyPackage(PROVIDER_PACKAGES);

    @ArchTest
    static final ArchRule web_and_insight_layers_do_not_depend_on_app_or_providers = noClasses()
            .that().resideInAnyPackage("dev.healthhub.api..", "dev.healthhub.companionapi..", "dev.healthhub.insights..")
            .should().accessClassesThat().resideInAnyPackage(
                    "dev.healthhub.app..", "dev.healthhub.withings..", "dev.healthhub.garmin..");

    @ArchTest
    static final ArchRule no_vendor_named_classes_outside_adapter_modules = noClasses()
            .that().resideOutsideOfPackages(PROVIDER_PACKAGES)
            .should().haveSimpleNameContaining("Withings")
            .orShould().haveSimpleNameContaining("Garmin");
}
