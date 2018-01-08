/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.configuration;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.talend.dataprep.util.SortAndOrderHelper;

@ControllerAdvice
public class SortAndOrderSerializationAdvice {

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        // This allow to bind Sort and Order parameters in lower-case even if the key is uppercase.
        // URLs are cleaner in lowercase.
        binder.registerCustomEditor(SortAndOrderHelper.Sort.class, SortAndOrderHelper.getSortPropertyEditor());
        binder.registerCustomEditor(SortAndOrderHelper.Order.class, SortAndOrderHelper.getOrderPropertyEditor());
        binder.registerCustomEditor(SortAndOrderHelper.Format.class, SortAndOrderHelper.getFormatPropertyEditor());
    }

}
