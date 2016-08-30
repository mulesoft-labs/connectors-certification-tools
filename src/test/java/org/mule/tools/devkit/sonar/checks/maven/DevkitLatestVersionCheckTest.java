package org.mule.tools.devkit.sonar.checks.maven;


import com.google.common.collect.Iterables;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.mule.tools.devkit.sonar.checks.ConnectorIssue;
import org.mule.tools.devkit.sonar.utils.PomUtils;

import javax.xml.stream.XMLStreamException;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class DevkitLatestVersionCheckTest {

    private MavenProject mavenProject;

    @Test
    public void checkDevkitVersionIsLatest() throws IOException, XmlPullParserException, XMLStreamException {
        Iterable<ConnectorIssue> pomIssues = analyze("src/test/files/maven/devkit-latest-version/devkit-version-is-latest");
        assertThat(pomIssues, emptyIterable());
    }

    @Test
    public void checkDevkitVersionIsNotLatest() throws IOException, XmlPullParserException, XMLStreamException {
        devkitVersionIsRevisionOrNotLatest("src/test/files/maven/devkit-latest-version/devkit-version-is-not-latest");
    }

    @Test
    public void checkDevkitVersionIsRevision() throws IOException, XmlPullParserException, XMLStreamException {
        devkitVersionIsRevisionOrNotLatest("src/test/files/maven/devkit-latest-version/devkit-version-is-revision");
    }

    //Both have the same code but read different POMs
    private void devkitVersionIsRevisionOrNotLatest(String filePath){
        Iterable<ConnectorIssue> pomIssues = analyze(filePath);
        String currentDevkitVersion = mavenProject.getModel().getParent().getVersion();
        assertThat(Iterables.size(pomIssues), is(1));
        ConnectorIssue connectorIssue = Iterables.getOnlyElement(pomIssues);
        assertThat(connectorIssue.ruleKey(), is("devkit-latest-version"));
        assertThat(connectorIssue.message(),
                is(String.format("Current connector Devkit version '%s' is not the latest stable version. If feasible, use version '%s'.",
                        currentDevkitVersion, PomUtils.getLatestDevkitVersion())));
    }

    private Iterable<ConnectorIssue> analyze(String fileName){
        mavenProject = PomUtils.createMavenProjectFromPomFile(new File(fileName));
        final DevkitLatestVersionCheck check = new DevkitLatestVersionCheck();
        return check.analyze(mavenProject);
    }
}
