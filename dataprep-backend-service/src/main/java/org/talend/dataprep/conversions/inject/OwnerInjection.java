package org.talend.dataprep.conversions.inject;

import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.security.Security;

@Component
@Scope("prototype")
public class OwnerInjection implements BiFunction<PersistentPreparation, PreparationDTO, PreparationDTO> {

    @Autowired
    private Security security;

    private Owner owner;

    @PostConstruct
    public void init() {
        owner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
    }

    @Override
    public PreparationDTO apply(PersistentPreparation persistentPreparation, PreparationDTO dto) {
        dto.setOwner(owner);
        return dto;
    }
}
