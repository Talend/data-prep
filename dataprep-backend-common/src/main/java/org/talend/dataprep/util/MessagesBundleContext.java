// ============================================================================
//
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

package org.talend.dataprep.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.i18n.MessagesBundle;

/**
 * This bean and servlet Filter will hold a ThreadLocal variable with a reference to the bean {@link MessagesBundle}
 *
 * @deprecated please, use {@link org.talend.dataprep.i18n.DataprepBundle} for i18n needs.
 */
@Component
@Deprecated
public class MessagesBundleContext {

    @Autowired
    private MessagesBundle messagesBundle;

    /**
     * Instance created by Spring at instantiation.
     */
    private static MessagesBundleContext INSTANCE;

    public MessagesBundleContext() {
        synchronized (MessagesBundleContext.class) {
            INSTANCE = this;
        }
    }

    /**
     * Dirty way to keep the retrocompatibility.
     *
     * @deprecated please uses {@link org.talend.dataprep.i18n.DataprepBundle}.
     */
    @Deprecated
    public static MessagesBundle get() {
        if (INSTANCE == null) {
            synchronized (MessagesBundleContext.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MessagesBundleContext();
                    INSTANCE.messagesBundle = new MessagesBundle();
                }
            }
        }
        return INSTANCE.messagesBundle;
    }
}
