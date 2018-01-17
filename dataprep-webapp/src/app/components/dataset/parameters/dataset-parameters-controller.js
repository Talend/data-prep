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
 * @name data-prep.dataset-parameters.controller:DatasetParametersCtrl
 * @description Dataset parameters controller
 */
export default function DatasetParametersCtrl() {
	const vm = this;

    /**
     * @ngdoc method
     * @name separatorIsInList
     * @methodOf data-prep.dataset-parameters.controller:DatasetParametersCtrl
     * @description Check if the current separator is in the list of separators
     */
	vm.separatorIsInList = function separatorIsInList() {
		return vm.parameters.separator &&
            _.find(vm.configuration.separators, { value: vm.parameters.separator });
	};

    /**
     * @ngdoc method
     * @name validate
     * @methodOf data-prep.dataset-parameters.controller:DatasetParametersCtrl
     * @description Call the parameter change validation callback
     */
	vm.validate = function validate() {
		vm.onParametersChange({
			dataset: vm.dataset,
			parameters: vm.parameters,
		});
	};
}
