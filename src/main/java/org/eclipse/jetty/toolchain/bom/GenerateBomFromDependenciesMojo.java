package org.eclipse.jetty.toolchain.bom;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Generate a bom pom from the projects present in the reactor.
 */
@SuppressWarnings("unused")
@Mojo(name = "generate-from-dependencies", defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true, requiresProject = true)
public class GenerateBomFromDependenciesMojo extends AbstractGenerateBomMojo
{
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        List<Artifact> allArtifacts = new ArrayList<>();
        allArtifacts.addAll(project.getDependencyArtifacts());
        
        Log log = getLog();
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Found %d dependency artifacts", allArtifacts.size()));
        }
        
        generateBom(allArtifacts);
    }
}
