package org.eclipse.jetty.toolchain.bom;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;

public class ArtifactRef
{
    protected final String groupId;
    protected final String artifactId;
    protected final String type;
    protected final String classifier;
    
    public ArtifactRef(Artifact artifact)
    {
        this(artifact.getGroupId(), artifact.getArtifactId(), artifact.getType(), artifact.getClassifier());
    }
    
    public ArtifactRef(String groupId, String artifactId, String type, String classifier)
    {
        this.groupId = Objects.toString(groupId, "");
        this.artifactId = Objects.toString(artifactId, "");
        this.type = Objects.toString(type, "");
        this.classifier = Objects.toString(classifier, "");
    }
    
    public ArtifactRef(String id)
    {
        String defGroupId = "";
        String defArtifactId = "*";
        String defType = "*";
        String defClassifier = "*";
        
        if (StringUtils.isNotBlank(id))
        {
            String[] tokens = id.split(":", -1);
            if (tokens.length > 0) defGroupId = tokens[0];
            if (tokens.length > 1) defArtifactId = tokens[1];
            if (tokens.length > 2) defType = tokens[2];
            if (tokens.length > 3) defClassifier = tokens[3];
        }
        
        groupId = defGroupId;
        artifactId = defArtifactId;
        type = defType;
        classifier = defClassifier;
    }
    
    public String getArtifactId()
    {
        return artifactId;
    }
    
    public String getClassifier()
    {
        return classifier;
    }
    
    public String getGroupId()
    {
        return groupId;
    }
    
    public String getType()
    {
        return type;
    }
}
