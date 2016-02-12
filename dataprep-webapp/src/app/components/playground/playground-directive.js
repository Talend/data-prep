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
 * @name data-prep.playground.directive:Playground
 * @description This directive create the playground.
 * @restrict E
 */
export default function Playground() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/playground/playground.html',
        bindToController: true,
        controllerAs: 'playgroundCtrl',
        controller: 'PlaygroundCtrl'
    };
}