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
 * @name talend.widget.controller:TalendConfirmCtrl
 * @description Talend confirm controller.<br/>
 * Watchers :
 * <ul>
 *     <li>On modal hide, the controller reject the confirm promise if the valid button is not clicked</li>
 * </ul>
 * @requires talend.widget.service:TalendConfirmService
 */
export default function TalendConfirmCtrl($scope, TalendConfirmService) {
	'ngInject';

	const vm = this;

    /**
     * @ngdoc property
     * @name modalState
     * @propertyOf talend.widget.controller:TalendConfirmCtrl
     * @description Flag that controls the display of the confirm modal
     */
	vm.modalState = true;

    /**
     * @ngdoc property
     * @name buttonClicked
     * @propertyOf talend.widget.controller:TalendConfirmCtrl
     * @description Flag that keep the state of the validation button click
     */
	vm.buttonClicked = false;

    /**
     * Watch the modal state to reject confirm promise on modal dismiss
     */
	$scope.$watch(
        function () {
	return vm.modalState;
},

        function (newValue) {
	if (!newValue && !vm.buttonClicked) {
		TalendConfirmService.reject('dismiss');
	}
}
    );

    /**
     * @ngdoc method
     * @name valid
     * @methodOf talend.widget.controller:TalendConfirmCtrl
     * @description Validate the confirm choice and resolve the confirm promise
     */
	vm.valid = function () {
		vm.buttonClicked = true;
		TalendConfirmService.resolve();
	};

    /**
     * @ngdoc method
     * @name cancel
     * @methodOf talend.widget.controller:TalendConfirmCtrl
     * @description Cancel the confirm choice and reject the confirm promise
     */
	vm.cancel = function () {
		vm.buttonClicked = true;
		TalendConfirmService.reject(null);
	};
}
