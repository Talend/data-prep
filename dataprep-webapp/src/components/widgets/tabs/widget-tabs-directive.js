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
     * @param {number} tab The selected tab index
     * @param {function} onTabChange The callback when the selected tab change
     */
    function TalendTabs() {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'components/widgets/tabs/tabs.html',
            controller: 'TalendTabsCtrl',
            controllerAs: 'tabsCtrl',
            bindToController: true,
            scope: {
                tab: '=',
                onTabChange: '&'
            },
            link: function (scope, iElement, iAttrs, ctrl) {

                scope.$watch(
                    function () {
                        return ctrl.tab;
                    },
                    ctrl.setSelectedTab
                );
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendTabs', TalendTabs);
})();