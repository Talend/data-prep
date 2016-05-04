/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.transformation-form.controller:TransformDateParamCtrl
 * @description Transformation date parameter controller.
 */
export default function TransformDateParamCtrl() {
    var vm = this;

    /**
     * @ngdoc method
     * @name initParamValues
     * @methodOf data-prep.transformation-form.controller:TransformDateParamCtrl
     * @description [PRIVATE] Init date parameter values to default
     */
    var initParamValue = function () {
        var param = vm.parameter;
        if (!param.value) {
            param.value = param.default;
        }
        if (!param.value){
            param.value = moment(new Date()).format('DD/MM/YYYY HH:mm:ss');
        }
    };

    initParamValue();
}