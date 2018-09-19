/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { PATTERNS_TYPE } from '../../../services/statistics/statistics-service.js';

describe('Stats-details controller', () => {
	'use strict';

	let createController;
	let scope;
	let stateMock;

	beforeEach(angular.mock.module('data-prep.stats-details', ($provide) => {
		stateMock = {
			playground: {
				grid: {},
				statistics: {},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $controller, FilterManagerService) => {
		scope = $rootScope.$new();

		createController = () => {
			return $controller('StatsDetailsCtrl', {
				$scope: scope,
			});
		};

		spyOn(FilterManagerService, 'addFilterAndDigest').and.returnValue();
	}));

	it('should add a new "pattern" filter', inject((FilterManagerService) => {
		//given
		const ctrl = createController();
		const obj = { data: 'Ulysse', occurrences: 5, pattern: 'Aa9' };

		stateMock.playground.statistics.patternsType = PATTERNS_TYPE.CHARACTER;
		stateMock.playground.grid.selectedColumns = [{
			id: '0001',
			name: 'firstname',
		}];

		//when
		ctrl.addPatternFilter(obj);

		//then
		expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('matches', '0001', 'firstname', {
			patterns: [
				{
					value: 'Aa9',
				},
			],
		}, null, null);
	}));

	it('should add a new "empty" filter if pattern is empty', inject((FilterManagerService) => {
		//given
		const ctrl = createController();
		const obj = { data: 'Ulysse', occurrences: 5 };

		stateMock.playground.grid.selectedColumns = [{
			id: '0001',
			name: 'firstname',
		}];

		//when
		ctrl.addPatternFilter(obj);

		//then
		expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('quality', '0001', 'firstname', { empty: true, invalid: false }, null, null);
	}));

	it('should add a new word based pattern filter', inject((FilterManagerService) => {
		//given
		const ctrl = createController();
		const obj = { data: 'Ulysse', occurrences: 5, pattern: '[word]' };

		stateMock.playground.statistics.patternsType = PATTERNS_TYPE.WORD;
		stateMock.playground.grid.selectedColumns = [{
			id: '0001',
			name: 'firstname',
		}];

		//when
		ctrl.addPatternFilter(obj);

		//then
		expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('word_matches', '0001', 'firstname', {
			patterns: [
				{
					value: '[word]',
				},
			],
		}, null, null);
	}));

	describe('Pattern switcher', () => {
		beforeEach(inject((StateService) => {
			spyOn(StateService, 'setStatisticsPatternsType');
		}));

		it('should select character based patterns', inject((StateService) => {
			//given
			const ctrl = createController();

			//when
			ctrl.onCharacterPatternSelect();

			//then
			expect(StateService.setStatisticsPatternsType).toHaveBeenCalledWith(PATTERNS_TYPE.CHARACTER);
		}));

		it('should select word based patterns', inject((StateService) => {
			//given
			const ctrl = createController();

			//when
			ctrl.onWordPatternSelect();

			//then
			expect(StateService.setStatisticsPatternsType).toHaveBeenCalledWith(PATTERNS_TYPE.WORD);
		}));
	});
});
