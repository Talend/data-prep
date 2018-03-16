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
	const w = { location: { href: 'https://a.b/c/d?e=f&g=h' }, open: () => {} };

	beforeEach(angular.mock.module('app.settings.actions', ($provide) => {
		$provide.value('$window', w);
	}));

	describe('dispatch', () => {
		it('should open page', inject(($window, ExternalActionsService) => {
			const action = {
				type: '@@external/OPEN_PAGE',
				payload: {
					method: 'open',
					args: ['https://1.2/3/4']
				}
			};

			ExternalActionsService.dispatch(action);

			expect($window.location.href).toEqual('https://1.2/3/4?redirect=https%3A%2F%2Fa.b%2Fc%2Fd%3Fe%3Df%26g%3Dh');
		}));

		it('should open window', inject(($window, ExternalActionsService) => {
			const action = {
				type: '@@external/OPEN_WINDOW',
				payload: {
					method: 'open',
					args: ['http://www.google.fr']
				}
			};
			spyOn($window, 'open').and.returnValue();

			ExternalActionsService.dispatch(action);

			expect($window.open).toHaveBeenCalledWith('http://www.google.fr');
		}));

		it('should open without args', inject(($window, ExternalActionsService) => {
			const action = {
				type: '@@external/OPEN_WINDOW',
				payload: {
					method: 'open',
					args: [],
					url: 'http://www.google.fr',
				}
			};
			spyOn($window, 'open').and.returnValue();

			ExternalActionsService.dispatch(action);

			expect($window.open).toHaveBeenCalledWith('http://www.google.fr');
		}));
	});
});
