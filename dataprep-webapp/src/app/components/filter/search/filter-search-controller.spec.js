/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import i18n from '../../../../i18n/en';

describe('filter search controller', () => {
	'use strict';

	let createController;
	let scope;
	let stateMock;

	const data = {
		metadata: {
			columns: [
				{
					id: '0000',
					name: 'id',
					quality: {
						empty: 5,
						invalid: 10,
						valid: 72,
					},
					type: 'number',
				},
				{
					id: '0001',
					name: 'Postal',
					quality: {
						empty: 5,
						invalid: 10,
						valid: 72,
					},
					type: 'string',
				},
				{
					id: '0002',
					name: 'State',
					quality: {
						empty: 5,
						invalid: 10,
						valid: 72,
					},
					type: 'string',
				},
				{
					id: '0003',
					name: 'Capital',
					quality: {
						empty: 5,
						invalid: 10,
						valid: 72,
					},
					type: 'string',
				},
				{
					id: '0004',
					name: 'MostPopulousCity',
					quality: {
						empty: 5,
						invalid: 10,
						valid: 72,
					},
					type: 'string',
				},
			],
		},
		records: [
			{
				'0000': '1',
				'0001': 'AL',
				'0002': 'My Alabama',
				'0003': 'Montgomery',
				'0004': 'Birmingham city',
				tdpId: 0,
			},
			{
				'0000': '2',
				'0001': 'AK',
				'0002': 'Alaska',
				'0003': 'Juneau',
				'0004': 'Anchorage',
				tdpId: 1,
			},
			{
				'0000': '3',
				'0001': 'AL',
				'0002': 'My Alabama 2',
				'0003': 'Montgomery',
				'0004': 'Birmingham city',
				tdpId: 2,
			},
			{
				'0000': '3',
				'0001': 'AL',
				'0002': 'My Alabama 3',
				'0003': 'Montgomery',
				'0004': 'Alabama city',
				tdpId: 3,
			},
		],
	};

	beforeEach(angular.mock.module('data-prep.filter-search', ($provide) => {
		stateMock = { playground: {} };
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $controller, FilterManagerService) => {
		scope = $rootScope.$new();

		createController = () => {
			return $controller('FilterSearchCtrl', {
				$scope: scope,
			});
		};

		spyOn(FilterManagerService, 'addFilter').and.returnValue();
	}));

	it('should create sorted suggestions based on case insensitive typed word and current data from service', () => {
		//given
		stateMock.playground.data = data;
		const ctrl = createController();

		//when
		const suggestions = ctrl.filterSuggestOptions.suggest('ala');

		//then
		expect(suggestions.length).toBe(2);
		expect(suggestions[0]).toEqual({
			label: `ala ${i18n.IN} <b>MostPopulousCity</b>`,
			value: 'ala',
			columnId: '0004',
			columnName: 'MostPopulousCity',
		});
		expect(suggestions[1]).toEqual({
			label: `ala ${i18n.IN} <b>State</b>`,
			value: 'ala',
			columnId: '0002',
			columnName: 'State',
		});
	});

	it('should create sorted suggestions based on typed word with wildcard', () => {
		//given
		stateMock.playground.data = data;
		const ctrl = createController();

		//when
		const suggestions = ctrl.filterSuggestOptions.suggest('ala*ma');

		//then
		expect(suggestions.length).toBe(2);
		expect(suggestions[0]).toEqual({
			label: `ala*ma ${i18n.IN} <b>MostPopulousCity</b>`,
			value: 'ala*ma',
			columnId: '0004',
			columnName: 'MostPopulousCity',

		});
		expect(suggestions[1]).toEqual({
			label: `ala*ma ${i18n.IN} <b>State</b>`,
			value: 'ala*ma',
			columnId: '0002',
			columnName: 'State',
		});
	});

	it('should return empty array if typed string is empty', () => {
		//given
		stateMock.playground.data = data;
		const ctrl = createController();

		//when
		const suggestions = ctrl.filterSuggestOptions.suggest('');

		//then
		expect(suggestions.length).toBe(0);
	});

	it('should reset input search on item selection', () => {
		//given
		stateMock.playground.data = data;
		const ctrl = createController();
		ctrl.filterSearch = 'ala';

		//when
		ctrl.filterSuggestOptions.on_select({
			label: `ala ${i18n.IN} <b>State</b>`,
			value: 'ala',
			columnName: 'State',
			columnId: '0002',
		});

		//then
		expect(ctrl.filterSearch).toBe('');
	});

	it('should add filter on item selection', inject((FilterManagerService) => {
		//given
		stateMock.playground.data = data;
		const ctrl = createController();
		ctrl.filterSearch = 'ala';

		expect(FilterManagerService.addFilter).not.toHaveBeenCalled();

		//when
		ctrl.filterSuggestOptions.on_select({
			label: `ala ${i18n.IN} <b>State</b>`,
			value: 'ala',
			columnName: 'State',
			columnId: '0002',
		});

		//then
		expect(FilterManagerService.addFilter).toHaveBeenCalledWith('contains', '0002', 'State', {
			phrase: [
				{
					value: 'ala',
				},
			],
		});
	}));
});
