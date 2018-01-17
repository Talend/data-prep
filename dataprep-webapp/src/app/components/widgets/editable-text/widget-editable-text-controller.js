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
 * @name talend.widgets.controller:TalendEditableTextCtrl
 * @description Editable Text controller
 */
export default class TalendEditableTextCtrl {

	constructor($scope) {
		'ngInject';
		this.$scope = $scope;
	}

	$onInit() {
		this.$scope.$watch(() => this.text, this.reset.bind(this));
	}

    /**
     * @ngdoc method
     * @name reset
     * @methodOf talend.widgets.controller:TalendEditableTextCtrl
     * @description Set the edition text with the original value
     */
	reset() {
		this.editionText = this.text;
	}

    /**
     * @ngdoc method
     * @name edit
     * @methodOf talend.widgets.controller:TalendEditableTextCtrl
     * @description Reset the edition text and set edition mode flag
     */
	edit() {
		this.reset();
		this.editionMode = true;
	}

    /**
     * @ngdoc method
     * @name validate
     * @methodOf talend.widgets.controller:TalendEditableTextCtrl
     * @description Execute the validation callback
     * when value has changed
     * then switch to non the edition mode
     */
	validate() {
		if (angular.isUndefined(this.validateOnlyOnChange) || this.editionText !== this.text) {
			this.onValidate({ text: this.editionText });
		}

		this.editionMode = false;
	}

    /**
     * @ngdoc method
     * @name cancel
     * @methodOf talend.widgets.controller:TalendEditableTextCtrl
     * @description Execute the cancel callback and set the edition mode to false
     */
	cancel() {
		this.onCancel({ text: this.editionText });
		this.editionMode = false;
	}
}
