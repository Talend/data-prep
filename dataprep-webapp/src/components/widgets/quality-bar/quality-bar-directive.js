(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.quality-bar.directive:QualityBar
     * @description Quality bar tooltip
     * @restrict E
     * @usage
     <quality-bar
             quality="quality"
             enterAnimation="enableEnterAnimation"
             hasMenu="true">
     </talend-tooltip>
     * @param {object} quality {empty: number, invalid: number, valid: number} The quality values
     * @param {string} enterAnimation Do not animate on enter if this flag === 'false'
     * @param {string} hasMenu Do not show the menu if hasMenu === 'false'
     */
    function QualityBar($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/widgets/quality-bar/quality-bar.html',
            scope: {
                enterAnimation: '@',
                quality: '=',
                hasMenu: '='
            },
            transclude: true,
            bindToController: true,
            controller: 'QualityBarCtrl',
            controllerAs: 'qualityBarCtrl',
            link: function(scope, iElement, iAttrs, ctrl) {
                var initializing = true;

                /**
                 * @ngdoc method
                 * @name enableTransition
                 * @methodOf talend.widget.directive:QualityBar
                 * @description [PRIVATE] Enable animation
                 */
                var enableTransition = function enableTransition() {
                    ctrl.blockTransition = false;
                };

                /**
                 * @ngdoc method
                 * @name refreshBarsWithAnimation
                 * @methodOf talend.widget.directive:QualityBar
                 * @description [PRIVATE] Block animation, reset width to 0 and calculate the new width with animation enabling
                 */
                var refreshBarsWithAnimation = function refreshBarsWithAnimation() {
                    ctrl.width = {
                        invalid: 0,
                        empty: 0,
                        valid: 0
                    };

                    enableTransition();

                    $timeout(function() {
                        ctrl.computePercent();
                        ctrl.computeQualityWidth();
                        scope.$digest();
                    }, 300, false);
                };

                /**
                 * @ngdoc method
                 * @name refreshBars
                 * @methodOf talend.widget.directive:QualityBar
                 * @description [PRIVATE] Refresh the quality bars infos (percent and width)
                 * When enterAnimation === 'false', we do NOT animate on first render
                 */
                var refreshBars = function refreshBars() {
                    //Do NOT animate on first values and enterAnimation is false
                    if(initializing && ctrl.enterAnimation === 'false') {
                        initializing = false;
                        ctrl.blockTransition = true;
                        ctrl.computePercent();
                        ctrl.computeQualityWidth();
                    }
                    else {
                        refreshBarsWithAnimation();
                    }
                };

                scope.$watch(ctrl.hashQuality, refreshBars);

                if(ctrl.hasMenu){
                    $timeout(function(){
                        var validMenuContent = iElement.find('.valid-menu-item');
                        var emptyMenuContent = iElement.find('.empty-menu-items');
                        var invalidMenuContent = iElement.find('.invalid-menu-items');

                        iElement.find('.valid-partition .quality-bar-menu').eq(0).append(validMenuContent);
                        iElement.find('.empty-partition .quality-bar-menu').eq(0).append(emptyMenuContent);
                        iElement.find('.invalid-partition .quality-bar-menu').eq(0).append(invalidMenuContent);
                    }, 300, false);
                }
            }
        };
    }

    angular.module('talend.widget')
        .directive('qualityBar', QualityBar);
})();