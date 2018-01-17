/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Modal actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should trigger state', inject((StateService, ModalActionsService) => {
			// given
			const action = { 
				type: '@@modal/SHOW',
				payload: {
					method: 'showFeedback',
				}
			};
			spyOn(StateService, 'showFeedback').and.returnValue();
			
			// when
			ModalActionsService.dispatch(action);
			
			// then
			expect(StateService.showFeedback).toHaveBeenCalled();
		}));
	});
});
