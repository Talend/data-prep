/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './slidable.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendSlidable
 * @description Slidable widget.<br/>
 * If the slidable is resizable, the widget keeps the last width in localstorage so the element is resized in creation<br/>
 * To customize the slidable width, use the sass mixin to set properly all the flexbox element size
 <pre>

    .my-slidable-element {
        &#64;include slidable-size(300px, 20px);
    }

 </pre>
 * Parameters :
 * <ul>
 *     <li>First param (300px) is the panel width</li>
 *     <li>Second param (20px) is action bar (used to show/hide) width</li>
 * </ul>
 * @restrict E
 * @usage
 <talend-slidable
     visible="visible"
     side="right"
     control-bar="false"
     resizable="custom-namespace">
 Content
 </talend-slidable>
 * @param {boolean} visible Controle the slidable visibility
 * @param {string} side `left` (default) | right. This defines the action bar and the resize bar position
 * @param {string} resizable Pass unique ID that will be used to store custom size in local storage (key = {data-prep-' + resizable_namespace + '-width}).<br/>
 * @param {string} controlBar If 'false', the action bar will not been displayed.<br/>
 * Resize feature is disabled by default and enabled if the attribute si set
 */
export default function TalendSlidable($window) {
	'ngInject';

	return {
		restrict: 'E',
		transclude: true,
		templateUrl: template,
		scope: {
			side: '@',
			visible: '=',
			resizable: '@',
			controlBar: '@',
		},
		bindToController: true,
		controllerAs: 'slidableCtrl',
		controller() {
			const vm = this;

			vm.cssClass = 'slide-' + vm.side;
			vm.actionCssClass = vm.side;
			vm.hasControlButton = vm.controlBar !== 'false';

			vm.toggle = function () {
				vm.visible = !vm.visible;
			};
		},

		link(scope, iElement, iAttrs, ctrl) {
			iElement.addClass('slidable');
			iElement.addClass('slide-' + (ctrl.side ? ctrl.side : 'left'));

			if (ctrl.resizable) {
				const localStorageWidthKey = 'org.talend.dataprep.' + ctrl.resizable + '.width';
				let width = $window.localStorage.getItem(localStorageWidthKey);
				if (width) {
					iElement.css('flex', '0 0 ' + width);
				}

				iElement.resizable({
					handles: ctrl.side === 'right' ? 'w' : 'e',
					start() {
						iElement.addClass('no-transition');
					},

					stop(event, ui) {
						iElement.removeClass('no-transition');
						width = ui.size.width + 'px';
						$window.localStorage.setItem(localStorageWidthKey, width);
					},

					resize(event, ui) {
						iElement.css('left', 'auto');
						iElement.css('right', 'auto');
						iElement.css('flex', '0 0 ' + ui.size.width + 'px');
					},
				});
			}

			scope.$watch(
                () => ctrl.visible,
                (visible) => {
	if (visible) {
		iElement.removeClass('slide-hide');
	}
	else {
		iElement.addClass('slide-hide');
	}
}
            );
		},
	};
}
