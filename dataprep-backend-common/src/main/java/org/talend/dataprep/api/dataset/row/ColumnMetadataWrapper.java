// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.dataset.row;

import static org.talend.dataprep.api.dataset.row.RowMetadataUtils.DATAPREP_FIELD_PREFIX;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.dataset.statistics.Statistics;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ColumnMetadataWrapper extends ColumnMetadata {

    private transient Logger logger;

    private transient Schema.Field field;

    public ColumnMetadataWrapper(Schema.Field field) {
        this.field = field;
    }

    @Override
    public String getId() {
        final String name = field.name();
        if (name.startsWith(DATAPREP_FIELD_PREFIX)) {
            return name.substring(DATAPREP_FIELD_PREFIX.length());
        } else {
            return name;
        }
    }

    @Override
    public String getName() {
        return readWrappedColumn().getName();
    }

    private ColumnMetadata readWrappedColumn() {
        try {
            return new ObjectMapper().reader(ColumnMetadata.class).readValue(field.getProp("_dp_column"));
        } catch (IOException e) {
            getLogger().error("Unable to read from wrapped column.", e);
        }
        return null;
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(ColumnMetadataWrapper.class);
        }
        return logger;
    }

    private void updateWrappedColumn(Consumer<ColumnMetadata> updater) {
        final ColumnMetadata column = readWrappedColumn();
        updater.accept(column);
        field = RowMetadataUtils.toField(column);
    }

    @Override
    public void setName(String name) {
        updateWrappedColumn(c -> c.setName(name));
    }

    @Override
    public String getType() {
        return readWrappedColumn().getType();
    }

    @Override
    public void setType(String typeName) {
        updateWrappedColumn(c -> c.setType(typeName));
    }

    @Override
    public String getDiffFlagValue() {
        return readWrappedColumn().getDiffFlagValue();
    }

    @Override
    public void setDiffFlagValue(String diffFlagValue) {
        updateWrappedColumn(c -> c.setDiffFlagValue(diffFlagValue));
    }

    @Override
    public int getHeaderSize() {
        return readWrappedColumn().getHeaderSize();
    }

    @Override
    public void setHeaderSize(int headerSize) {
        updateWrappedColumn(c -> c.setHeaderSize(headerSize));
    }

    @Override
    public Quality getQuality() {
        return readWrappedColumn().getQuality();
    }

    @Override
    public boolean isDomainForced() {
        return readWrappedColumn().isDomainForced();
    }

    @Override
    public void setDomainForced(boolean domainForced) {
        updateWrappedColumn(c -> c.setDomainForced(domainForced));
    }

    @Override
    public boolean isTypeForced() {
        return readWrappedColumn().isTypeForced();
    }

    @Override
    public void setTypeForced(boolean typeForced) {
        updateWrappedColumn(c -> c.setTypeForced(typeForced));
    }

    @Override
    public Statistics getStatistics() {
        return readWrappedColumn().getStatistics();
    }

    @Override
    public void setStatistics(Statistics statistics) {
        updateWrappedColumn(c -> c.setStatistics(statistics));
    }

    @Override
    public void setDomain(String domain) {
        updateWrappedColumn(c -> c.setDomain(domain));
    }

    @Override
    public String getDomain() {
        return readWrappedColumn().getDomain();
    }

    @Override
    public String getDomainLabel() {
        return readWrappedColumn().getDomainLabel();
    }

    @Override
    public void setDomainLabel(String domainLabel) {
        updateWrappedColumn(c -> c.setDomainLabel(domainLabel));
    }

    @Override
    public List<SemanticDomain> getSemanticDomains() {
        return readWrappedColumn().getSemanticDomains();
    }

    @Override
    public void setSemanticDomains(List<SemanticDomain> semanticDomains) {
        updateWrappedColumn(c -> c.setSemanticDomains(semanticDomains));
    }

    @Override
    public float getDomainFrequency() {
        return readWrappedColumn().getDomainFrequency();
    }

    @Override
    public void setDomainFrequency(float domainFrequency) {
        updateWrappedColumn(c -> c.setDomainFrequency(domainFrequency));
    }

    @Override
    public void setId(String id) {
        updateWrappedColumn(c -> c.setId(id));
    }

}
