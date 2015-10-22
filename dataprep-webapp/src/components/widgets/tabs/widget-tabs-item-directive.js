(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:TalendTabs
     * @description Tabs directive. This MUST be used with tabs directive. It register itself to the tabs component.
     * @restrict E
     * @usage
     <talend-tabs>
        <talend-tabs-item tab-title="tab 1 title">
            Content tab 1
        </talend-tabs-item>
        <talend-tabs-item tab-title="tab 2 Title" default="true">
            Content tab 2
        </talend-tabs-item>
        <talend-tabs-item tab-title="tab 3 Title">
            Content tab 3
        </talend-tabs-item>
     </talend-tabs>
     * @param {string} tabTitle The tab Title to display
     * @param {boolean} default The default tab to select
     */
    function TalendTabsItem() {
        return {
            restrict: 'E',
            replace: true,
            transclude : true,
            templateUrl: 'components/widgets/tabs/tabs-item.html',
            require: '^^talendTabs',
            scope: {
                tabTitle: '@',
                'default': '=',
                'actionOnInit': '&'
            },
            bindToController: true,
            controller: function() {},
            controllerAs: 'tabsItemCtrl',
            link: function(scope, iElement, iAttrs, tabsCtrl) {
                var ctrl = scope.tabsItemCtrl;

                //register itself
                tabsCtrl.register(ctrl);
                if(ctrl.default) {
                    tabsCtrl.select(ctrl);
                }

                //unregister itself on destroy
                scope.$on('$destroy', function() {
                    tabsCtrl.unregister(ctrl);
                });

                ctrl.actionOnInit();
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendTabsItem', TalendTabsItem);
})();