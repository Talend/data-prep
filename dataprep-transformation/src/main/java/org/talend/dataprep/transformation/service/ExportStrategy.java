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

package org.talend.dataprep.transformation.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.export.ExportParameters;

public interface ExportStrategy {

    /**
     * Return whether this export strategy is applicable to the parameters or not.
     * @param parameters Export parameters (can be null).
     * @return <code>true</code> if export strategy is applicable to parameters.
     */
    boolean accept(ExportParameters parameters);

    /**
     * Execute export strategy with given parameters. Callers are expected to ensure {@link #accept(ExportParameters)}
     * returns <code>true</code> before calling this method.
     * @param parameters Export parameters (can <b>not</b> be null).
     * @return A {@link StreamingResponseBody} that streams the export data to the provided output stream.
     */
    StreamingResponseBody execute(ExportParameters parameters);
}
