// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service.export;

import org.springframework.beans.factory.annotation.Value;
import org.talend.dataprep.transformation.service.BaseExportStrategy;

/**
 * Base class for all SampleExport strategies.
 *
 * By default the sample is 10k lines.
 */
public abstract class BaseSampleExportStrategy extends BaseExportStrategy implements SampleExportStrategy {

    @Value("${dataset.records.limit:10000}")
    protected long limit;

}
