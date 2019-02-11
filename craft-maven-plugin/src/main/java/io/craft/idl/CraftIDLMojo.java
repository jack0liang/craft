package io.craft.idl;

import io.craft.idl.generator.Generator;
import io.craft.idl.generator.java.JavaGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.net.URL;
import java.net.URLClassLoader;

@Mojo(name = "gen", defaultPhase = LifecyclePhase.COMPILE)
@Execute(phase = LifecyclePhase.COMPILE)
public class CraftIDLMojo extends AbstractMojo {

    @Parameter
    private String serviceClass;

    @Component
    MavenProject project;

    public void execute() throws MojoExecutionException {
        try {
            URL classes = new URL("file://" + project.getBuild().getOutputDirectory() + "/");
            ClassLoader custom = new URLClassLoader( new URL[] { classes }, getClass().getClassLoader());
            Class clazz = custom.loadClass(serviceClass);
            Generator generator = new JavaGenerator(clazz, project.getBuild().getSourceDirectory());
            generator.process();
        } catch (Exception e) {
            throw new MojoExecutionException("发生异常：" + e.getMessage(), e);
        }
    }
}
