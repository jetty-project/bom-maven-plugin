package org.eclipse.jetty.toolchain.bom;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.codehaus.plexus.util.WriterFactory;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public abstract class AbstractGenerateBomMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    
    /**
     * Artifacts to include/exclude from the bom.
     * <p>
     * Artifact pattern syntax is {@code groupId:artifactId:type:classifier}.
     * </p>
     * <p>
     * Partial patterns will result in remaining segments being declated as wildcard '{@code *}'.
     * Matching behavior is defined by {@link org.codehaus.plexus.util.SelectorUtils#match(String, String)}
     * </p>
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
    protected ArtifactSet artifactSet;
    
    @Parameter(defaultValue = "${project.build.directory}/bom-pom.xml")
    protected File pomLocation;
    
    protected void generateBom(List<Artifact> allArtifacts) throws MojoExecutionException
    {
        Log log = getLog();
        log.info(String.format("Generating BOM: %s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion()));
        
        if (artifactSet == null)
        {
            artifactSet = new ArtifactSet();
            artifactSet.addInclude(project.getGroupId() + "*");
        }
        SelectedArtifactPredicate selectedArtifactPredicate = new SelectedArtifactPredicate(artifactSet);
        
        List<Artifact> includedArtifacts = allArtifacts.stream()
                .filter(selectedArtifactPredicate)
                .collect(Collectors.toList());
        
        
        if (log.isDebugEnabled())
        {
            log.info(String.format("%d artifacts selected for bom", includedArtifacts.size()));
            
            for (Artifact artifact : includedArtifacts)
            {
                StringBuilder buf = new StringBuilder();
                buf.append("Including: ").append(artifact.getGroupId());
                buf.append(':').append(artifact.getArtifactId());
                buf.append(':').append(artifact.getVersion());
                buf.append(':').append(artifact.getType());
                
                if (StringUtils.isNotBlank(artifact.getClassifier()))
                {
                    buf.append(':').append(artifact.getClassifier());
                }
                
                log.debug(buf.toString());
            }
        }
        
        // Generate a new pom
        
        Model model = new Model();
        model.setModelVersion(project.getModelVersion());
        model.setGroupId(project.getGroupId());
        model.setArtifactId(project.getArtifactId());
        model.setVersion(project.getVersion());
        model.setPackaging("pom");
        
        DependencyManagement dependencyManagement = new DependencyManagement();
        
        for (Artifact artifact : includedArtifacts)
        {
            Dependency dependency = new Dependency();
            dependency.setGroupId(artifact.getGroupId());
            dependency.setArtifactId(artifact.getArtifactId());
            dependency.setVersion(artifact.getVersion());
            
            if (StringUtils.isNotBlank(artifact.getType()))
            {
                dependency.setType(artifact.getType());
            }
            
            if (StringUtils.isNotBlank(artifact.getClassifier()))
            {
                dependency.setClassifier(artifact.getClassifier());
            }
            dependencyManagement.addDependency(dependency);
        }
        
        model.setDependencyManagement(dependencyManagement);
        
        // Write pom to disk
        
        try
        {
            Artifact bomPom = writePom(model, pomLocation);
            project.addAttachedArtifact(bomPom);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Unable to write bom pom: " + pomLocation, e);
        }
    }
    
    private Artifact writePom(Model newModel, File pomLocation) throws IOException
    {
        File parentDir = pomLocation.getParentFile();
        if (!parentDir.exists())
        {
            if (!parentDir.mkdirs())
            {
                throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
            }
        }
        
        Writer w = WriterFactory.newXmlWriter(pomLocation);
        try
        {
            Element root = new Element("project");
            
            String modelVersion = newModel.getModelVersion();
            
            Namespace pomNamespace = Namespace.getNamespace("", "http://maven.apache.org/POM/" + modelVersion);
            
            root.setNamespace(pomNamespace);
            
            Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            
            root.addNamespaceDeclaration(xsiNamespace);
            
            if (root.getAttribute("schemaLocation", xsiNamespace) == null)
            {
                root.setAttribute("schemaLocation",
                        "http://maven.apache.org/POM/" + modelVersion + " http://maven.apache.org/maven-v"
                                + modelVersion.replace('.', '_') + ".xsd", xsiNamespace);
            }
            
            DefaultJDOMFactory factory = new DefaultJDOMFactory();
            
            Document doc = new Document(root);
            
            // Build jdom
            root.addContent(factory.element("modelVersion", pomNamespace).setText(newModel.getModelVersion()));
            root.addContent(factory.element("groupId", pomNamespace).setText(newModel.getGroupId()));
            root.addContent(factory.element("artifactId", pomNamespace).setText(newModel.getArtifactId()));
            root.addContent(factory.element("version", pomNamespace).setText(newModel.getVersion()));
            root.addContent(factory.element("packaging", pomNamespace).setText("pom"));
            
            DependencyManagement dependencyManagement = newModel.getDependencyManagement();
            
            if (dependencyManagement != null)
            {
                Element elemDependencyManagement = factory.element("dependencyManagement", pomNamespace);
                root.addContent(elemDependencyManagement);
                
                Element elemDependencies = factory.element("dependencies", pomNamespace);
                elemDependencyManagement.addContent(elemDependencies);
                
                for (Dependency dependency : dependencyManagement.getDependencies())
                {
                    Element elemDependency = factory.element("dependency", pomNamespace);
                    
                    elemDependency.addContent(factory.element("groupId", pomNamespace).setText(dependency.getGroupId()));
                    elemDependency.addContent(factory.element("artifactId", pomNamespace).setText(dependency.getArtifactId()));
                    elemDependency.addContent(factory.element("version", pomNamespace).setText(dependency.getVersion()));
    
                    if (StringUtils.isNotBlank(dependency.getType()) && !"jar".equals(dependency.getType()))
                    {
                        elemDependency.addContent(factory.element("type", pomNamespace).setText(dependency.getType()));
                    }
                    
                    if (StringUtils.isNotBlank(dependency.getClassifier()))
                    {
                        elemDependency.addContent(factory.element("classifier", pomNamespace).setText(dependency.getClassifier()));
                    }
                    
                    elemDependencies.addContent(elemDependency);
                }
            }
            
            // Write file
            String encoding = newModel.getModelEncoding() != null ? newModel.getModelEncoding() : "UTF-8";
            Format format = Format.getPrettyFormat().setEncoding(encoding);
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(format);
            outputter.output(doc, w);
        }
        finally
        {
            w.close();
        }
        
        // Replace existing artifact file location (HACKY?)
        // Shame this doesn't work.
        // project.getArtifact().setFile(pomLocation);
        
        AttachedArtifact attachedArtifact = new AttachedArtifact(project.getArtifact(), "pom", "bom", project.getArtifact().getArtifactHandler());
        attachedArtifact.setFile(pomLocation);
        return attachedArtifact;
    }
}
