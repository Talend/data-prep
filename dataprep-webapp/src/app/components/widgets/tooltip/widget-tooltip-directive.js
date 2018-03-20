/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './tooltip.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendTooltip
 * @description Configurable tooltip widget<br/>
 * The tooltip state is blocked when the mouse is over it or the focus is on a tooltip inner element.
 * The requested state is applied when the mouse leave the tooptip and the focus is not on it.<br/>
 * Watchers :
 * <ul>
 *     <li>position : on position change, the tooltip position is recalculated</li>
 * </ul>
 * @restrict E
 * @usage <talend-tooltip position="position" requested-state="showTooltip"> </talend-tooltip>
 * @param {object} position {x: number, y: number} The position where to display the tooltip
 * @param {boolean} requestedState Show/hide tooltip if not blocked
 */
export default function TalendTooltip($window, $document) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		transclude: true,
		scope: {
			position: '=',
			requestedState: '=',
		},
		bindToController: true,
		controller: 'TalendTooltipCtrl',
		controllerAs: 'talendTooltipCtrl',
		link(scope, iElement, iAttrs, ctrl) {
			let hasFocus;
			let isOver;
            /**
             * @ngdoc method
             * @name processBlockUnblock
             * @methodOf talend.widget.directive:TalendTooltip
             * @description [PRIVATE] Block (mouse over || focus in) or unblock (mouse leave && focus out) the visibility change
             */
			const processBlockUnblock = function () {
				if (hasFocus || isOver) {
					ctrl.blockState();
				}
				else {
					ctrl.unblockState();
					scope.$digest();
				}
			};

			iElement.hover(
                function () {
	isOver = true;
	processBlockUnblock();
},

                function () {
	isOver = false;
	processBlockUnblock();
});

			iElement.focusin(function () {
				hasFocus = true;
				processBlockUnblock();
			});

			iElement.focusout(function () {
				hasFocus = false;
				processBlockUnblock();
			});

            /**
             * @ngdoc method
             * @name calculateHorizontalPosition
             * @methodOf talend.widget.directive:TalendTooltip
             * @param {object} position {{x: Number}} The requested position
             * @description [PRIVATE] Calculate left/right position.<br/>
             * If the place to display is in the first half of the window, the tooltip is displayed on the right
             * Otherwise, the tooltip is displayed on the left
             * @returns {object} {left: string, right: string} The calculated left and right css
             */
			const calculateHorizontalPosition = function (position) {
				const windowWidth = $window.innerWidth || $document.documentElement.clientWidth || $document.body.clientWidth;

				if (position.x < windowWidth / 2) {
					return {
						left: position.x + 'px',
						right: 'auto',
					};
				}
				else {
					return {
						left: 'auto',
						right: (windowWidth - position.x) + 'px',
					};
				}
			};

            /**
             * Calculate top/bottom position.
             * If the place to display is in the first half of the window, the tooltip is displayed on the bottom
             * Otherwise, the tooltip is displayed on the top
             * @param position - {{y: Number}} the requested position
             * @returns {{top: String, bottom: String}}
             */
            /**
             * @ngdoc method
             * @name calculateVerticalPosition
             * @methodOf talend.widget.directive:TalendTooltip
             * @param {object} position {{y: Number}} The requested position
             * @description [PRIVATE] Calculate top/bottom position.<br/>
             * If the place to display is in the first half of the window, the tooltip is displayed on the bottom
             * Otherwise, the tooltip is displayed on the top
             * @returns {object} {{top: String, bottom: String}} The calculated top and bottom css
             */
			const calculateVerticalPosition = function (position) {
				const windowHeight = $window.innerHeight || $document.documentElement.clientHeight || $document.body.clientHeight;

				if (position.y < windowHeight / 2) {
					return {
						top: position.y,
						bottom: 'auto',
					};
				}
				else {
					return {
						top: 'auto',
						bottom: (windowHeight - position.y) + 'px',
					};
				}
			};

            /**
             * Calculate css and update tooltip on requested position change
             */
			scope.$watch(
                function () {
	return ctrl.position;
},

                function (position) {
	if (position) {
		const horizontalPosition = calculateHorizontalPosition(position);
		const verticalPosition = calculateVerticalPosition(position);

		ctrl.updatePosition(horizontalPosition, verticalPosition);
	}
}
            );
		},
	};
}
