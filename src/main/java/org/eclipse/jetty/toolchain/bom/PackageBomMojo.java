package org.eclipse.jetty.toolchain.bom;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 */
@Mojo(name = "package-bom", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresProject = true)
public class PackageBomMojo
    extends AbstractMojo
{

    @Component
    private MavenProject project;

    @Override
    public void execute()
        throws MojoExecutionException
    {
        List<Artifact> attachedArtifacts = project.getAttachedArtifacts();
        List<Artifact> boms =  attachedArtifacts.stream().filter(
            artifact -> {
                if ("bom".equals( artifact.getClassifier()))
                {
                    return true;
                }
                return false;
            }
        ).collect( Collectors.toList() );

        if (boms.size() != 1)
        {
            throw new MojoExecutionException( "no bom found" );
        }
        Artifact bom = boms.get( 0 );
        // a bit hackhish way to replace the current pom....
        this.project.setFile( bom.getFile() );
    }

}
