/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import {
	CONTAINS,
	EXACT,
	INSIDE_RANGE,
	WILDCARD,
} from '../../../services/filter/adapter/tql-filter-adapter-service';

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
		this.WILDCARD = WILDCARD;
		this.update();
	}

	$onChanges(changes) {
		const model = changes.value;
		if (model) {
			const newModel = model.currentValue;
			if (newModel) {
				this.filter = newModel;
				this.update();
			}
		}
	}

	/**
	 * @ngdoc method
	 * @name update
	 * @methodOf data-prep.filter-item:FilterItemCtrl
	 * @description Update model on changes
	 */
	update() {
		if (this.filter) {
			this.filterValues = this.filter.value;
			switch (this.filter.type) {
			case CONTAINS:
				this.sign = '≅';
				break;
			case EXACT:
				this.sign = '=';
				break;
			case INSIDE_RANGE:
				this.sign = this.$translate.instant('IN');
				break;
			default:
				this.sign = this.$translate.instant('COLON');
			}
		}
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
