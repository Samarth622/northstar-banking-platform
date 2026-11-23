package com.northstar.bank.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.northstar.bank");

    @Test
    void applicationShouldNotDependOnTestFrameworks() {
        noClasses()
                .that().resideInAPackage("com.northstar.bank..")
                .and().haveSimpleNameNotEndingWith("Test")
                .and().haveSimpleNameNotEndingWith("Tests")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.junit..", "com.tngtech.archunit..")
                .check(classes);
    }
}
