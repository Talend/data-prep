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
export default function TalendTabs() {
    return {
        restrict: 'E',
        transclude: true,
        templateUrl: 'app/components/widgets/tabs/tabs.html',
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