/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import TypeaheadCtrl from './typeahead-controller';

/**
 * @ngdoc directive
 * @name talend.widget.directive:Typeahead
 * @description This directive create an input with a dropdown element.
 * @param {function} search function called when input changes
 */
export default function Typeahead($timeout) {
    'ngInject';

    return {
        restrict: 'E',
        transclude: true,
        templateUrl: 'app/components/widgets/typeahead/typeahead.html',
        scope: {
            search: '&',
            placeholder: '@'
        },
        bindToController: true,
        controller: TypeaheadCtrl,
        controllerAs: 'typeaheadCtrl',
        link: {
            post: (scope, iElement, iAttrs, ctrl) => {
                const body = angular.element('body').eq(0);
                const input = iElement.find('.input-search');

                function hideResults() {
                    $timeout(() => ctrl.hideResults());
                }

                input.keydown((event) => {
                    if (event.keyCode === 27) {
                        ctrl.hideResults();
                        scope.$digest();
                    }
                });
                input.click((event) => event.stopPropagation());
                body.click(hideResults);

                iElement.on('$destroy', () => scope.$destroy());
                scope.$on('$destroy', () => body.off('click', hideResults));
            }
        }
    };
}