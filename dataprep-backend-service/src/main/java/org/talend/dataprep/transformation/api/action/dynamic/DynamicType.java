//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.dynamic;

import java.util.Arrays;

import org.springframework.context.ApplicationContext;
import org.talend.dataprep.transformation.api.action.dynamic.cluster.ClusterParameters;

/**
 * Enum that lists all dynamic types.
 */
public enum DynamicType {

    TEXT_CLUSTER("textclustering", ClusterParameters.class);

    /** The action parse */
    private String action;

    /** Concrete class of the dynamic parameter. */
    private Class<? extends DynamicParameters> generatorType;

    /**
     * Constructor.
     * 
     * @param action the action.
     * @param generatorType the concrete class of the dynamic parameter.
     */
    DynamicType(final String action, Class<? extends DynamicParameters> generatorType) {
        this.action = action;
        this.generatorType = generatorType;
    }

    /**
     * Return the dynamic type that matches the given action.
     * 
     * @param action the action.
     * @return the dynamic type that matches the given action or null if not found.
     */
    public static DynamicType fromAction(final String action) {
        return Arrays.stream(values()).filter(type -> type.getAction().equals(action)).findFirst().orElse(null);
    }

    /**
     * Return the instance of the type from the spring application context.
     * 
     * @param context the spring application context.
     * @return the instance of the type from the spring application context.
     */
    public DynamicParameters getGenerator(final ApplicationContext context) {
        return context.getBean(generatorType);
    }

    /**
     * @return the dynamic type action.
     */
    public String getAction() {
        return action;
    }
}
