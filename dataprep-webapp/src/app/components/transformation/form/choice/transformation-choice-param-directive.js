/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformChoiceParam
 * @description This directive display a transformation choice parameter form
 * @restrict E
 * @usage <transform-choice-param parameter="parameter"></transform-choice-params>
 * @param {object} parameter The transformation choices parameter
 */
export default function TransformChoiceParam($rootScope, $compile) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: 'app/components/transformation/form/choice/transformation-choice-param.html',
        scope: {
            parameter: '='
        },
        bindToController: true,
        controllerAs: 'choiceParamCtrl',
        controller: 'TransformChoiceParamCtrl',
        link: function (scope, iElement, iAttrs, ctrl) {
            _.chain(ctrl.parameter.configuration.values)
                .filter(function (optionValue) {
                    return optionValue.parameters && optionValue.parameters.length;
                })
                .forEach(function (optionValue) {
                    var isolatedScope = $rootScope.$new(true);
                    isolatedScope.parameter = ctrl.parameter;
                    isolatedScope.optionValue = optionValue;

                    var template = '<transform-params ' +
                        'parameters="optionValue.parameters" ' +
                        'ng-if="parameter.value === optionValue.value" ' +
                        '></transform-params>';
                    $compile(template)(isolatedScope, function (cloned) {
                        iElement.append(cloned);
                    });
                })
                .value();
        }
    };
}