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

package org.talend.dataprep.dataset.event;

import org.talend.daikon.messages.MessageTypes;
import org.talend.daikon.messages.OperationTypes;
import org.talend.daikon.messages.header.producer.MessageHeaderFactory;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.event.DaikonMessageEvent;
import org.talend.dataprep.event.MessageClass;
import org.talend.dataprep.event.MessageScope;
import org.talend.dataprep.messages.DatasetMessage;

/**
 * An event to indicate a data set content has been changed.
 */
public class DataSetRawContentUpdateEvent extends DaikonMessageEvent<DataSetMetadata, DatasetMessage> {

    public DataSetRawContentUpdateEvent(DataSetMetadata source) {
        super(source, new MessageScope[] { MessageScope.INTERNAL_UNIQUE }, OperationTypes.UPDATE);
    }

    @Override
    public DataSetMetadata getSource() {
        return (DataSetMetadata) super.getSource();
    }

    @Override
    public DatasetMessage toAvroPayload(MessageHeaderFactory messageHeaderFactory) {
        return DatasetMessage
                .newBuilder()
                .setHeader(messageHeaderFactory.createMessageHeader(MessageTypes.EVENT, "datasetUpdated",
                        OperationTypes.UPDATE))
                .setDatasetId(this.getSource().getId())
                .build();
    }

    @Override
    public MessageClass getMessageClass() {
        return MessageClass.DATASET_MESSAGE;
    }
}
