/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Modal directive', () => {

	let scope;
	let element;
	let createElement;
	let disableElementHide;
	let createFormElement;
	let createNestedElement;
	let createButtonElement;
	let createBeforeCloseElement;

	beforeEach(angular.mock.module('talend.widget'));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	beforeEach(inject(($rootScope, $compile, $timeout) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			const html = '<talend-modal fullscreen="fullscreen" state="state" on-close="onClose()" close-button="closeButton"></talend-modal>';
			scope.onClose = () => {
				scope.closeCallbackCalled = true;
			};

			element = $compile(html)(scope);
			scope.$digest();
			$timeout.flush();
		};

		disableElementHide = () => {
			const html = '<talend-modal close-button="false" state="state" forbid-close-on-background-click="disableCloseOnBackgroundClick">';
			element = $compile(html)(scope);
			scope.$digest();
			$timeout.flush();
		};

		createFormElement = () => {
				const html = `
					<talend-modal fullscreen="fullscreen" state="state" close-button="closeButton">
						<input type="text" id="firstInput" class="no-focus"/>
						<input type="text" id="secondInput" />
					</talend-modal>
				`;

			element = $compile(html)(scope);
			scope.$digest();
			$timeout.flush();
		};

		createNestedElement = () => {
			const html = `
				<talend-modal id="outerModal" fullscreen="fullscreen" state="state" close-button="closeButton">
					<talend-modal id="innerModal" fullscreen="innerfullscreen" state="innerState" close-button="innerCloseButton"></talend-modal>
				</talend-modal>
			`;

			element = $compile(html)(scope);
			scope.$digest();
			$timeout.flush();
		};

		createButtonElement = () => {
			const html = `
				<talend-modal fullscreen="fullscreen" state="state" close-button="closeButton">
					<button class="modal-primary-button" ng-click="click()"/>
				</talend-modal>
			`;

			scope.click = () => {
				scope.primaryButtonClicked = true;
			};

			element = $compile(html)(scope);
			scope.$digest();
			$timeout.flush();
		};

		createBeforeCloseElement = (beforeCloseFn) => {
			const html = '<talend-modal fullscreen="fullscreen" state="state" before-close="beforeClose()" close-button="closeButton"></talend-modal>';
			scope.beforeClose = beforeCloseFn;
			element = $compile(html)(scope);
			scope.$digest();
			$timeout.flush();
		};

		spyOn($rootScope, '$apply').and.callThrough();
	}));

	describe('display', () => {
		it('should show "normal" close button', () => {
			//given
			scope.fullscreen = false;
			scope.state = false;
			scope.closeButton = true;

			//when
			createElement();

			//then
			expect(element.find('.modal-close').length).toBe(1);
			expect(element.find('.modal-close').hasClass('ng-hide')).toBe(false);
		});

		it('should not show "normal" close button', () => {
			//given
			scope.fullscreen = false;
			scope.state = false;
			scope.closeButton = false;

			//when
			createElement();

			//then
			expect(element.find('.modal-close').length).toBe(1);
			expect(element.find('.modal-close').hasClass('ng-hide')).toBe(true);
		});

		it('should show "fullscreen" close button', () => {
			//given
			scope.fullscreen = true;
			scope.state = false;
			scope.closeButton = true;

			//when
			createElement();

			//then
			expect(element.find('.modal-header-close').length).toBe(1);
			expect(element.find('.modal-close').hasClass('ng-hide')).toBe(false);
		});

		it('should not show "fullscreen" close button', () => {
			//given
			scope.fullscreen = true;
			scope.state = false;
			scope.closeButton = false;

			//when
			createElement();

			//then
			expect(element.find('.modal-header-close').length).toBe(1);
			expect(element.find('.modal-header-close').hasClass('ng-hide')).toBe(true);
		});

		it('should add "modal-open" class to body when modal open state is true', () => {
			//given
			const body = angular.element('body');

			scope.fullscreen = false;
			scope.state = false;
			scope.closeButton = false;
			createElement();

			//when
			scope.state = true;
			scope.$apply();

			//then
			expect(body.hasClass('modal-open')).toBe(true);
		});

		it('should remove "modal-open" class to body when modal open state is false', () => {
			//given
			const body = angular.element('body');
			body.addClass('modal-open');

			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = false;
			createElement();

			//when
			scope.state = false;
			scope.$digest();

			//then
			expect(body.hasClass('modal-open')).toBe(false);
		});
	});

	describe('actions', () => {
		it('should hide modal on "modal-window" div click', inject(($timeout) => {
			//given
			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = false;
			createElement();

			//when
			element.find('.modal-window').click();
			$timeout.flush();

			//then
			expect(scope.state).toBe(false);
		}));

		it('should hide modal on "modal-close" button click', inject(($timeout) => {
			//given
			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = true;
			createElement();

			//when
			element.find('.modal-close').click();
			$timeout.flush();

			//then
			expect(scope.state).toBe(false);
		}));

		it('should hide modal on "modal-header-close" button click', inject(($timeout) => {
			//given
			scope.fullscreen = true;
			scope.state = true;
			scope.closeButton = true;
			createElement();

			//when
			element.find('.modal-header-close').click();
			$timeout.flush();

			//then
			expect(scope.state).toBe(false);
		}));

		it('should not hide modal on "modal-inner" div click', inject(($rootScope, $timeout) => {
			//given
			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = true;
			createElement();

			//when
			element.find('.modal-inner').click();
			try {
				$timeout.flush();
			}
			catch (error) {
				$rootScope.$apply();

				//then
				expect(scope.state).toBe(true);
				return;
			}

			throw new Error('Should have thrown error on timeout flush because hide should not be called on click in modal-inner div');
		}));

		it('should focus on "modal-inner" on input ESC keydown', inject(() => {
			//given
			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = true;
			createFormElement();

			const input = element.find('#firstInput').eq(0);
			input.focus();
			expect(document.activeElement.className).not.toContain('modal-inner');

			const event = angular.element.Event('keydown');
			event.keyCode = 27;

			//when
			input.trigger(event);

			//then
			expect(document.activeElement.className).toContain('modal-inner');
		}));

		it('should hide on ESC keydown', inject(($rootScope, $timeout) => {
			//given
			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = true;
			createElement();

			const event = angular.element.Event('keydown');
			event.keyCode = 27;

			//when
			element.find('.modal-inner').trigger(event);
			$timeout.flush();

			//then
			expect(scope.state).toBe(false);
		}));

		it('should not hide on not ESC keydown', inject(($rootScope, $timeout) => {
			//given
			scope.fullscreen = false;
			scope.state = true;
			scope.closeButton = true;
			createElement();

			const event = angular.element.Event('keydown');
			event.keyCode = 97;

			//when
			element.find('.modal-inner').trigger(event);
			try {
				$timeout.flush();
			}
				//then
			catch (error) {
				expect(scope.state).toBe(true);
				return;
			}

			//otherwise
			throw new Error('should have thrown error because no timeout is pending');
		}));

		it('should call close callback', inject(($rootScope) => {
			//given
			scope.state = true;
			createElement();
			expect(scope.closeCallbackCalled).toBeFalsy();

			//when
			scope.state = false;
			$rootScope.$apply();

			//then
			expect(scope.closeCallbackCalled).toBe(true);
		}));

		it('should NOT close modal when beforeClose returns false', inject(($timeout) => {
			//given
			scope.state = true;
			createBeforeCloseElement(() => {
				return false;
			});

			//when
			element.find('.modal-close').click();
			$timeout.flush();

			//then
			expect(scope.state).toBe(true);
		}));

		it('should close modal when beforeClose returns true', inject(($timeout) => {
			//given
			scope.state = true;
			createBeforeCloseElement(() => {
				return true;
			});

			//when
			element.find('.modal-close').click();
			$timeout.flush();

			//then
			expect(scope.state).toBe(false);
		}));
	});

	describe('element', () => {
		it('should attach popup to body', () => {
			//when
			createElement();

			//then
			expect(angular.element('body').find('talend-modal').length).toBe(1);
		});

		it('should remove element on scope destroy', () => {
			//given
			createElement();

			//when
			scope.$destroy();
			scope.$digest();

			//then
			expect(angular.element('body').find('talend-modal').length).toBe(0);
		});
	});

	describe('multi modal management', () => {
		it('should focus on "modal-inner" on module open', () => {
			//given
			scope.fullscreen = false;
			scope.state = false;
			scope.closeButton = false;
			createElement();

			const body = angular.element('body');
			body.append(element);
			expect(document.activeElement).not.toBe(element);

			//when
			scope.state = true;
			scope.$digest();

			//then
			expect(document.activeElement.className).toContain('modal-inner');
		});

		it('should focus on second input on show and select the text coz first has "no-focus" class', inject(($window) => {
			//given
			scope.fullscreen = false;
			scope.state = false;
			scope.closeButton = false;
			createFormElement();

			angular.element('body').append(element);

			element.find('#secondInput').val('city');

			expect(document.activeElement).not.toBe(element);
			expect($window.getSelection().toString()).toBeFalsy();

			//when
			scope.state = true;
			scope.$digest();

			//then
			const activeElement = document.activeElement;
			expect(activeElement.id).toBe('secondInput');
			expect(activeElement.value).toBe('city');
		}));

		it('should focus on next last shown modal on focused modal close', () => {
			//given : init
			scope.fullscreen = false;
			scope.state = false;
			scope.closeButton = false;
			scope.innerFullscreen = false;
			scope.innerState = false;
			scope.innerCloseButton = false;
			createNestedElement();

			const body = angular.element('body');
			body.append(element);
			expect(document.activeElement).not.toBe(element);

			//given : show outer modal
			scope.state = true;
			scope.$digest();
			const outerModal = body.find('#outerModal').eq(0).find('.modal-inner').eq(0)[0];
			expect(document.activeElement).toBe(outerModal);

			//given : show inner modal
			scope.innerState = true;
			scope.$digest();
			const innerModal = body.find('#innerModal').eq(0).find('.modal-inner').eq(0)[0];
			expect(document.activeElement).toBe(innerModal);

			//when
			scope.innerState = false;
			scope.$digest();

			//then
			expect(document.activeElement).toBe(outerModal);
		});
	});

	describe('forbid modal hide', () => {
		it('should NOT hide the modal on background click', inject(() => {
			//given
			scope.state = true;
			scope.disableCloseOnBackgroundClick = true;
			disableElementHide();

			//when
			element.find('.modal-window').click();

			//then
			expect(scope.state).toBe(true);
		}));

		it('should NOT hide the modal on ESCAPE button hit', inject(() => {
			//given
			scope.state = true;
			scope.disableCloseOnBackgroundClick = true;
			disableElementHide();
			const event = angular.element.Event('keydown');
			event.keyCode = 27;

			//when
			element.find('.modal-inner').trigger(event);

			//then
			expect(scope.state).toBe(true);
		}));
	});
});
