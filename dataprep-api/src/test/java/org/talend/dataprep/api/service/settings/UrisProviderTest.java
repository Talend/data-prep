// ============================================================================
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

package org.talend.dataprep.api.service.settings;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.service.ApiServiceTestBase;
import org.talend.dataprep.api.service.settings.uris.api.UriSettings;
import org.talend.dataprep.api.service.settings.uris.provider.UrisProvider;

public class UrisProviderTest extends ApiServiceTestBase {

    @Autowired
    UrisProvider urisProvider;

    @Test
    public void shouldTestgetSettings() throws Exception {

        // when
        List<UriSettings> listUriSettings = urisProvider.getSettings();

        // then
        assertThat(listUriSettings.size(), is(15));
    }

}
