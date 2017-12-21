package org.talend.dataprep.configuration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

class ActionsImport implements ImportBeanDefinitionRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionsImport.class);

    private static final String ACTION_BEAN_PREFIX = "action#";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // Create the annotation-based context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context, false);
        AtomicInteger actionCount = new AtomicInteger(0);
        scanner.setBeanNameGenerator((definition, beanRegistry) -> {
            try {
                final Class<?> clazz = Class.forName(definition.getBeanClassName());
                return ACTION_BEAN_PREFIX + AnnotationUtils.findAnnotation(clazz, Action.class).value();
            } catch (Exception e) {
                // Rather unexpected, filter must have found and check the class earlier.
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                actionCount.incrementAndGet();
            }
        });
        scanner.addIncludeFilter(new ActionFilter());
        scanner.scan("org.talend.dataprep");
        // Import scanned services in current registry
        final String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            final BeanDefinition definition = context.getBeanDefinition(name);
            registry.registerBeanDefinition(name, definition);
        }
        LOGGER.info("{} action(s) found.", actionCount.get());
    }

    private static class ActionFilter implements TypeFilter {

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            final ClassMetadata classMetadata = metadataReader.getClassMetadata();
            try {
                final Class<?> clazz = Class.forName(classMetadata.getClassName());
                return AnnotationUtils.findAnnotation(clazz, Action.class) != null;
            } catch (Throwable e) { // NOSONAR
                if (!LOGGER.isDebugEnabled()) {
                    LOGGER.error("Unable to filter class {}.", classMetadata.getClassName());
                } else {
                    LOGGER.debug("Unable to filter class {}.", classMetadata.getClassName(), e);
                }
            }
            return false;
        }
    }

}
