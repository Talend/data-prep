(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.quality-bar.directive:lookupQualityBar
     * @description Quality bar tooltip
     * @restrict E
     * @usage
     <talend-quality-bar
             quality="quality"
             column="column"
             enterAnimation="enableEnterAnimation">
     </talend-tooltip>
     * @param {object} quality {empty: number, invalid: number, valid: number} The quality values
     * @param {object} column The quality target column
     * @param {string} enterAnimation Do not animate on enter if this flag === 'false'
     */
    function LookupQualityBar($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/lookup/lookup-datagrid/quality-bar/lookup-quality-bar.html',
            replace: true,
            scope: {
                quality: '=',
                column: '=',
                enterAnimation: '@'
            },
            bindToController: true,
            controller: 'LookupQualityBarCtrl',
            controllerAs: 'lookupQualityBarCtrl',
            link: function(scope, iElement, iAttrs, ctrl) {
                var initializing = true;

                /**
                 * @ngdoc method
                 * @name enableTransition
                 * @methodOf data-prep.quality-bar.directive:QualityBar
                 * @description [PRIVATE] Enable animation
                 */
                var enableTransition = function enableTransition() {
                    ctrl.blockTransition = false;
                };

                /**
                 * @ngdoc method
                 * @name refreshBarsWithAnimation
                 * @methodOf data-prep.quality-bar.directive:QualityBar
                 * @description [PRIVATE] Block animation, reset width to 0 and calculate the new width with animation enabling
                 */
                var refreshBarsWithAnimation = function refreshBarsWithAnimation() {
                    ctrl.blockTransition = true;
                    ctrl.width = {
                        invalid: 0,
                        empty: 0,
                        valid: 0
                    };

                    $timeout(enableTransition);

                    $timeout(function() {
                        ctrl.computePercent();
                        ctrl.computeQualityWidth();
                    }, 300);
                };

                /**
                 * @ngdoc method
                 * @name refreshBars
                 * @methodOf data-prep.quality-bar.directive:QualityBar
                 * @description [PRIVATE] Refresh the quality bars infos (percent and width)
                 * When enterAnimation === 'false', we do NOT animate on first render
                 */
                var refreshBars = function refreshBars() {
                    //Do NOT animate on first values and enterAnimation is false
                    if(initializing && ctrl.enterAnimation === 'false') {
                        initializing = false;
                        ctrl.computePercent();
                        ctrl.computeQualityWidth();
                    }
                    else {
                        refreshBarsWithAnimation();
                    }
                };

                scope.$watch(ctrl.hashQuality, refreshBars);
            }
        };
    }

    angular.module('data-prep.lookup-quality-bar')
        .directive('lookupQualityBar', LookupQualityBar);
})();