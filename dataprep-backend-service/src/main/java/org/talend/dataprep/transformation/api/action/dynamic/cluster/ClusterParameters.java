//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.dynamic.cluster;

import static org.apache.commons.lang.StringUtils.EMPTY;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.i18n.DataprepBundle;
import org.talend.dataprep.parameters.ClusterItem;
import org.talend.dataprep.parameters.Clusters;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.dynamic.DynamicParameters;
import org.talend.dataprep.transformation.api.action.dynamic.GenericParameter;
import org.talend.dataquality.record.linkage.analyzer.PostMerge;
import org.talend.dataquality.record.linkage.analyzer.StringClusters;
import org.talend.dataquality.record.linkage.analyzer.StringsClusterAnalyzer;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;

/**
 * Cluster action dynamic parameter generator It takes an InputStream as argument, containing the dataset
 */
@Component
public class ClusterParameters implements DynamicParameters {

    @Override
    public GenericParameter getParameters(final String columnId, final DataSet content) {
        // Analyze clusters service
        StringsClusterAnalyzer clusterAnalyzer = new StringsClusterAnalyzer();
        clusterAnalyzer.withPostMerges(new PostMerge(AttributeMatcherType.SOUNDEX, 0.8f));
        clusterAnalyzer.init();
        content.getRecords().forEach(row -> {
            String value = row.get(columnId);
            clusterAnalyzer.analyze(value);
        });
        clusterAnalyzer.end();
        // Build results
        final Clusters.Builder builder = Clusters
                .builder()
                .title(DataprepBundle.message("parameter.textclustering.title.1"))
                .title(DataprepBundle.message("parameter.textclustering.title.2"));
        final StringClusters result = clusterAnalyzer.getResult().get(0);
        for (StringClusters.StringCluster cluster : result) {
            // String clustering may cluster null / empty values, however not interesting for data prep.
            if (!StringUtils.isEmpty(cluster.survivedValue)) {
                final ClusterItem.Builder currentCluster = ClusterItem.builder();
                for (String value : cluster.originalValues) {
                    currentCluster.parameter(new ConstantParameter(value, ParameterType.BOOLEAN));
                }
                currentCluster.replace(Parameter.parameter(LocaleContextHolder.getLocale()).setName("replaceValue")
                        .setType(ParameterType.STRING)
                        .setDefaultValue(cluster.survivedValue)
                        .build(null));
                builder.cluster(currentCluster);
            }
        }
        return new GenericParameter("cluster", builder.build());
    }

    private static class ConstantParameter extends Parameter {

        private ConstantParameter(String value, ParameterType type) {
            super(value, type, null, false, true, EMPTY, EMPTY, EMPTY, true);
        }

        @Override
        public String getLabel() {
            return super.getName();
        }

        @Override
        public String getDescription() {
            return super.getName();
        }
    }
}
