package org.mule.tools.devkit.sonar.checks.java;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.runners.Suite.SuiteClasses;
import org.mule.tools.devkit.sonar.utils.ClassParserUtils;
import org.sonar.check.Rule;
import org.sonar.java.ast.parser.InitializerListTreeImpl;
import org.sonar.java.model.expression.NewArrayTreeImpl;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

@Rule(key = FunctionalTestSuiteCheck.KEY, name = "Functional test coverage", description = "Checks that: 1. There is ONE unit test per @Processor. 2. TestCases class names for processors end with 'TestCases'. 3. All test cases in each package (functional, system and unit) are included in their corresponding *TestSuite classes.", tags = { "connector-certification" })
@ActivatedByDefault
public class FunctionalTestSuiteCheck extends BaseLoggingVisitor {

    public static final String KEY = "functional-test-suite-coverage";
    public static final String SUFFIX = "TestCases";
    public static final String TEST_DIR = "src/test/java";
    public static final Pattern FILE_PATH_PATTERN = Pattern.compile("^((src/test/java/org/mule/modules)+(/\\w+/)+(automation/functional/)+(\\w*.java)$)");

    @Override
    public final void visitClass(ClassTree tree) {
        IdentifierTree treeName = tree.simpleName();
        if (treeName != null && treeName.name().endsWith("TestSuite")) {
            final AnnotationTree runWithAnnotation = Iterables.find(tree.modifiers().annotations(), ClassParserUtils.hasAnnotationPredicate(SuiteClasses.class), null);
            if (runWithAnnotation == null) {
                logAndRaiseIssue(tree, String.format("Missing @SuiteClasses annotation on Test Suite class '%s'.", tree.simpleName().name()));
            } else {
                final List<ExpressionTree> arguments = runWithAnnotation.arguments();
                final NewArrayTreeImpl arrayTree = (NewArrayTreeImpl) Iterables.getOnlyElement(arguments);

                InitializerListTreeImpl suiteClasses = (InitializerListTreeImpl) arrayTree.initializers();

                if (suiteClasses.isEmpty()) {
                    logAndRaiseIssue(tree, "No tests have been declared under @SuiteClasses.");
                } else {

                    final List<File> tests = (List<File>) FileUtils.listFiles(new File(TEST_DIR), new WildcardFileFilter("*TestCases.java"), TrueFileFilter.INSTANCE);

                    for (ExpressionTree test : suiteClasses) {
                        String testName = ((MemberSelectExpressionTree) test).expression().symbolType().name();
                        Iterable<? extends File> matchingTests = Iterables.filter(tests, new FilePredicate(testName));

                        if (testName.endsWith(SUFFIX)) {
                            if (Iterables.isEmpty(matchingTests)) {
                                logAndRaiseIssue(test, String.format("A file named '%s.java' must exist in directory 'src/test/java/../automation/functional'.", testName));
                            }
                        } else {
                            logAndRaiseIssue(test, String.format("Functional test classes must end with 'TestCases'. Rename '%s.java' accordingly.", testName));
                        }
                    }
                }
            }
        }
        super.visitClass(tree);
    }

    class FilePredicate implements Predicate<File> {

        private String filename;

        public FilePredicate(final String filename) {
            this.filename = filename;
        }

        @Override
        public boolean apply(File file) {
            return filename.equals(FilenameUtils.removeExtension(file.getName())) && FILE_PATH_PATTERN.matcher(file.getPath()).find();

        }
    };

}
