/*
 * // ============================================================================
 * // Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * //
 * // This source code is available under agreement available at
 * // https://github.com/Talend/data-prep/blob/master/LICENSE
 * //
 * // You should have received a copy of the agreement
 * // along with this program; if not, write to Talend SA
 * // 9 rue Pages 92150 Suresnes, France
 * //
 * // ============================================================================
 */

package org.talend.dataprep.transformation.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
/**
 * This annotation is equivalent to @Scope("prototype") of Spring framework for the Provider.
 *
 * When this annotation is declared for a class, the Provider will return a new instance for each call of
 * {@link Providers#get(Class, Object...)}.
 * 
 * @see Providers
 */
public @interface PrototypeScope {

}
