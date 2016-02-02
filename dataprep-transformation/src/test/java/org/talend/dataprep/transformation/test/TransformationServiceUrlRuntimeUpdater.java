//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.test;

import java.util.List;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.transformation.service.BaseTransformationService;

import com.jayway.restassured.RestAssured;

/**
 * Update the TransformationService url at runtime. This is needed for the tests.
 */
@Component
@Lazy
public class TransformationServiceUrlRuntimeUpdater {

    @Value("${local.server.port}")
    protected int port;

    /** Get ALL transformation service implementations (also from the EE repository). */
    @Autowired
    private List<BaseTransformationService> transformationServices;

    /**
     * This method should be called before each test.
     */
    public void setUp() {

        RestAssured.port = port;

        // set the service url @runtime
        for (BaseTransformationService service : transformationServices) {
            setField(service, "datasetServiceUrl", "http://localhost:" + port);
            setField(service, "preparationServiceUrl", "http://localhost:" + port);
        }
    }

    /**
     * Set the field with the given value on the given object.
     *
     * @param service the service to update.
     * @param fieldName the field name.
     * @param value the field value.
     */
    private void setField(BaseTransformationService service, String fieldName, String value) {
        try {
            ReflectionTestUtils.setField( //
                    unwrapProxy(BaseTransformationService.class, service), //
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
    private Object unwrapProxy(Class clazz, Object proxy) {
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
