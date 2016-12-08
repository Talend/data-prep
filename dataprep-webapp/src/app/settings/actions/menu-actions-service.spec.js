/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Menu actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should change route', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO',
				payload: {
					method: 'go',
					args: ['nav.index.preparations']
				}
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith('nav.index.preparations');
		}));

		it('should change route with dataset parameters', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_DATASET',
				payload: {
					method: 'go',
					args: ['nav.index.datasets'],
					id: 'acbd'
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith('nav.index.datasets', { datasetid: 'acbd' });
		}));

		it('should change route with folder parameters', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_FOLDER',
				payload: {
					method: 'go',
					args: ['nav.index.preparations'],
					id: 'acbd'
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith('nav.index.preparations', { folderId: 'acbd' });
		}));

		it('should change route with preparation id', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_PREPARATION',
				payload: {
					method: 'go',
					args: ['playground.preparation'],
					id: 'acbd'
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: 'acbd' });
		}));
	});
});
