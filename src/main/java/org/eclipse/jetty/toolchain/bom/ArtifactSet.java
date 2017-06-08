package org.eclipse.jetty.toolchain.bom;

import java.util.HashSet;
import java.util.Set;

public class ArtifactSet
{
    @SuppressWarnings("unused")
    private Set<String> includes;
    
    @SuppressWarnings("unused")
    private Set<String> excludes;
    
    public void addInclude(String pattern)
    {
        if (includes == null)
        {
            includes = new HashSet<>();
        }
        includes.add(pattern);
    }
    
    public Set<String> getIncludes()
    {
        return includes;
    }
    
    public Set<String> getExcludes()
    {
        return excludes;
    }
}
