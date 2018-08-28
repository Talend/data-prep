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

package org.talend.dataprep.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.audit.DataprepEventAuditLogger;
import org.talend.logging.audit.AuditLoggerFactory;

/**
 * The configuration file for the Audit logger.
 */
@Configuration
public class AuditLoggerConfiguration {

    @Bean
    @ConditionalOnProperty(name = "audit.log.enabled", havingValue = "true")
    public DataprepEventAuditLogger auditLogger() {
        return AuditLoggerFactory.getEventAuditLogger(DataprepEventAuditLogger.class);
    }

}
