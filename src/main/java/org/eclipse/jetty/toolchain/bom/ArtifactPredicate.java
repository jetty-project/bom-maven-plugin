package org.eclipse.jetty.toolchain.bom;

import java.util.function.Predicate;

import org.codehaus.plexus.util.SelectorUtils;

/**
 * Artifact pattern (version-less)
 */
public class ArtifactPredicate extends ArtifactRef implements Predicate<ArtifactRef>
{
    public ArtifactPredicate(String id)
    {
        super(id);
    }
    
    @Override
    public boolean test(ArtifactRef artifactRef)
    {
        if (artifactRef == null)
        {
            return false;
        }
        if (!SelectorUtils.match(getGroupId(), artifactRef.getGroupId()))
        {
            return false;
        }
        if (!SelectorUtils.match(getArtifactId(), artifactRef.getArtifactId()))
        {
            return false;
        }
        if (!SelectorUtils.match(getType(), artifactRef.getType()))
        {
            return false;
        }
        if (!SelectorUtils.match(getClassifier(), artifactRef.getClassifier()))
        {
            return false;
        }
        
        return true;
    }
}
