package org.mule.tools.devkit.sonar.checks.java;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.mule.tools.devkit.sonar.utils.ClassParserUtils;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;

import com.google.common.collect.Iterables;

@Rule(key = FunctionalTestForMetadataCategoryCheck.KEY, name = "One test per metadata category", description = "Checks that there is one test case per class annotated as @MetaDataCategory and that its name ends with the suffix 'TestCases' and that it inherits from DevKit's AbstractMetaDataTestCase.", priority = Priority.CRITICAL, tags = { "connector-certification"
})
@ActivatedByDefault
public class FunctionalTestForMetadataCategoryCheck extends BaseLoggingVisitor {

    public static final String KEY = "functional-test-per-metadata-category";
    public static final String TEST_DIR = "src/test/java";
    public static final Pattern TEST_PARENT_DIR_PATTERN = Pattern.compile("^((src/test/java/org/mule/module[s]?)+(/\\w+/)+(automation/functional)+$)");

    @Override
    public void scanFile(JavaFileScannerContext context) {
        super.scanFile(context);
    }

    @Override
    public final void visitClass(ClassTree tree) {
        for (AnnotationTree annotationTree : Iterables.filter(tree.modifiers().annotations(), ClassParserUtils.ANNOTATION_TREE_PREDICATE)) {
            if (ClassParserUtils.is(annotationTree, "org.mule.api.annotations.components.MetaDataCategory")) {
                String categoryName = extractCategoryName(tree);
                String testClassName = categoryName + "MetaDataTestCases";
                File dir = new File(TEST_DIR);
                List<File> testFiles = (List<File>) FileUtils.listFiles(dir, new WildcardFileFilter(testClassName), TrueFileFilter.INSTANCE);

                if (testFiles.size() != 1) {
                    logAndRaiseIssue(tree,
                            String.format("There should be one functional test per metadata category. Add test '%s' for category '%s'.", testClassName, categoryName));
                } else {
                    Matcher m = TEST_PARENT_DIR_PATTERN.matcher(testFiles.get(0).getParent());
                    if (!m.matches()) {
                        logAndRaiseIssue(tree, String.format("'%s' must be placed under directory 'src/test/java/.../automation/functional'.", testClassName));
                    }
                }
            }
        }
        super.visitClass(tree);
    }

    private String extractCategoryName(ClassTree tree) {
        String categoryName = tree.simpleName().name();
        return categoryName.replace("MetaData", "").replace("Metadata", "");
    }

}
