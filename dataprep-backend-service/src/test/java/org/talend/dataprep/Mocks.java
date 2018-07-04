// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep;

import static org.mockito.Mockito.when;

import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.info.Version;

public class Mocks {

    public static void configure(VersionService versionService) {
        when(versionService.version()).thenReturn(new Version("0.0.0", "abdce"));
    }

}
