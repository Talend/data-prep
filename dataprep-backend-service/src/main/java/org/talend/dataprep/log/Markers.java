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

package org.talend.dataprep.log;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Markers {

    private Markers() {
    }

    public static Marker dataset(String id) {
        return MarkerFactory.getMarker("DS" + id);
    }
}
