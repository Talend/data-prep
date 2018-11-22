/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_PREPARATIONS_ROUTE } from '../../../index-route';


describe('Playground header controller', () => {
	'use strict';

	let scope;
	let createController;
	let element;
	let stateMock;
	const createPreparation = { id: 'create-preparation-id' };

	beforeEach(angular.mock.module('data-prep.playground-header', ($provide) => {
		stateMock = {
			route: {
				previous: HOME_PREPARATIONS_ROUTE,
			},
			playground: {
				dataset: {},
				lookup: { actions: [] },
				preparationName: '',
				recipe: { current: { steps: [] } },
			},
			inventory: {
				homeFolder: { id: 'LW==' },
				currentFolder: { path: 'test' },
				folder: {
					metadata: {
						id: 'abcd'
					}
				}
			},
		};

		$provide.constant('state', stateMock);
	}));


	beforeEach(inject(($rootScope, $q, $controller, $state, PlaygroundService) => {
		scope = $rootScope.$new();

		createController = () => {
			return $controller('PlaygroundHeaderCtrl', { $scope: scope });
		};

		spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(createPreparation));
		spyOn($state, 'go').and.returnValue();
	}));

	describe('recipe header', () => {
		it('should create/update preparation with clean name on name edition confirmation', inject((PlaygroundService) => {
			const ctrl = createController();
			stateMock.playground.preparationName = '  my new name  ';

			ctrl.editName({}, { value: stateMock.playground.preparationName });

			expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my new name');
		}));

		it('should change route to preparation route on name edition confirmation', inject(($rootScope, $state) => {
			const ctrl = createController();
			stateMock.playground.preparationName = '  my new name  ';
			stateMock.playground.preparation = { id: 'fe6843da512545e' };

			ctrl.editName({}, { value: stateMock.playground.preparationName });
			$rootScope.$digest();

			expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: createPreparation.id });
		}));

		it('should not call service create/updateName service if name is blank on name edition confirmation', inject((PlaygroundService) => {
			const ctrl = createController();

			ctrl.editName({}, { value: ' ' });

			expect(PlaygroundService.createOrUpdatePreparation).not.toHaveBeenCalled();
		}));
	});
});
