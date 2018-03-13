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
import org.talend.dataprep.event.DaikonMessageEvent;
import org.talend.dataprep.event.MessageClass;
import org.talend.dataprep.event.MessageScope;
import org.talend.dataprep.messages.DatasetMessage;

/**
 * Event sent when a DataSet was just imported (good starting point to start asynchronous analysis).
 */
public class DatasetImportedEvent extends DaikonMessageEvent<String, DatasetMessage> {

    /** For the Serialization interface. */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     *
     * @param datasetId the imported dataset id.
     */
    public DatasetImportedEvent(String datasetId) {
        super(datasetId, new MessageScope[] { MessageScope.INTERNAL_UNIQUE });
    }

    /**
     * @return the DatasetId
     */
    public String getSource() {
        return (String) source;
    }

    @Override
    public DatasetMessage toAvroPayload(MessageHeaderFactory messageHeaderFactory) {
        return DatasetMessage
                .newBuilder()
                .setHeader(messageHeaderFactory.createMessageHeader(MessageTypes.EVENT, "datasetCreated",
                        OperationTypes.CREATION))
                .setDatasetId(this.getSource())
                .build();
    }

    @Override
    public MessageClass getMessageClass() {
        return MessageClass.DATASET_MESSAGE;
    }
}
