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

import org.talend.daikon.messages.MessageTypes;
import org.talend.daikon.messages.header.producer.MessageHeaderFactory;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.messages.CacheMessage;
import org.talend.dataprep.messages.OperationTypes;

/**
 * Event sent when the dataset cache needs to be cleaned.
 */
public class CleanCacheEvent extends DaikonMessageEvent<ContentCacheKey, CacheMessage> {

    /**
     * For the Serialization interface.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Is it a partial key ?
     */
    private boolean partialKey;

    /**
     * Create a new CleanCacheEvent event.
     *
     * @param cacheKey the key to delete on cache
     */
    public CleanCacheEvent(ContentCacheKey cacheKey) {
        this(cacheKey, Boolean.FALSE);
    }

    /**
     * Create a new CleanCacheEvent event.
     *
     * @param cacheKey the key to delete on cache
     */
    public CleanCacheEvent(ContentCacheKey cacheKey, Boolean partialKey) {
        super(cacheKey, new MessageScope[] { MessageScope.INTERNAL_UNIQUE });
        this.partialKey = partialKey;
    }

    @Override
    public ContentCacheKey getSource() {
        return (ContentCacheKey) super.getSource();
    }

    @Override
    public CacheMessage toAvroPayload(MessageHeaderFactory messageHeaderFactory) {
        return CacheMessage
                .newBuilder()
                .setHeader(messageHeaderFactory.createMessageHeader(MessageTypes.EVENT, "deleteCache"))
                .setOperationType(OperationTypes.DELETION)
                .setPartialKey(this.partialKey)
                .setCacheKey(this.getSource().getKey())
                .build();
    }

    @Override
    public MessageClass getMessageClass() {
        return MessageClass.CACHE_MESSAGE;
    }

    public boolean isPartialKey() {
        return partialKey;
    }

}
