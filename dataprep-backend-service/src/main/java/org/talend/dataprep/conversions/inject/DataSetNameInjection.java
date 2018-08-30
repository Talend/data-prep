package org.talend.dataprep.conversions.inject;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationListItemDTO;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.security.SecurityProxy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class DataSetNameInjection
        implements BiFunction<PreparationDTO, PreparationListItemDTO, PreparationListItemDTO> {

    private final Cache<String, Cache<String, String>> cache;

    @Autowired
    private Security security;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private DatasetClient datasetClient;

    public DataSetNameInjection() {
        cache = CacheBuilder
                .newBuilder() //
                .maximumSize(10) //
                .expireAfterAccess(1, TimeUnit.MINUTES) //
                .build();
    }

    @Override
    public PreparationListItemDTO apply(PreparationDTO dto, PreparationListItemDTO item) {
        String dataSetName = dto.getDataSetName();
        if (dataSetName == null) {
            try {
                final String tenantId = security.getTenantId();
                final Cache<String, String> tenantCache = cache.get(tenantId, this::initTenant);
                if (tenantCache.getIfPresent(dto.getDataSetId()) == null) {
                    securityProxy.asTechnicalUserForDataSet();
                    try {
                        tenantCache.put(dto.getDataSetId(),
                                datasetClient.getDataSetMetadata(dto.getDataSetId()).getName());
                    } finally {
                        securityProxy.releaseIdentity();
                    }
                }
                dataSetName = tenantCache.getIfPresent(dto.getDataSetId());

                // On-the-fly update
                final PersistentPreparation preparation = preparationRepository.get(dto.getId(), PersistentPreparation.class);
                preparation.setDataSetName(dataSetName);
                preparationRepository.add(preparation);
            } catch (Exception e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        }
        item.getDataSet().setDataSetName(dataSetName);
        return item;
    }

    private Cache<String, String> initTenant() {
        return CacheBuilder
                .newBuilder() //
                .maximumSize(100) //
                .expireAfterAccess(1, TimeUnit.MINUTES) //
                .build();
    }
}
