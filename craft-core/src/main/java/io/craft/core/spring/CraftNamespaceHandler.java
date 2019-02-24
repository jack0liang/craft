package io.craft.core.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CraftNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("service", new CraftServiceDefinitionParser());
        registerBeanDefinitionParser("property-placeholder", new CraftPlaceholderDefinitionParser());
    }
}
