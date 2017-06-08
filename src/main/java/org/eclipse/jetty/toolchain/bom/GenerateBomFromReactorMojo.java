package org.eclipse.jetty.toolchain.bom;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generate a bom pom from the projects present in the reactor.
 */
@SuppressWarnings("unused")
@Mojo(name = "generate-from-reactor", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresProject = true)
public class GenerateBomFromReactorMojo extends AbstractGenerateBomMojo
{
    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        List<Artifact> allArtifacts = new ArrayList<>();
        reactorProjects.stream().forEach((reactorProject) ->
        {
            allArtifacts.add(reactorProject.getArtifact());
            allArtifacts.addAll(reactorProject.getArtifacts());
        });
        
        Log log = getLog();
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Found %d projects (%d overall artifacts)", reactorProjects.size(), allArtifacts.size()));
        }
        
        generateBom(allArtifacts);
    }
}
