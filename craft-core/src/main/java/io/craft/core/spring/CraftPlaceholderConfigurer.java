package io.craft.core.spring;

import io.craft.core.config.ConfigClient;
import io.craft.core.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.lang.Nullable;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

public class CraftPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements PropertyManager {

    private static final Logger logger = LoggerFactory.getLogger(CraftPlaceholderConfigurer.class);

    private final PropertyPlaceholderConfigurerResolver configurerResolver;

    private final Properties properties;

    private ConfigClient<ConfigClient.NamespaceEvent> configClient;

    private int systemPropertiesMode;

    public CraftPlaceholderConfigurer() {
        properties = new Properties();
        configurerResolver = new PropertyPlaceholderConfigurerResolver(properties);
        systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;
    }

    private void initSystemPropertiesMode() {
        Class clazz = PropertyPlaceholderConfigurer.class;

        try {
            Field field = clazz.getDeclaredField("systemPropertiesMode");
            field.setAccessible(true);
            systemPropertiesMode = field.getInt(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void setConfigClient(ConfigClient<ConfigClient.NamespaceEvent> configClient) {
        this.configClient = configClient;
    }

    @Override
    public void setSystemPropertiesMode(int systemPropertiesMode) {
        super.setSystemPropertiesMode(systemPropertiesMode);
        initSystemPropertiesMode();
    }

    @Override
    public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
        super.setSystemPropertiesModeName(constantName);
        initSystemPropertiesMode();
    }

    @Override
    public String getProperty(String name) {
        return getProperty(name, null);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        String value = resolvePlaceholder(name, properties, systemPropertiesMode);
        if (value == null && defaultValue != null) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);
        doProcessProperties(beanFactory, new PlaceholderResolvingStringValueResolver(configurerResolver));
        if (configClient == null) {
            //如果没有configClient，不需要加载远程配置
            return;
        }
        try {
            loadProperty(beanFactory);
        } catch (Exception e) {
            throw new BeansException(e.getMessage(), e){};
        }
        //注册服务

    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        properties.putAll(props);
    }

    protected void loadProperty(ConfigurableListableBeanFactory beanFactory) throws Exception {
        String applicationNamespace = properties.getProperty(Constants.APPLICATION_NAMESPACE);
        String applicationName = properties.getProperty(Constants.APPLICATION_NAME);
        String prefix = applicationNamespace + applicationName + Constants.PROPERTIES_PATH;
        Properties properties = configClient.watch(prefix, event -> {
            if (event.getValue() != null) {
                Properties props = new Properties();
                props.setProperty(event.getKey(), event.getValue());
                processProperties(beanFactory, props);
            } else {
                this.properties.remove(event.getKey());
            }
            Map<String, PropertyChangeHandler> handlers = beanFactory.getBeansOfType(PropertyChangeHandler.class);
            if (handlers != null) {
                for(Map.Entry<String, PropertyChangeHandler> entry : handlers.entrySet()) {
                    try {
                        entry.getValue().accept(event.getKey(), event.getValue(), event.getOldValue());
                    } catch (Exception e) {
                        logger.error("change handler accept error:{}", e.getMessage(), e);
                    }
                }
            }
        });
        processProperties(beanFactory, properties);
    }

    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final PropertyPlaceholderHelper helper;

        private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

        public PlaceholderResolvingStringValueResolver(PropertyPlaceholderConfigurerResolver resolver) {
            this.helper = new PropertyPlaceholderHelper(
                    placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
            this.resolver = resolver;
        }

        @Override
        @Nullable
        public String resolveStringValue(String strVal) throws BeansException {
            String resolved = this.helper.replacePlaceholders(strVal, this.resolver);
            if (trimValues) {
                resolved = resolved.trim();
            }
            return (resolved.equals(nullValue) ? null : resolved);
        }
    }


    private final class PropertyPlaceholderConfigurerResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private final Properties props;

        private PropertyPlaceholderConfigurerResolver(Properties props) {
            this.props = props;
        }

        @Override
        @Nullable
        public String resolvePlaceholder(String placeholderName) {
            return CraftPlaceholderConfigurer.this.resolvePlaceholder(placeholderName,
                    this.props, systemPropertiesMode);
        }
    }

}
