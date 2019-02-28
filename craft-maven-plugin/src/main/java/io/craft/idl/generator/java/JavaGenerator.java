package io.craft.idl.generator.java;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.craft.idl.generator.Generator;
import io.craft.idl.meta.MetaClass;
import io.craft.idl.util.FormatDirective;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
        configuration.setNumberFormat("#");
    }


    @Override
    protected void processService(MetaClass metaClass) throws Exception {
        Template template = configuration.getTemplate("interface.ftl");
        process(template, metaClass);
    }

    @Override
    protected void processStruct(MetaClass metaClass) throws Exception {
        Template template = configuration.getTemplate("class.ftl");
        process(template, metaClass);
    }

    @Override
    protected void processEnum(MetaClass metaClass) throws Exception {
        Template template = configuration.getTemplate("enum.ftl");
        process(template, metaClass);
    }

    private void process(Template template, MetaClass metaClass) throws IOException, TemplateException {
        String path = baseDir + "/" + metaClass.getPackageName().replace(".", "/");
        String filename = metaClass.getClassName() + ".java";
        File dir = new File(path);
        //递归创建文件夹
        dir.mkdirs();
        File file = new File(path + "/" + filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        template.process(metaClass, fw);
        fw.close();
    }
}