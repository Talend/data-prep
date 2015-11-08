(function () {
    'use strict';

    /**
     * @ngdoc property
     * @name shownModalsInnerElements
     * @propertyOf talend.widget.directive:TalendModal
     * @description [PRIVATE] Array of all modal inner element that is visible
     * @type {object[]}
     */
    var shownModalsInnerElements = [];

    /**
     * @ngdoc method
     * @name registerShownElement
     * @methodOf talend.widget.directive:TalendModal
     * @param {object} innerElement - the modal to register
     * @description [PRIVATE] Add an element to the list of visible modals
     */
    var registerShownElement = function (innerElement) {
        shownModalsInnerElements.push(innerElement);
    };

    /**
     * @ngdoc method
     * @name deregisterShownElement
     * @methodOf talend.widget.directive:TalendModal
     * @param {object} innerElement - the modal to deregister
     * @description [PRIVATE] Remove an element from list of visible modals
     */
    var deregisterShownElement = function (innerElement) {
        var index = shownModalsInnerElements.indexOf(innerElement);
        if (index > -1) {
            shownModalsInnerElements = shownModalsInnerElements.slice(0, index);
        }
    };

    /**
     * @ngdoc method
     * @name getLastRegisteredInnerElement
     * @methodOf talend.widget.directive:TalendModal
     * @description [PRIVATE] Return last visible modal (the one the most in front on the screen)
     * @returns {object} The last visible modal
     */
    var getLastRegisteredInnerElement = function () {
        if (shownModalsInnerElements.length) {
            return shownModalsInnerElements[shownModalsInnerElements.length - 1];
        }
    };

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
                     disable-enter="true"
                     on-close="homeCtrl.closeHandler()"
                     close-button="true">
                     Modal content
     </talend-modal>

     <talend-modal   fullscreen="true"
                     state="homeCtrl.dataModal"
                     disable-enter="false"
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
     * @param {boolean} disable-enter Flag that disable the validation on ENTER press. The validation is a click on the button with `modal-primary-button` class
     * @param {function} before-close Optional callback that is called before user close event. If the callback return true, the modal is closed.
     * @param {function} on-close Optional close callback which is called at each modal close
     * @param {class} no-focus Prevent the targeted input to be focused on modal show
     * @param {class} talend-modal-close Hide the modal on click
     * @param {class} modal-header FULLSCREEN mode only.<br/>The modal header
     * @param {class} modal-body FULLSCREEN mode only.<br/>The modal body
     */
    function TalendModal($timeout) {
        return {
            restrict: 'EA',
            transclude: true,
            templateUrl: 'components/widgets/modal/modal.html',
            scope: {
                state: '=',
                closeButton: '=',
                fullscreen: '=',
                disableEnter: '=',
                beforeClose: '&',
                onClose: '&'
            },
            bindToController: true,
            controllerAs: 'talendModalCtrl',
            controller: function () {},
            link: {
                post: function (scope, iElement, iAttrs, ctrl) {
                    var body = angular.element('body').eq(0);
                    var innerElement = iElement.find('.modal-inner').eq(0);
                    var primaryButton = iElement.find('.modal-primary-button').eq(0);
                    var hasBeforeEachFn = iAttrs.beforeClose !== undefined;

                    /**
                     * @ngdoc method
                     * @name hideModal
                     * @methodOf talend.widget.directive:TalendModal
                     * @description [PRIVATE] Hide modal action
                     */
                    var hideModal = function () {
                        $timeout(function () {
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
                    var deregisterAndFocusOnLastModal = function (innerElement) {
                        deregisterShownElement(innerElement);
                        var mostAdvancedModal = getLastRegisteredInnerElement();
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
                    var attachListeners = function () {
                        // Close action on all 'talend-modal-close' elements
                        iElement.find('.talend-modal-close').on('click', hideModal);

                        // stop propagation on click on inner modal to prevent modal close
                        innerElement.on('click', function (e) {
                            e.stopPropagation();
                        });
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
                    var attachKeyMap = function () {
                        innerElement.bind('keydown', function (e) {

                            // hide modal on 'ESC' keydown
                            if (e.keyCode === 27) {
                                hideModal();
                            }

                            // click on primary button on 'ENTER' keydown
                            else if (e.keyCode === 13 && !ctrl.disableEnter && primaryButton.length) {
                                primaryButton.click();
                            }
                        });
                    };

                    /**
                     * @ngdoc method
                     * @name attachModalToBody
                     * @methodOf talend.widget.directive:TalendModal
                     * @description [PRIVATE] Attach element to body directly to avoid parent styling
                     */
                    var attachModalToBody = function () {
                        iElement.detach();
                        body.append(iElement);
                    };

                    attachListeners();
                    attachKeyMap();
                    attachModalToBody();

                    // remove element on destroy
                    scope.$on('$destroy', function () {
                        deregisterAndFocusOnLastModal(innerElement);
                        iElement.remove();
                    });

                    //enable/disable scroll on main body depending on modal display
                    //on show : modal focus
                    //on close : close callback and focus on last opened modal
                    scope.$watch(function () {
                        return ctrl.state;
                    }, function (newValue, oldValue) {
                        if (newValue) {
                            //register modal in shown modal list and focus on inner element
                            body.addClass('modal-open');
                            registerShownElement(innerElement);
                            innerElement.focus();

                            setTimeout(function () {
                                //focus on first input (ignore first because it's the state checkbox)
                                var inputs = iElement.find('input:not(".no-focus")').eq(1);
                                if (inputs.length) {
                                    inputs.focus();
                                    inputs.select();
                                }
                            }, 200);

                        } else if (oldValue) {
                            ctrl.onClose();
                            deregisterAndFocusOnLastModal(innerElement);
                        }
                    });
                }
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendModal', TalendModal);
})();
