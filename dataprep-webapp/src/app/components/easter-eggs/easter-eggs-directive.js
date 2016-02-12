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
 * @name data-prep.easter-eggs.directive:EasterEggs
 * @description DataPrep easter eggs
 * @restrict E
 * @usage <easter-eggs></easter-eggs>
 */
export default function EasterEggs() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/easter-eggs/easter-eggs.html',
        bindToController: true,
        controllerAs: 'easterEggsCtrl',
        controller: 'EasterEggsCtrl'
    };
}