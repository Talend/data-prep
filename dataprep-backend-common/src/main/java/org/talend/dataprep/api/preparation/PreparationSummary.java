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

package org.talend.dataprep.api.preparation;

import org.talend.dataprep.api.share.Owner;

public class PreparationSummary {
    private String id;

    private String name;

    private Owner owner;

    private long lastModificationDate;

    private boolean allowDistributedRun;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public boolean isAllowDistributedRun() {
        return allowDistributedRun;
    }

    public void setAllowDistributedRun(boolean allowDistributedRun) {
        this.allowDistributedRun = allowDistributedRun;
    }
}
