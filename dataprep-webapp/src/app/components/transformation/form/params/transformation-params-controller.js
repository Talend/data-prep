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
 * @name data-prep.transformation-form.controller:TransformParamsCtrl
 * @description Transformation parameters controller.
 */
export default function TransformParamsCtrl() {
	const vm = this;

    /**
     * @ngdoc method
     * @name getParameterType
     * @methodOf data-prep.transformation-form.controller:TransformParamsCtrl
     * @description Return the parameter type to display
     * @param {object} parameter The parameter
     */
	vm.getParameterType = function getParameterType(parameter) {
		const type = parameter.type.toLowerCase();
		switch (type) {
		case 'select':
		case 'cluster':
		case 'date':
		case 'column':
		case 'regex':
		case 'hidden':
			return type;
		default:
			return 'simple';
		}
	};
}
