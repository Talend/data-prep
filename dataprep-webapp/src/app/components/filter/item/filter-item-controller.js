/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.filter-item.controller:FilterItemCtrl
 * @description FilterItem controller.
 */
export default class FilterItemCtrl {

	constructor($translate) {
		'ngInject';

		this.$translate = $translate;
	}

	$onInit() {
		this.filter = this.value;
		if (this.filter) {
			this.filterValues = this.filter.value;
			switch (this.filter.type) {
			case 'contains':
				this.sign = '≅';
				break;
			case 'exact':
				this.sign = '=';
				break;
			case 'inside_range':
				this.sign = this.$translate.instant('IN');
				break;
			default:
				this.sign = this.$translate.instant('COLON');
			}
		}
	}

	$onChanges(changes) {
		const model = changes.value;
		if (model) {
			const newModel = model.currentValue;
			if (newModel) {
				this.filter = newModel;
				this.filterValues = this.filter.value;
			}
		}
	}

    /**
     * @ngdoc method
     * @name getBadgeClassByFilterType
     * @methodOf data-prep.filter-item:FilterItemCtrl
     * @description get badge class by filter type
     */
	getBadgeClassByFilterType(filter) {
		if (filter.type === 'quality' && filter.args && filter.args.invalid && filter.args.empty) {
			return 'invalid_empty_records';
		}
		return filter.type;
	}

    /**
     * @ngdoc method
     * @name submit
     * @methodOf data-prep.filter-item:FilterItemCtrl
     * @description Apply changes
     */
	edit(index, value) {
		const filterValue = this.filterValues[index];
		if (filterValue) {
			filterValue.value = value;
			this.submit();
		}
	}

    /**
     * @ngdoc method
     * @name remove
     * @methodOf data-prep.filter-item:FilterItemCtrl
     * @description Remove criterion from a multi-valued filter
     * @param indexToRemove Position into the multi-valued list
     */
	remove(indexToRemove) {
		this.onEdit({
			filter: this.filter,
			value: this.filterValues.filter((value, index) => index !== indexToRemove),
		});
	}

    /**
     * @ngdoc method
     * @name submit
     * @methodOf data-prep.filter-item:FilterItemCtrl
     * @description Submit updated filter values
     */
	submit() {
		this.onEdit({
			filter: this.filter,
			value: this.filterValues,
		});
	}

    /**
     * @ngdoc method
     * @name close
     * @methodOf data-prep.filter-item:FilterItemCtrl
     * @description Trigger the close callback
     */
	close() {
		this.onRemove({
			filter: this.filter,
		});
	}
}
