package org.mule.tools.devkit.sonar;

import com.google.common.collect.ImmutableList;
import org.mule.tools.devkit.sonar.checks.java.LicenseByCategoryCheck;
import org.mule.tools.devkit.sonar.checks.java.NumberOfArgumentsInProcessorCheck;
import org.mule.tools.devkit.sonar.checks.java.RedundantExceptionNameCheck;
import org.mule.tools.devkit.sonar.checks.java.RefOnlyInComplexTypesCheck;
import org.mule.tools.devkit.sonar.checks.java.RestCallDeprecatedCheck;
import org.mule.tools.devkit.sonar.checks.pom.*;

import java.util.Collection;

public class ConnectorsChecks {

    private ConnectorsChecks() {
    }

    public static Collection<Class> javaChecks() {
        final ImmutableList.Builder<Class> builder = ImmutableList.builder();
        builder.add(NumberOfArgumentsInProcessorCheck.class);
        builder.add(RefOnlyInComplexTypesCheck.class);
        builder.add(LicenseByCategoryCheck.class);
        builder.add(RestCallDeprecatedCheck.class);
        builder.add(RedundantExceptionNameCheck.class);
        return builder.build();
    }

    public static Collection<Class> pomChecks() {
        final ImmutableList.Builder<Class> builder = ImmutableList.builder();
        builder.add(ScopeProvidedInMuleDependenciesCheck.class);
        builder.add(TestingFrameworkNotOverwrittenCheck.class);
        return builder.build();
    }

    public static Collection<Class> allChecks() {
        return ImmutableList.<Class> builder().addAll(javaChecks()).addAll(pomChecks()).build();
    }

}
