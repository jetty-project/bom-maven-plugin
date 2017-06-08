package org.eclipse.jetty.toolchain.bom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generate a bom pom from the projects present in the reactor.
 */
@SuppressWarnings("unused")
@Mojo(name = "generate", threadSafe = true, requiresProject = true)
public class GenerateBomMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;
    
    /**
     * Artifacts to include/exclude from the bom.
     * <p>
     * Artifact pattern syntax is {@code groupId:artifactId:type:classifier}.
     * </p>
     * <p>
     * Partial patterns will result in remaining segments being declated as wildcard '{@code *}'.
     * Matching behavior is defined by {@link org.codehaus.plexus.util.SelectorUtils#match(String, String)}
     * </p>
     * <p>
     * <pre>
     * &lt;artifactSet&gt;
     *   &lt;includes&gt;
     *     &lt;include&gt;org.eclipse.jetty:*&lt;/include&gt;
     *   &lt;/includes&gt;
     *   &lt;excludes&gt;
     *     &lt;exclude&gt;*:test-*&lt;/exclude&gt;
     *   &lt;/excludes&gt;
     * &lt;/artifactSet&gt;
     * </pre>
     */
    @Parameter
    private ArtifactSet artifactSet;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        Log log = getLog();
        log.info(String.format("Generating BOM: %s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion()));
        List<MavenProject> reactorProjects = session.getSortedProjects();
        
        List<Artifact> allArtifacts = new ArrayList<>();
        reactorProjects.stream().forEach((reactorProject) -> allArtifacts.addAll(reactorProject.getArtifacts()));
        
        log.info(String.format("Found %d overall artifacts", allArtifacts.size()));
        
        if (artifactSet == null)
        {
            artifactSet = new ArtifactSet();
            artifactSet.addInclude(project.getGroupId() + "*");
        }
        SelectedArtifactPredicate selectedArtifactPredicate = new SelectedArtifactPredicate(artifactSet);
        
        List<Artifact> includedArtifacts = allArtifacts.stream()
                .filter(selectedArtifactPredicate)
                .collect(Collectors.toList());
        
        log.info(String.format("Found %d reactor projects (%d artifacts selected)", reactorProjects.size(), includedArtifacts.size()));
        
        includedArtifacts.stream()
                .forEach((artifact) -> log.info(String.format("%s:%s:%s:%s:%s",
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getType(),
                        artifact.getVersion(),
                        artifact.getClassifier())));
        
        // TODO: generate new pom
        
        // TODO: deploy generated pom
        // TODO: swap generated pom for actual pom in deployment (shade plugin does this?)
    }
}
