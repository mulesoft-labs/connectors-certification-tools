package org.mule.tools.devkit.sonar.checks;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.mule.api.annotations.licensing.RequiresEnterpriseLicense;
import org.mule.api.annotations.licensing.RequiresEntitlement;
import org.mule.tools.devkit.sonar.JavaRuleRepository;
import org.mule.tools.devkit.sonar.utils.ClassParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.squidbridge.annotations.ActivatedByDefault;

import javax.annotation.Nullable;
import java.util.List;

@Rule(key = LicenseByCategoryCheck.KEY,
        name = "Check licensing annotations match the category declared in pom.xml",
        description = "This rule checks the correct usage of @RequiresEnterpriseLicense and @RequiresEntitlement according to category defined in pom.xml",
        tags = { "connector-certification" })
@ActivatedByDefault
public class LicenseByCategoryCheck extends AbstractConnectorClassCheck {

    public static final String KEY = "license-by-category";
    private static final RuleKey RULE_KEY = RuleKey.of(JavaRuleRepository.REPOSITORY_KEY, KEY);

    private static final Logger logger = LoggerFactory.getLogger(LicenseByCategoryCheck.class);
    public static final Predicate<AnnotationTree> HAS_REQUIRES_ENTERPRISE_LICENSE_ANNOTATION = new Predicate<AnnotationTree>() {

        @Override
        public boolean apply(@Nullable AnnotationTree input) {
            return input != null && ClassParserUtils.is(input, RequiresEnterpriseLicense.class);
        }
    };

    public static final Predicate<AnnotationTree> HAS_REQUIRES_ENTITLEMENT_ANNOTATION = new Predicate<AnnotationTree>() {

        @Override
        public boolean apply(@Nullable AnnotationTree input) {
            return input != null && ClassParserUtils.is(input, RequiresEntitlement.class);
        }
    };

    private final MavenProject mavenProject;

    public LicenseByCategoryCheck(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    @Override
    protected RuleKey getRuleKey() {
        return RULE_KEY;
    }

    @Override
    protected void verifyConnector(@NotNull ClassTree classTree, @NotNull IdentifierTree connectorAnnotation) {
        final List<? extends AnnotationTree> annotations = classTree.modifiers().annotations();

        boolean hasEnterpriseAnnotation = Iterables.any(annotations, HAS_REQUIRES_ENTERPRISE_LICENSE_ANNOTATION);
        boolean hasEntitlementAnnotation = Iterables.any(annotations, HAS_REQUIRES_ENTITLEMENT_ANNOTATION);

        String category = mavenProject.getProperties().getProperty("category");
        logger.debug("Parsed Category version -> {}", category);

        switch (category.toUpperCase()) {
            case "PREMIUM":
                if (!hasEnterpriseAnnotation || !hasEntitlementAnnotation) {
                    logAndRaiseIssue(classTree, "@RequiresEnterpriseLicense and @RequiresEntitlement need to be defined for Premium category.");
                }

                if (hasEntitlementAnnotation) {
                    final AnnotationTree annotation = Iterables.find(annotations, HAS_REQUIRES_ENTITLEMENT_ANNOTATION);
                    final List<? extends ExpressionTree> arguments = annotation.arguments();
                    final ExpressionTree find = Iterables.find(arguments, new Predicate<ExpressionTree>() {

                        @Override
                        public boolean apply(@Nullable ExpressionTree input) {
                            return input != null && input.is(Tree.Kind.ASSIGNMENT) && "name".equals(((AssignmentExpressionTree) input).variable().toString());
                        }
                    }, null);
                    if (find == null) {
                        logAndRaiseIssue(classTree, "'name' attribute must be defined for @RequiresEntitlement using connector name.");
                    }
                }
                break;

            case "STANDARD":
            case "SELECT":
            case "CERTIFIED":
                if (!hasEnterpriseAnnotation || hasEntitlementAnnotation) {
                    logAndRaiseIssue(classTree, "@RequiresEnterpriseLicense must be defined and @RequiresEntitlement must not be present for Select and Certified category.");
                }
                break;

            case "COMMUNITY":
                if (hasEnterpriseAnnotation || hasEntitlementAnnotation) {
                    logAndRaiseIssue(classTree, "@RequiresEnterpriseLicense and @RequiresEntitlement must not be present for Community category.");
                }
                break;

            default:
                logAndRaiseIssue(classTree, "Invalid category specified in pom.xml");
                break;
        }
    }

}
