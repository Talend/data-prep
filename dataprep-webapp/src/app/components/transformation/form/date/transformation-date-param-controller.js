/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import moment from 'moment';

/**
 * @ngdoc controller
 * @name data-prep.transformation-form.controller:TransformDateParamCtrl
 * @description Transformation date parameter controller.
 */
export default function TransformDateParamCtrl() {
	const vm = this;

    /**
     * @ngdoc method
     * @name initParamValues
     * @methodOf data-prep.transformation-form.controller:TransformDateParamCtrl
     * @description [PRIVATE] Init date parameter values to default
     */
	function initParamValue() {
		const param = vm.parameter;
		if (!param.value) {
			param.value = param.default || moment(new Date()).format('YYYY-MM-DD HH:mm');
		}
	}

	initParamValue();
}
