package org.talend.dataprep.url;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Updates the injected Urls into services that has Spring scope != request.
 * This is needed for the tests.
 *
 * Explaination :
 * * The services are started before the tests. When started, the service port is available, the url is dynamically defined.
 * The @Before in tests allows to override the services url defined in application.properties
 * * The beans that are not in scope "request" are instanciated before the tests @Before.
 * If they have an @Value to inject the url, that will be the value defined in application.properties, not the dynamic one.
 *
 * If you have issues with that
 * * Inject your bean with @Autowire
 * * call setField to inject the dynamic value
 */
public abstract class UrlRuntimeUpdater {

    @Value("${local.server.port}")
    protected int port;

    public abstract void setUp();

    /**
     * Set the field with the given value on the given object.
     *
     * @param service the service to update.
     * @param fieldName the field name.
     * @param value the field value.
     */
    protected void setField(Object service, String fieldName, String value) {
        try {
            ReflectionTestUtils.setField( //
                    unwrapProxy(service.getClass(), service), //
                    fieldName, //
                    value, //
                    String.class);
        } catch (IllegalArgumentException e) {
            // nothing to do here
        }
    }

    /**
     * Black magic / voodoo needed to make ReflectionTestUtils.setField(...) work on class proxied by Spring.
     *
     * @param clazz the wanted class.
     * @param proxy the proxy.
     * @return the actual object behind the proxy.
     */
    protected Object unwrapProxy(Class clazz, Object proxy) {
        if (AopUtils.isAopProxy(proxy) && proxy instanceof Advised) {
            try {
                return ((Advised) proxy).getTargetSource().getTarget();
            } catch (Exception e) {
                // nothing to do here
            }
        }
        return proxy;
    }
}
