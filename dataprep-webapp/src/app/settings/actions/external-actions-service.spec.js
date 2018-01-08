/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('External actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should open window', inject(($window, ExternalActionsService) => {
			// given
			const action = { 
				type: '@@external/OPEN_WINDOW',
				payload: {
					method: 'open',
					args: ['http://www.google.fr']
				}
			};
			spyOn($window, 'open').and.returnValue();
			
			// when
			ExternalActionsService.dispatch(action);
			
			// then
			expect($window.open).toHaveBeenCalledWith('http://www.google.fr');
		}));

		it('should open without args', inject(($window, ExternalActionsService) => {
			// given
			const action = {
				type: '@@external/OPEN_WINDOW',
				payload: {
					method: 'open',
					args: [],
					url: 'http://www.google.fr',
				}
			};
			spyOn($window, 'open').and.returnValue();

			// when
			ExternalActionsService.dispatch(action);

			// then
			expect($window.open).toHaveBeenCalledWith('http://www.google.fr');
		}));
	});
});
