package org.talend.dataprep.preparation.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.adapter.DataCatalogClient;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.security.SecurityProxy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Inject dataset name into preparations to gain time in preparation listing.
 * This also migrates preparation in database to minimize database access (denormalization).
 */
@Component
public class DataSetNameInjection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetNameInjection.class);

    private final Cache<String, Cache<String, String>> cache = Caffeine
            .newBuilder() //
            .maximumSize(10) //
            .expireAfterAccess(1, TimeUnit.MINUTES) //
            .build();

    @Autowired
    private Security security;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private DataCatalogClient dataCatalogClient;

    @Autowired
    protected PreparationRepository preparationRepository;

    public PersistentPreparation injectDatasetNameBasedOnId(PersistentPreparation preparation) {
        String dataSetName = preparation.getDataSetName();
        if (dataSetName == null) {
            try {
                final String tenantId = security.getTenantId();
                final Cache<String, String> tenantCache = cache.get(tenantId, this::createTenantCache);
                assert tenantCache != null; // initTenant() cannot return a null value
                String dataSetId = preparation.getDataSetId();
                dataSetName = tenantCache.get(dataSetId, this::getDatasetLabel);
                if (dataSetName != null) {
                    preparation.setDataSetName(dataSetName);
                    preparationRepository.add(preparation);
                }
            } catch (Exception e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
        return preparation;
    }

    @SuppressWarnings("unused") // needed to fit the cache get method
    private Cache<String, String> createTenantCache(String tenantId) {
        return Caffeine
                .newBuilder() //
                .maximumSize(100) //
                .expireAfterAccess(1, TimeUnit.MINUTES) //
                .build();
    }

    private String getDatasetLabel(String dataSetId) {
        securityProxy.asTechnicalUserForDataSet();
        try {
            Dataset metadata = dataCatalogClient.getMetadata(dataSetId);
            return metadata == null ? null : metadata.getLabel();
        } catch (TDPException e) {
            // I so love exception-driven programming...
            // happen when there is no matching dataset AND there is no dataset name.
            // dunno how we are matching this preparation with a dataset now
            LOGGER.warn(
                    "Unable to find data set name of id #{} for legacy preparation import (dataset does not exist).",
                    dataSetId);
            return null;
        } catch (Exception e) {
            // Failsafe when, for instance, Hystrix circuit breaker is OPEN
            LOGGER.warn("Unable to find data set name of id #" + dataSetId
                    + " for legacy preparation import. An unexpected exception occurred", e);
            return null;
        } finally {
            securityProxy.releaseIdentity();
        }
    }
}
