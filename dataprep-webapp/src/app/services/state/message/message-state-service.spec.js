/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/


describe('Message state service', function () {
	'use strict';

	beforeEach(angular.mock.module('data-prep.services.state'));

	describe('push', function () {
		it('should push a new message with a random id', inject((messageState, MessageStateService) => {
			const mock = {
				type: 'warning',
				title: 'Test title',
				message: 'Test Message',
			};

			messageState.messages = [];

			MessageStateService.push(mock);

			expect(messageState.messages[0].id).toBeTruthy();
			expect(messageState.messages[0].title).toBe(mock.title);
			expect(messageState.messages[0].message).toBe(mock.message);
		}));
	});

	describe('pop', function () {
		it('should remove a message from the stack', inject((messageState, MessageStateService) => {
			const a = { type: 'warning', id: '666', message: 'A' };
			const b = { type: 'error', id: '42', message: 'B' };
			const c = { type: 'info', id: '314', message: 'C' };

			messageState.messages = [a, b, c];

			MessageStateService.pop(b);

			expect(messageState.messages.length).toBe(2);
			expect(messageState.messages[0]).toEqual(a);
			expect(messageState.messages[1]).toEqual(c);
		}));
	});
});
