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

package org.talend.dataprep.event;

import org.springframework.context.ApplicationEvent;

/**
 * All DaikonMessageEvent will automatically be send to kafka with DaikonMessage format.
 * @param <T>
 */
public class DaikonMessageEvent<T> extends ApplicationEvent {

    public DaikonMessageEvent(T payload) {
        super(payload);
    }

    //TODO TO COMPLETE

}
