package io.craft.idl;

import io.craft.core.annotation.CraftApplication;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;

@Mojo(name = "package", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CraftApplicationMojo extends AbstractMojo {

    @Parameter
    String serviceClass;

    @Parameter
    String outputDirectory;

    @Component
    MavenProject project;

    @Component
    MavenSession session;

    public Class scan(ClassLoader loader, String root, File file) {
        Class clazz = null;
        if (file.isDirectory()) {
            for(File f : file.listFiles()) {
                clazz = scan(loader, root, f);
                if (clazz != null) {
                    return clazz;
                }
            }
        } else {
            String className = file.getAbsolutePath()
                    .replaceFirst(root, "")
                    .replace(File.separatorChar, '.')
                    .replaceAll("^\\.", "");
            if (!className.endsWith(".class")) {
                //如果不是class文件,不需要加载了
                return null;
            }
            className = className.replaceAll("\\.class$", "");
            try {
                Class cls = loader.loadClass(className);
                Annotation mojo = cls.getAnnotation(CraftApplication.class);
                if (mojo != null) {
                    clazz = cls;
                }
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
        }

        return clazz;
    }

    public void execute() throws MojoExecutionException {
        try {
            File file = new File(project.getBuild().getOutputDirectory());
            if (!file.exists()) {
                throw new MojoExecutionException("project outputDirectory not exists");
            }
            URL classes = new URL("file://" + project.getBuild().getOutputDirectory() + "/");
            ClassLoader loader = new URLClassLoader( new URL[] { classes }, getClass().getClassLoader());
            Class clazz = scan(loader, project.getBuild().getOutputDirectory(), file);
            if (clazz == null) {
                throw new MojoExecutionException("(@CraftApplication) craft application not found");
            }
            session.getUserProperties().setProperty("archive.manifest.mainClass", clazz.getName());
            CraftApplication application = (CraftApplication) clazz.getAnnotation(CraftApplication.class);
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("mojo exception:" + e.getMessage(), e);
        }
    }
}
