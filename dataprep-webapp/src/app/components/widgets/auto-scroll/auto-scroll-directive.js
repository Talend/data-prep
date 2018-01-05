/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.auto-scroll.directive:AutoScroll
 * @description This directive enables to scroll top and bottom while dragging
 * @restrict A
 * @usage
 * <div auto-scroll
 *  bottom-delta="bottomDelta"
 *  dnd-position="eventClientY"
 *  scroll-step="scrollStep"
 *  top-delta="topDelta"
 *  while-dragging="isDragStart">
 * </div>
 * @param {Number}  bottomDelta the delta before the mouse reaches the bottom of the container
 * @param {Number}  dndPosition the Y position of the mouse
 * @param {Number}  scrollStep the number of pixels for a scroll step
 * @param {Number}  topDelta the delta before the mouse reaches the top of the container
 * @param {Boolean} whileDragging when true launches the periodic loop to scroll the element
 */
export default function AutoScroll($interval) {
	'ngInject';
	return {
		restrict: 'A',
		scope: true, // impossible to have an isolated scope because the directive as-sortable scope is not isolated
		link: (scope, iElement, attrs) => {
			let interval;
			let elementPositions;

			const scrollStep = +attrs.scrollStep;
			const topDelta = +attrs.topDelta;
			const bottomDelta = +attrs.bottomDelta;
			const updateScroll = () => {
				const dndPosition = +scope.$eval(attrs.dndPosition);
				const above = dndPosition < elementPositions.top + topDelta;
				const below = dndPosition > elementPositions.bottom - bottomDelta;
				if (below || above) {
					const currentScroll = iElement.scrollTop();
					iElement.scrollTop(below ? currentScroll + scrollStep : currentScroll - scrollStep);
				}
			};

			scope.$watch(() => scope.$eval(attrs.whileDragging), (newVal) => {
				if (newVal) {
					elementPositions = iElement[0].getBoundingClientRect();
					interval = $interval(updateScroll, 100);
				}
				else {
					$interval.cancel(interval);
					interval = null;
				}
			});
		},
	};
}
