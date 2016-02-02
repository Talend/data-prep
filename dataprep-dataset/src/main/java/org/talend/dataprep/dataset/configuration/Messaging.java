//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.configuration;

import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class Messaging {

    @Bean
    public JmsTemplate getJmsTemplate() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost"); //$NON-NLS-1$
        connectionFactory.setOptimizeAcknowledge(true);
        connectionFactory.setAlwaysSyncSend(true);
        PooledConnectionFactory pool = new PooledConnectionFactory();
        pool.setConnectionFactory(connectionFactory);
        JmsTemplate jmsTemplate = new JmsTemplate(pool);
        jmsTemplate.setDeliveryPersistent(false);
        jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return jmsTemplate;
    }

}
