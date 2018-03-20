// ============================================================================
//
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

package org.talend.dataprep.api.service.version;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.service.command.info.VersionCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.info.Version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;

public abstract class AbstractVersionSupplier implements VersionsSupplier {

    protected static final String VERSION_ENTRY_POINT = "/version";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVersionSupplier.class);

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected ApplicationContext context;

    /**
     * Call the version service on the given service: dataset, preparation or transformation.
     *
     * @param serviceName the name of the service
     * @return the version of the called service
     */
    protected Version callVersionService(String serviceUrl, String serviceName, String entryPoint) {
        HystrixCommand<InputStream> versionCommand = context.getBean(VersionCommand.class, serviceUrl, entryPoint);
        try (InputStream content = versionCommand.execute()) {
            final Version version = mapper.readerFor(Version.class).readValue(content);
            version.setServiceName(serviceName);
            return version;
        } catch (IOException | TDPException e) {
            LOGGER.warn("Unable to get the version of the service {}", serviceName, e);
            return null;
        }
    }
}
