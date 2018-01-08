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
 * @name data-prep.filter-item-value.controller:FilterValueCtrl
 * @description FilterItemValue controller.
 */
export default class FilterValueCtrl {
	constructor(TextFormatService) {
		'ngInject';

		this.TextFormatService = TextFormatService;
	}

	$onInit() {
		this.filterValue = this.value;
		this.valueToDisplay = this.filterValue;
	}

	$onChanges(changes) {
		const model = changes.value;
		if (model) {
			const newModel = model.currentValue;
			if (newModel) {
				this.filterValue = newModel;
				this.valueToDisplay = this.filterValue;
			}
		}
	}

    /**
     * @ngdoc method
     * @name onKeydown
     * @methodOf data-prep.filter-item-value.controller:FilterValueCtrl
     * @description ESC key down must reset filter item value and ENTER key down must validate
     * @param event Key event
     */
	onKeydown(event) {
		if (event && event.keyCode === 27) {
			this.valueToDisplay = this.filterValue;
		}

		if (event && (event.keyCode === 27 || event.keyCode === 13)) {
			this.onEdit({
				value: this.valueToDisplay,
			});
			event.stopPropagation();
		}
	}

	get valueWithInvisibleChars() {
		return this.TextFormatService.adaptToGridConstraints(this.valueToDisplay);
	}
}
