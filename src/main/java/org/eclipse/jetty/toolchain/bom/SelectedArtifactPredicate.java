package org.eclipse.jetty.toolchain.bom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.maven.artifact.Artifact;

public class SelectedArtifactPredicate implements Predicate<Artifact>
{
    private Set<ArtifactPredicate> includes;
    private Set<ArtifactPredicate> excludes;
    
    public SelectedArtifactPredicate(ArtifactSet artifactSet)
    {
        this(artifactSet.getIncludes(), artifactSet.getExcludes());
    }
    
    public SelectedArtifactPredicate(Set<String> includes, Set<String> excludes)
    {
        this.includes = toPredicates(includes);
        this.excludes = toPredicates(excludes);
    }
    
    private static Set<ArtifactPredicate> toPredicates(Collection<String> predicates)
    {
        Set<ArtifactPredicate> result = new HashSet<>();
        
        if (predicates != null)
        {
            for (String pattern : predicates)
            {
                result.add(new ArtifactPredicate(pattern));
            }
        }
        
        return result;
    }
    
    @Override
    public boolean test(Artifact artifact)
    {
        ArtifactRef ref = new ArtifactRef(artifact);
        return (includes.isEmpty() || matches(includes, ref)) && !matches(excludes, ref);
    }
    
    private boolean matches(Set<ArtifactPredicate> patterns, ArtifactRef ref)
    {
        for (ArtifactPredicate pattern : patterns)
        {
            if (pattern.test(ref))
            {
                return true;
            }
        }
        return false;
    }
    
}
