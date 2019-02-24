package io.craft.core.spring;

import com.google.common.collect.Lists;
import io.craft.core.config.EtcdClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.net.URI;
import java.util.List;

public class CraftPlaceholderDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String SYSTEM_PROPERTIES_MODE_ATTRIBUTE = "system-properties-mode";

    private static final String SYSTEM_PROPERTIES_MODE_DEFAULT = "ENVIRONMENT";

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return CraftPlaceholderConfigurer.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        String location = element.getAttribute("location");
        List<String> locals = Lists.newArrayList();
        List<String> etcdServers = Lists.newArrayList();
        if (StringUtils.hasLength(location)) {
            location = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(location);
            String[] locations = StringUtils.commaDelimitedListToStringArray(location);
            for(String loc : locations) {
                URI uri = URI.create(loc);
                if (!uri.isAbsolute()) {
                    locals.add(loc);
                } else if (uri.getScheme().equals("etcd")) {
                    etcdServers.add(loc.replaceFirst("etcd", "http"));
                } else {
                    //忽略不认识的scheme
                }
            }
            builder.addPropertyValue("locations", locals.toArray());
        }

        if (CollectionUtils.isNotEmpty(etcdServers)) {
            BeanDefinitionBuilder clientDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(EtcdClient.class);
            clientDefinitionBuilder.addPropertyValue("endpoints", etcdServers);
            clientDefinitionBuilder.addPropertyValue("keepAlive", false);
            clientDefinitionBuilder.setInitMethodName("init");
            clientDefinitionBuilder.setDestroyMethodName("close");
            String beanName = parserContext.getReaderContext().generateBeanName(clientDefinitionBuilder.getBeanDefinition());
            parserContext.getRegistry().registerBeanDefinition(beanName, clientDefinitionBuilder.getBeanDefinition());
            builder.addPropertyReference("configClient", beanName);
        }

        String propertiesRef = element.getAttribute("properties-ref");
        if (StringUtils.hasLength(propertiesRef)) {
            builder.addPropertyReference("properties", propertiesRef);
        }

        String fileEncoding = element.getAttribute("file-encoding");
        if (StringUtils.hasLength(fileEncoding)) {
            builder.addPropertyValue("fileEncoding", fileEncoding);
        }

        String order = element.getAttribute("order");
        if (StringUtils.hasLength(order)) {
            builder.addPropertyValue("order", Integer.valueOf(order));
        }

        builder.addPropertyValue("ignoreResourceNotFound",
                Boolean.valueOf(element.getAttribute("ignore-resource-not-found")));

        builder.addPropertyValue("localOverride",
                Boolean.valueOf(element.getAttribute("local-override")));

        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        builder.addPropertyValue("ignoreUnresolvablePlaceholders",
                Boolean.valueOf(element.getAttribute("ignore-unresolvable")));

        String systemPropertiesModeName = element.getAttribute(SYSTEM_PROPERTIES_MODE_ATTRIBUTE);
        if (StringUtils.hasLength(systemPropertiesModeName) &&
                !systemPropertiesModeName.equals(SYSTEM_PROPERTIES_MODE_DEFAULT)) {
            builder.addPropertyValue("systemPropertiesModeName", "SYSTEM_PROPERTIES_MODE_" + systemPropertiesModeName);
        }

        if (element.hasAttribute("value-separator")) {
            builder.addPropertyValue("valueSeparator", element.getAttribute("value-separator"));
        }
        if (element.hasAttribute("trim-values")) {
            builder.addPropertyValue("trimValues", element.getAttribute("trim-values"));
        }
        if (element.hasAttribute("null-value")) {
            builder.addPropertyValue("nullValue", element.getAttribute("null-value"));
        }
    }
}
