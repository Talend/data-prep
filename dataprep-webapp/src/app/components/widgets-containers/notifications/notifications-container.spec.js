/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import settings from '../../../../mocks/Settings.mock';

function getNotification(type) {
	return {
		type,
		message: 'Test Message',
	};
}


describe('Notifications container', () => {
	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('@talend/react-components.containers'));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	describe('Notifications rendering', () => {
		beforeEach(inject(($rootScope, $compile, StateService, state) => {
			state.message.messages = [];
			scope = $rootScope.$new();

			createElement = () => {
				element = angular.element('<notifications id="notifications-container"></notifications>');
				$compile(element)(scope);
				scope.$digest();
			};
		}));

		describe('Collapsible header', () => {
			it('should render pure-notification', () => {
				createElement();
				expect(element.find('pure-notification').length).toBe(1);
			});

			it('should render warning notification', inject((StateService) => {
				StateService.pushMessage(getNotification('warning'));
				createElement();

				expect(element.find('.tc-notification-warning').length).toBe(1);
			}));

			it('should render error notification', inject((StateService) => {
				StateService.pushMessage(getNotification('error'));
				createElement();

				expect(element.find('.tc-notification-error').length).toBe(1);
			}));

			it('should render info notification', inject((StateService) => {
				StateService.pushMessage(getNotification('info'));
				createElement();

				expect(element.find('.tc-notification-info').length).toBe(1);
			}));

			it('should render multiple notifications', inject((StateService) => {
				StateService.pushMessage(getNotification('info'));
				StateService.pushMessage(getNotification('info'));
				StateService.pushMessage(getNotification('info'));
				createElement();

				expect(element.find('.tc-notification-info').length).toBe(3);
			}));

			it('should render notification with title', inject((StateService) => {
				StateService.pushMessage({
					...getNotification('warning'),
					title: 'Test title',
				});
				createElement();

				expect(element.find('.tc-notification-warning').length).toBe(1);
				expect(element.find('.tc-notification-title').length).toBe(1);
			}));

			it('should render multi-lines notification', inject((StateService) => {
				StateService.pushMessage({
					...getNotification('warning'),
					message: ['Line 1', 'Line 2', 'Line 3'],
				});
				createElement();

				expect(element.find('.tc-notification-warning').length).toBe(1);
				expect(element.find('.tc-notification-message').length).toBe(3);
			}));
		 });
	});
});
