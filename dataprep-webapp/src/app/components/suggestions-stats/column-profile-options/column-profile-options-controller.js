/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const AGGREGATIONS = ['SUM', 'MAX', 'MIN', 'AVERAGE'];
const LINE_COUNT = 'LINE_COUNT';

/**
 * @ngdoc controller
 * @name data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
 * @description Column profile header controller
 */
export default class ColumnProfileOptionsCtrl {
	constructor($translate) {
		'ngInject';

		this.$translate = $translate;
		this.aggregations = AGGREGATIONS;
	}

	$onInit() {
		this._updateNumericColumns();
		this._updateCurrentDescription();
		this._updateCurrentShortDescription();
		this.resetSelected();
	}

	$onChanges(changes) {
		if (changes.numericColumns || changes.group) {
			this._updateNumericColumns();
		}

		if (changes.column || changes.aggregation || changes.group) {
			this._updateCurrentDescription();
			this._updateCurrentShortDescription();
		}
	}

    /**
     * @ngdoc method
     * @name selectColumn
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Select a column for aggregation callback.
     * It refresh the description and init aggregation type.
     */
	selectColumn() {
		this.selected.aggregation = this.selected.column ?
            (this.aggregation || AGGREGATIONS[0]) :
            null;
		this.updateSelectedDescription();
	}

    /**
     * @ngdoc method
     * @name resetSelected
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Reset selected infos with the initial values.
     * This updates the description too.
     */
	resetSelected() {
		this.selected = {
			column: this.column,
			aggregation: this.aggregation,
		};
		this.updateSelectedDescription();
	}

    /**
     * @ngdoc method
     * @name changeAggregation
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Perform aggregation change. This is triggered on fom validation.
     */
	changeAggregation() {
		const config = {
			column: this.selected.column,
			aggregation: this.selected.aggregation,
		};

		if (config.column === this.column && config.aggregation === this.aggregation) {
			return;
		}

		this.onAggregationChange(config);
	}

    /**
     * @ngdoc method
     * @name removeAggregation
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Remove aggregation.
     */
	removeAggregation() {
		this.selected.column = undefined;
		this.selected.aggregation = undefined;
		this.changeAggregation();
	}

    /**
     * @ngdoc method
     * @name updateSelectedDescription
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Update the description of selected aggregation values
     */
	updateSelectedDescription() {
		if (!this.selected.column) {
			this.selected.description = null;
		}
		else {
			const selectedOptions = {
				aggreg: this.$translate.instant(this.selected.aggregation),
				col: this.selected.column.name,
				group: this.group.name,
			};
			this.selected.description = this.$translate.instant('AGGREGATION_DETAILS', selectedOptions);
		}
	}

    /**
     * @ngdoc method
     * @name _updateCurrentDescription
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Update the description of initial aggregation values (not the selected ones)
     */
	_updateCurrentDescription() {
		if (!this.column) {
			this.description = null;
		}
		else {
			const currentOptions = {
				aggreg: this.$translate.instant(this.aggregation),
				col: this.column.name,
				group: this.group.name,
			};

			this.$translate('AGGREGATION_DETAILS', currentOptions)
                .then((desc) => {
	this.description = desc;
});
		}
	}

    /**
     * @ngdoc method
     * @name _updateCurrentShortDescription
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description Update the short description of initial aggregation values
     */
	_updateCurrentShortDescription() {
		if (this.aggregation) {
			this.$translate(this.aggregation)
                .then((aggreg) => {
	this.shortDescription = `${aggreg} ${this.column.name}`;
});
		}
		else {
			this.shortDescription = this.$translate.instant(LINE_COUNT);
		}
	}

    /**
     * @ngdoc method
     * @name _updateNumericColumns
     * @methodOf data-prep.column-profile-options.controller:ColumnProfileOptionsCtrl
     * @description exclude grouping column from filteredNumericColumns
     */
	_updateNumericColumns() {
		this.filteredNumericColumns = this.group ?
            this.numericColumns.filter(col => col.id !== this.group.id) :
            this.numericColumns;
	}
}
