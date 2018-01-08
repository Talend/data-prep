/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name talend.widget.controller:TalendTooltipCtrl
 * @description Tooltip directive
 */
export default function TalendTooltipCtrl($scope) {
	'ngInject';

	const vm = this;
	let blocked = false;
	vm.innerState = vm.requestedState;

    /**
     * @ngdoc method
     * @name blockState
     * @methodOf talend.widget.controller:TalendTooltipCtrl
     * @description Block the display
     */
	vm.blockState = function blockState() {
		blocked = true;
	};

    /**
     * @ngdoc method
     * @name unblockState
     * @methodOf talend.widget.controller:TalendTooltipCtrl
     * @description Unblock the display
     */
	vm.unblockState = function unblockState() {
		blocked = false;
		vm.innerState = vm.requestedState;
	};

    /**
     * @ngdoc method
     * @name updatePosition
     * @methodOf talend.widget.controller:TalendTooltipCtrl
     * @param {object} horizontalPosition - {left: (number | string); right: (number | string)}
     * @param {object} verticalPosition - {top: (number | string); bottom: (number | string)}
     * @description Change the position of the tooltip
     */
	vm.updatePosition = function updatePosition(horizontalPosition, verticalPosition) {
		vm.style = {
			left: horizontalPosition.left,
			right: horizontalPosition.right,
			top: verticalPosition.top,
			bottom: verticalPosition.bottom,
		};
	};

    /**
     * Update visibility state if not blocked when requested state change
     */
	$scope.$watch(
        () => {
	return vm.requestedState;
},

        (newValue) => {
	if (!blocked) {
		vm.innerState = newValue;
	}
}
    );
}
