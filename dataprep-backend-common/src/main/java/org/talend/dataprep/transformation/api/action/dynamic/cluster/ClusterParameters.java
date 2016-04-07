//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.dynamic.DynamicParameters;
import org.talend.dataprep.transformation.api.action.dynamic.GenericParameter;
import org.talend.dataprep.parameters.ClusterItem;
import org.talend.dataprep.parameters.Clusters;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataquality.record.linkage.constant.AttributeMatcherType;
import org.talend.datascience.common.recordlinkage.PostMerge;
import org.talend.datascience.common.recordlinkage.StringClusters;
import org.talend.datascience.common.recordlinkage.StringsClusterAnalyzer;

/**
 * Cluster action dynamic parameter generator It takes an InputStream as argument, containing the dataset
 */
@Component
public class ClusterParameters implements DynamicParameters {

    @Autowired
    private MessagesBundle messagesBundle;

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
                .title(messagesBundle.getString("parameter.textclustering.title.1"))
                .title(messagesBundle.getString("parameter.textclustering.title.2"));
        final StringClusters result = clusterAnalyzer.getResult().get(0);
        for (StringClusters.StringCluster cluster : result) {
            // String clustering may cluster null / empty values, however not interesting for data prep.
            if (!StringUtils.isEmpty(cluster.survivedValue)) {
                final ClusterItem.Builder currentCluster = ClusterItem.builder();
                for (String value : cluster.originalValues) {
                    currentCluster.parameter(new Parameter(value, ParameterType.BOOLEAN));
                }
                currentCluster.replace(new Parameter("replaceValue", ParameterType.STRING, cluster.survivedValue));
                builder.cluster(currentCluster);
            }
        }
        return new GenericParameter("cluster", builder.build());
    }
}
