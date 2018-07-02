package org.talend.dataprep.conversions.inject;

import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;
import org.talend.dataprep.security.Security;

public class OwnerInjection<T extends SharedResource> implements BiFunction<Object, T, T> {

    @Autowired
    private Security security;

    private Owner owner;

    @PostConstruct
    public void init() {
        owner = new Owner(security.getUserId(), security.getUserDisplayName(), StringUtils.EMPTY);
    }

    @Override
    public T apply(Object persistentPreparation, T dto) {
        dto.setOwner(owner);
        return dto;
    }
}
