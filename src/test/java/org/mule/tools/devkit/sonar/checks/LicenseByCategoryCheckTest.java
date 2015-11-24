package org.mule.tools.devkit.sonar.checks;

import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.CheckMessagesVerifierRule;

import java.io.File;

public class LicenseByCategoryCheckTest {

    @Rule
    public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

    private void runForCategory(String category, int lineNumber, String expectedMessage) {
        runForCategory(category, category, lineNumber, expectedMessage);
    }

    private void runForCategory(String category, String testClass, int lineNumber, String expectedMessage) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.getProperties().setProperty("category", category);
        LicenseByCategoryCheck check = new LicenseByCategoryCheck(mavenProject);

        SourceFile file = JavaAstScanner
                .scanSingleFile(new File(String.format("src/test/files/licensechecks/LicenseByCategory%sCheck.java", testClass)), new VisitorsBridge(check));

        checkMessagesVerifier.verify(file.getCheckMessages())
                .next().atLine(lineNumber).withMessage(expectedMessage);
    }

    @Test
    public void checkCommunity() {
        runForCategory("Community", 11, "@RequiresEnterpriseLicense and @RequiresEntitlement must not be present for Community category.");
    }

    @Test
    public void checkCertified() {
        runForCategory("Certified", 9, "@RequiresEnterpriseLicense must be defined and @RequiresEntitlement must not be present for Select and Certified category.");
    }

    @Test
    public void checkSelect() {
        runForCategory("Select", 9, "@RequiresEnterpriseLicense must be defined and @RequiresEntitlement must not be present for Select and Certified category.");
    }

    @Test
    public void checkPremiumNoRequiresLicense() {
        runForCategory("Premium", "PremiumNoRequiresLicense", 11, "@RequiresEnterpriseLicense and @RequiresEntitlement need to be defined for Premium category.");
    }

    @Test
    public void checkPremiumNoNameAttribute() {
        runForCategory("Premium", "PremiumNoNameAttribute", 13, "'name' attribute must be defined for @RequiresEntitlement using connector name.");
    }

    @Test
    public void checkInvalid() {
        runForCategory("Invalid", 9, "Invalid category specified in pom.xml");
    }

}