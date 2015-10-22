(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:TalendTabs
     * @description Tabs directive. This is paired with tabs item directive.
     * @restrict E
     * @usage
     <talend-tabs tab="selectedTab">
         <talend-tabs-item tab-title="tab 1 title">
            Content tab 1
         </talend-tabs-item>
         <talend-tabs-item tab-title="tab 2 title" default="true">
            Content tab 2
         </talend-tabs-item>
         <talend-tabs-item tab-title="tab 3 title">
            Content tab 3
         </talend-tabs-item>
     </talend-tabs>
     * @param {string} selectedTab The tab to select
     * @param {string} title The tab title to display
     * @param {boolean} default The default tab to select
     * @param {string} selectedTab The tab title to be updated
     */
    function TalendTabs($timeout) {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'components/widgets/tabs/tabs.html',
            controller: 'TalendTabsCtrl',
            controllerAs: 'tabsCtrl',
            bindToController: true,
            scope: {
                tab: '=',
                actionOnClick: '&'
            },
            link: function (scope, iElement, iAttrs, ctrl) {

                $timeout(function(){
                    angular.element('.tabs-item').on('click', function(){
                        ctrl.actionOnClick();
                    });
                });

                scope.$watch(
                    function () {
                        return ctrl.tab;
                    },
                    function (value) {
                        if (angular.isDefined(value)) {
                            ctrl.setSelectedTab(value);
                        }
                    }
                );
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendTabs', TalendTabs);
})();