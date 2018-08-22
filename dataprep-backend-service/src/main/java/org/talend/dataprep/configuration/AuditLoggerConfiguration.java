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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.talend.dataprep.info.ClassPathManifestInfoProvider;
import org.talend.dataprep.info.ManifestInfo;
import org.talend.dataprep.info.ManifestInfoProvider;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.StandardEventAuditLogger;

/**
 * The configuration file for the Audit logger.
 */
@Configuration
public class AuditLoggerConfiguration {

    @Bean
    public StandardEventAuditLogger auditLogger() {
        return AuditLoggerFactory.getEventAuditLogger(StandardEventAuditLogger.class);
    }

}
