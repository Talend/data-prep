/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './modal.html';

/**
 * @ngdoc property
 * @name shownModalsInnerElements
 * @propertyOf talend.widget.directive:TalendModal
 * @description Array of all modal inner element that is visible
 * @type {object[]}
 */
let shownModalsInnerElements = [];

/**
 * @ngdoc method
 * @name registerShownElement
 * @methodOf talend.widget.directive:TalendModal
 * @param {object} innerElement - the modal to register
 * @description Add an element to the list of visible modals
 */
function registerShownElement(innerElement) {
	shownModalsInnerElements.push(innerElement);
}

/**
 * @ngdoc method
 * @name deregisterShownElement
 * @methodOf talend.widget.directive:TalendModal
 * @param {object} innerElement - the modal to deregister
 * @description Remove an element from list of visible modals
 */
function deregisterShownElement(innerElement) {
	const index = shownModalsInnerElements.indexOf(innerElement);
	if (index > -1) {
		shownModalsInnerElements = shownModalsInnerElements.slice(0, index);
	}
}

/**
 * @ngdoc method
 * @name getLastRegisteredInnerElement
 * @methodOf talend.widget.directive:TalendModal
 * @description [PRIVATE] Return last visible modal (the one the most in front on the screen)
 * @returns {object} The last visible modal
 */
function getLastRegisteredInnerElement() {
	if (shownModalsInnerElements.length) {
		return shownModalsInnerElements[shownModalsInnerElements.length - 1];
	}
}

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendModal
 * @description This directive create a Modal window with 2 modes : normal (default) | fullscreen.<br/>
 * The first input is focused on modal display (if not disabled).<br/>
 * Multiple modals can be opened. The order determines the one that is displayed (the last is the most visible).
 * When a modal is displayed, it has the focus to enable the keymap.<br/>
 * Key action :
 * <ul>
 *     <li>ENTER : click on the `modal-primary-button` button</li>
 *     <li>ESC : hide the modal</li>
 * </ul>
 * Watchers :
 * <ul>
 *     <li>on show : focus on the first input (not `no-focus`) or on the modal itself</li>
 *     <li>on close : hide the modal, execute the close callback, put the focus on the last displayed modal</li>
 * </ul>
 * @restrict E
 * @usage
 <talend-modal   fullscreen="false"
 state="homeCtrl.dataModalSmall"
 on-close="homeCtrl.closeHandler()"
 close-button="true">
 Modal content
 </talend-modal>

 <talend-modal   fullscreen="true"
 state="homeCtrl.dataModal"
 on-close="homeCtrl.closeHandler()"
 close-button="true">
 <div class="modal-header">
 <ul>
 <li>header 1</li>
 <li>header 2</li>
 </ul>
 </div>

 <div class="modal-body">
 Body content
 </div>
 </talend-modal>

 * @param {boolean} state Flag that represents the modal display state
 * @param {boolean} close-button Display a close button on the top right corner
 * @param {boolean} fullscreen Determine the modal mode. Default `false`
 * @param {function} before-close Optional callback that is called before user close event. If the callback return true, the modal is closed.
 * @param {function} on-close Optional close callback which is called at each modal close
 * @param {boolean} disableCloseOnBackgroundClick enables/disables the user to hide the modal on background click/Escape hit
 *
 * @param {class} no-focus Prevent the targeted input to be focused on modal show
 * @param {class} talend-modal-close Hide the modal on click
 * @param {class} modal-header FULLSCREEN mode only.<br/>The modal header
 * @param {class} modal-body FULLSCREEN mode only.<br/>The modal body
 */
export default function TalendModal($timeout) {
	'ngInject';

	return {
		restrict: 'EA',
		transclude: true,
		templateUrl: template,
		scope: {
			state: '=',
			closeButton: '=',
			fullscreen: '=',
			beforeClose: '&',
			onClose: '&',
			disableCloseOnBackgroundClick: '=',
		},
		bindToController: true,
		controllerAs: 'talendModalCtrl',
		controller() {
		},

		link: {
			post(scope, iElement, iAttrs, ctrl) {
				const body = angular.element('body').eq(0);
				const innerElement = iElement.find('.modal-inner').eq(0);
				const hasBeforeEachFn = angular.isDefined(iAttrs.beforeClose);

				/**
				 * @ngdoc method
				 * @name hideModal
				 * @methodOf talend.widget.directive:TalendModal
				 * @description [PRIVATE] Hide modal action
				 */
				const hideModal = () => {
					$timeout(() => {
						if (hasBeforeEachFn && !ctrl.beforeClose()) {
							return;
						}

						ctrl.state = false;
					});
				};

				/**
				 * @ngdoc method
				 * @name deregisterAndFocusOnLastModal
				 * @methodOf talend.widget.directive:TalendModal
				 * @description [PRIVATE] Deregister modal from list of shown modal and focus on the last shown modal
				 */
				const deregisterAndFocusOnLastModal = (innerElement) => {
					deregisterShownElement(innerElement);
					const mostAdvancedModal = getLastRegisteredInnerElement();
					if (mostAdvancedModal) {
						mostAdvancedModal.focus();
					}
					else {
						body.removeClass('modal-open');
					}
				};

				/**
				 * @ngdoc method
				 * @name attachListeners
				 * @methodOf talend.widget.directive:TalendModal
				 * @description [PRIVATE] Attach click listeners to elements that has `talend-modal-close` class
				 * and stop click propagation in inner modal to avoid a click on the dismiss screen
				 */
				const attachListeners = () => {
					innerElement.on('click', (e) => {
						if (e.target.classList.contains('talend-modal-close')) {
							hideModal();
						}
					});

					// Close action on modal background click
					if (!ctrl.disableCloseOnBackgroundClick) {
						iElement.find('.modal-window').on('click', (e) => {
							if (e.target === e.currentTarget) {
								hideModal();
							}
						});
					}
				};

				/**
				 * @ngdoc method
				 * @name attachKeyMap
				 * @methodOf talend.widget.directive:TalendModal
				 * @description [PRIVATE] Attach ESC and ENTER actions
				 * <ul>
				 *     <li>ESC : dismiss the modal</li>
				 *     <li>ENTER : click on the primary button (with `modal-primary-button` class)</li>
				 * </ul>
				 */
				const attachKeyMap = () => {
					innerElement.bind('keydown', (e) => {
						// hide modal on 'ESC' keydown
						if (e.keyCode === 27 && !ctrl.disableCloseOnBackgroundClick) {
							if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
								innerElement.focus();
							}
							else {
								hideModal();
							}
						}
					});
				};

				/**
				 * @ngdoc method
				 * @name attachModalToBody
				 * @methodOf talend.widget.directive:TalendModal
				 * @description [PRIVATE] Attach element to body directly to avoid parent styling
				 */
				const attachModalToBody = () => {
					iElement.detach();
					body.append(iElement);
				};

				attachListeners();
				attachKeyMap();
				attachModalToBody();

				// remove element on destroy
				scope.$on('$destroy', () => {
					deregisterAndFocusOnLastModal(innerElement);
					iElement.remove();
				});

				// enable/disable scroll on main body depending on modal display
				// on show : modal focus
				// on close : close callback and focus on last opened modal
				scope.$watch(() => ctrl.state,
					(newValue, oldValue) => {
						if (newValue) {
							// register modal in shown modal list and focus on inner element
							body.addClass('modal-open');
							registerShownElement(innerElement);
							innerElement.focus();

							// focus on first input (ignore first because it's the state checkbox)
							const inputs = innerElement.find('[type="checkbox"], [type="email"], [type="search"], [type="submit"], [type="radio"], [type="text"], select, textarea');
							const focusableInputs = inputs.not('.no-focus');
							if (focusableInputs.length) {
								const input = focusableInputs.eq(0);
								input.focus();
								input.select();
							}
							else {
								// or focus on first primary button
								const btns = innerElement.find('.btn-primary');
								if (btns.length) {
									btns.focus();
								}
							}
						}
						else if (oldValue) {
							ctrl.onClose();
							deregisterAndFocusOnLastModal(innerElement);
						}
					});
			},
		},
	};
}
