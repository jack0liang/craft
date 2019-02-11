package io.craft.idl.generator.java;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.craft.idl.generator.Generator;
import io.craft.idl.meta.MetaClass;
import io.craft.idl.util.FormatDirective;

import java.io.File;
import java.io.FileWriter;

public class JavaGenerator extends Generator {

    private Configuration configuration;

    private String baseDir;

    public JavaGenerator(Class clazz, String baseDir) throws Exception {
        super(clazz);
        this.baseDir = baseDir;
        configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassForTemplateLoading(getClass(), "/java");
        configuration.setSharedVariable("format", new FormatDirective());
    }


    @Override
    protected void processService(MetaClass metaClass) throws Exception {
        Template template = configuration.getTemplate("interface.ftl");
        String path = metaClass.getPackageName().replace(".", "/") + "/" + metaClass.getClassName() + ".java";
        File file = new File(baseDir + "/" + path);
        FileWriter fw = new FileWriter(file);
        template.process(metaClass, fw);
    }

    @Override
    protected void processStruct(MetaClass metaClass) throws Exception {
        Template template = configuration.getTemplate("class.ftl");
        String path = metaClass.getPackageName().replace(".", "/") + "/" + metaClass.getClassName() + ".java";
        File file = new File(baseDir + "/" + path);
        FileWriter fw = new FileWriter(file);
        template.process(metaClass, fw);
    }

    @Override
    protected void processEnum(MetaClass metaClass) throws Exception {
        Template template = configuration.getTemplate("enum.ftl");
        String path = metaClass.getPackageName().replace(".", "/") + "/" + metaClass.getClassName() + ".java";
        File file = new File(baseDir + "/" + path);
        FileWriter fw = new FileWriter(file);
        template.process(metaClass, fw);
    }
}