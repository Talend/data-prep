/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './column-profile-options.html';

import ColumnProfileOptionsCtrl from './column-profile-options-controller';

/**
 * @ngdoc component
 * @name data-prep.column-profile-options.component:ColumnProfileOptionsComponent
 * @description Component that renders the chart options
 * @restrict E
 * @usage
    <column-profile-options
         numeric-columns="numericColumns"
         aggregation="currentAggregation"
         column="currentAggregationColumn"
         group="selectedColumn"
         on-aggregation-change="changeAggregation(column, aggregation)"></column-profile-options>
 * @param {Array} numericColumns Numeric columns metadata
 * @param {String} aggregation Aggregation type
 * @param {object} column Aggregation on this column
 * @param {object} group Selected column (group aggregation by this column)
 * @param {function} onAggregationChange Aggregation change callback
 */
const ColumnProfileOptionsComponent = {
	templateUrl: template,
	bindings: {
		numericColumns: '<',
		aggregation: '<',
		column: '<',
		group: '<',
		onAggregationChange: '&',
	},
	controller: ColumnProfileOptionsCtrl,
};

export default ColumnProfileOptionsComponent;
