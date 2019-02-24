package io.craft.core.spring;

import io.craft.core.registry.EtcdServiceRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.thrift.TProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class CraftServiceDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionParserDelegate delegate = parserContext.getDelegate();
        BeanDefinitionHolder holder = delegate.parseBeanDefinitionElement(element);

        BeanDefinition definition = holder.getBeanDefinition();

        try {
            process(definition);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        //注册etcd service registry
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        BeanDefinitionBuilder registryBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(EtcdServiceRegistry.class);
        //取消lazy init 避免造成无法正常的注入到配置中心
        registryBeanDefinitionBuilder.setLazyInit(false);
        registryBeanDefinitionBuilder.addPropertyValue("root", "${application.service.registry.root}");
        registryBeanDefinitionBuilder.addPropertyValue("applicationName", "${application.name}");
        registryBeanDefinitionBuilder.addPropertyValue("host", "${application.host}");
        registryBeanDefinitionBuilder.addPropertyValue("port", "${application.port}");
        registryBeanDefinitionBuilder.addPropertyValue("endpoints", "${application.service.registry}");
        String beanName = parserContext.getReaderContext().generateBeanName(registryBeanDefinitionBuilder.getBeanDefinition());
        registry.registerBeanDefinition(beanName, registryBeanDefinitionBuilder.getBeanDefinition());

        //设置service的bean依赖服务注册
        String[] dependsOn = definition.getDependsOn();
        dependsOn = ArrayUtils.add(dependsOn, beanName);
        definition.setDependsOn(dependsOn);

        return (AbstractBeanDefinition) definition;
    }

    protected void process(BeanDefinition definition) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class implClazz = Class.forName(definition.getBeanClassName());
        Class[] implIfaces = implClazz.getInterfaces();
        if (implIfaces.length != 1) {
            throw new RuntimeException("service must implement an interface");
        }
        Class implIface = implIfaces[0];

        Class processor = null;

        LOOP:
        for(Class clz : implIface.getClasses()) {
            for(Class iface : clz.getInterfaces()) {
                if (iface.equals(TProcessor.class)) {
                    processor = clz;
                    break LOOP;
                }
            }
        }

        if (processor == null) {
            throw new RuntimeException("processor class not found");
        }

        try {
            processor.getConstructor(implIface);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("illegal processor class :" + processor.getName() + " ["+e.getMessage()+"]", e);
        }

        definition.setBeanClassName(processor.getName());
        definition.getConstructorArgumentValues().addGenericArgumentValue(implClazz.newInstance());
    }
}
