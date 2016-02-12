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
 * @param {boolean} default Is the default tab to select
 * @param {function} onInit The calback on component init
 */
export default function TalendTabsItem() {
	return {
		restrict: 'E',
		transclude: true,
		template: '<ng-transclude ng-if="tabsItemCtrl.active"></ng-transclude>',
		require: '^^talendTabs',
		scope: {
			tabTitle: '@',
			'default': '=',
			onInit: '&'
		},
		bindToController: true,
		controller: function () {
		},
		controllerAs: 'tabsItemCtrl',
		link: function (scope, iElement, iAttrs, tabsCtrl) {
			var ctrl = scope.tabsItemCtrl;

			//register itself
			tabsCtrl.register(ctrl);
			if (ctrl.default) {
				tabsCtrl.select(ctrl);
			}

			//unregister itself on destroy
			scope.$on('$destroy', function () {
				tabsCtrl.unregister(ctrl);
			});

			ctrl.onInit();
		}
	};
}