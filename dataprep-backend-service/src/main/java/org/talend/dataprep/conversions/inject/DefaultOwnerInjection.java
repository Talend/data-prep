package org.talend.dataprep.conversions.inject;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.security.Security;

/**
 * A default implementation of {@link OwnerInjection} dedicated to environments with no security enabled.
 */
public class DefaultOwnerInjection implements OwnerInjection {

    @Autowired
    private Security security;

    private Owner defaultOwner;

    @PostConstruct
    public void init() {
        defaultOwner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
    }

    @Override
    public PreparationDTO apply(PersistentPreparation source, PreparationDTO dto) {
        if (dto.getOwner() == null) {
            dto.setOwner(defaultOwner);
        }
        return dto;
    }
}
