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

import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.talend.daikon.messages.OperationTypes;
import org.talend.daikon.messages.header.producer.MessageHeaderFactory;

/**
 * All DaikonMessageEvent will automatically be send to kafka with DaikonMessage format.
 *
 * @param <T>
 */
public abstract class DaikonMessageEvent<T, S> extends ApplicationEvent {

    private List<MessageScope> scopes;

    private OperationTypes operationTypes;

    public DaikonMessageEvent(T payload, MessageScope[] scopes, OperationTypes operationTypes) {
        super(payload);
        this.scopes = Arrays.asList(scopes);
        this.operationTypes = operationTypes;
    }

    public abstract S toAvroPayload(MessageHeaderFactory messageHeaderFactory);

    public List<MessageScope> getScopes() {
        return scopes;
    }

    public OperationTypes getOperationTypes() {
        return operationTypes;
    }

    public abstract MessageClass getMessageClass();
}
