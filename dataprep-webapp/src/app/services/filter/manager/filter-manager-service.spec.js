/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import {
	INVALID_RECORDS,
	EMPTY_RECORDS,
	INVALID_EMPTY_RECORDS,
	VALID_RECORDS,
	QUALITY,
} from '../adapter/filter-adapter-service';

describe('Filter Manager Service', () => {

	let stateMock;
	beforeEach(angular.mock.module('data-prep.services.filter-manager-service', ($provide) => {
		const columns = [
			{ id: '0000', name: 'id' },
			{ id: '0001', name: 'name' },
		];

		stateMock = {
			playground: {
				preparation: {id: 'abcd'},
				filter: { gridFilters: [] },
				data: { metadata: { columns: columns } },
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject((FilterService, PlaygroundService, StateService, StatisticsService, StorageService) => {
		spyOn(StatisticsService, 'updateFilteredStatistics').and.returnValue();
		spyOn(FilterService, 'addFilter').and.returnValue();
		spyOn(FilterService, 'removeAllFilters').and.returnValue();
		spyOn(FilterService, 'removeFilter').and.returnValue();
		spyOn(FilterService, 'toggleFilters').and.returnValue();
		spyOn(FilterService, 'updateFilter').and.returnValue();
		spyOn(StorageService, 'saveFilter').and.returnValue();
		spyOn(StorageService, 'removeFilter').and.returnValue();
		spyOn(PlaygroundService, 'updateDatagrid').and.returnValue();
	}));

	describe('Interval label', () => {
	    it('should construct range label', inject((FilterManagerService) => {
	        //given
	        const intervals = [
	            { input: { min: 0, max: 10, isMaxReached: false }, output: '[0 .. 10[' },
	            { input: { min: 10, max: 10, isMaxReached: false }, output: '[10]' },
	            { input: { min: 0, max: 10, isMaxReached: true }, output: '[0 .. 10]' },
	            { input: { min: 'Jan 2015', max: 'Mar 2015', isMaxReached: true }, output: '[Jan 2015 .. Mar 2015]' },
	        ];

	        //when
	        const fn = FilterManagerService.getRangeLabelFor;

	        //then
	        intervals.forEach(interval => expect(fn(interval.input)).toEqual(interval.output));
	    }));
	});

	describe('add filter', () => {
		it('should addFilter', inject((FilterManagerService, FilterService) => {
			// given
			const type = 'type';
			const colId = 'colId';
			const colName = 'colName';
			const args = {};
			const removeFilterFn = () => {};
			const keyName = 'keyName';

			// when
			FilterManagerService.addFilter(type, colId, colName, args, removeFilterFn, keyName);

			// then
			expect(FilterService.addFilter).toHaveBeenCalledWith(type, colId, colName, args, removeFilterFn, keyName);
		}));

		it('should update Filtered Statistics', inject((FilterManagerService, StatisticsService) => {
			// when
			FilterManagerService.addFilter();

			// then
			expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
		}));

		it('should save Filter in local storage when preparation is opened', inject((FilterManagerService, StorageService) => {
			// when
			FilterManagerService.addFilter();

			// then
			expect(StorageService.saveFilter).toHaveBeenCalledWith(
				stateMock.playground.preparation.id,
				stateMock.playground.filter.gridFilters
			);
		}));

		it('should save Filter in local storage when dataset is opened', inject((FilterManagerService, StorageService) => {
			// given
			const dataset = { id: 'datasetId' };
			stateMock.playground.preparation = null;
			stateMock.playground.dataset = dataset;

			// when
			FilterManagerService.addFilter();

			// then
			expect(StorageService.saveFilter).toHaveBeenCalledWith(
				stateMock.playground.dataset.id,
				stateMock.playground.filter.gridFilters
			);
		}));
	});

	describe('add filter and digest', () => {
		it('should add a filter wrapped in $timeout to trigger a digest', inject(($timeout, FilterService, FilterManagerService) => {
			// given
			const type = 'type';
			const colId = 'colId';
			const colName = 'colName';
			const args = {};
			const removeFilterFn = () => {};
			const keyName = 'keyName';

			// when
			FilterManagerService.addFilterAndDigest(type, colId, colName, args, removeFilterFn, keyName);
			expect(FilterService.addFilter).not.toHaveBeenCalled();
			$timeout.flush();

			// then
			expect(FilterService.addFilter).toHaveBeenCalledWith(type, colId, colName, args, removeFilterFn, keyName);
		}));
	});

	describe('remove all filters', () => {
		it('should call removeAllFilters method of Filter service', inject((FilterManagerService, FilterService) => {
			// when
			FilterManagerService.removeAllFilters();

			// then
			expect(FilterService.removeAllFilters).toHaveBeenCalled();
		}));

		it('should update filtered statistics', inject((FilterManagerService, StatisticsService) => {
			// when
			FilterManagerService.removeAllFilters();

			// then
			expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
		}));

		it('should remove filter from local storage when preparation is opened', inject((FilterManagerService, StorageService) => {
			// when
			FilterManagerService.removeAllFilters();

			// then
			expect(StorageService.removeFilter).toHaveBeenCalledWith(stateMock.playground.preparation.id);
		}));

		it('should remove filter from local storage when dataset is opened', inject((FilterManagerService, StorageService) => {
			//given
			const dataset = { id: 'datasetId' };
			stateMock.playground.preparation = null;
			stateMock.playground.dataset = dataset;

			// when
			FilterManagerService.removeAllFilters();

			// then
			expect(StorageService.removeFilter).toHaveBeenCalledWith(stateMock.playground.dataset.id);
		}));
	});

	describe('remove a filter', () => {
		it('should call remove filter method of Filter service', inject((FilterManagerService, FilterService) => {
			// given
			const filter = {};

			// when
			FilterManagerService.removeFilter(filter);

			// then
			expect(FilterService.removeFilter).toHaveBeenCalledWith(filter);
		}));

		it('should update filtered statistics', inject((FilterManagerService, StatisticsService) => {
			// when
			FilterManagerService.removeFilter();

			// then
			expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
		}));

		it('should remove filter from local storage when preparation is opened', inject((FilterManagerService, StorageService) => {
			// when
			FilterManagerService.removeFilter();

			// then
			expect(StorageService.saveFilter).toHaveBeenCalledWith(
				stateMock.playground.preparation.id,
				stateMock.playground.filter.gridFilters
			);
		}));
	});

	describe('toggle all filters', () => {
		it('should call toggleFilters filter method of Filter service', inject((FilterManagerService, FilterService) => {
			// when
			FilterManagerService.toggleFilters();

			// then
			expect(FilterService.toggleFilters).toHaveBeenCalled();
		}));

		it('should update filtered statistics', inject((FilterManagerService, StatisticsService) => {
			// when
			FilterManagerService.toggleFilters();

			// then
			expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
		}));
	});

	describe('update filter', () => {
		it('should call updateFilter method of filter service', inject((FilterManagerService, FilterService) => {
			// given
			const arg1 = {};
			const arg2 = 'newValue';
			const arg3 = 'keyName';

			// when
			FilterManagerService.updateFilter(arg1, arg2, arg3);

			// then
			expect(FilterService.updateFilter).toHaveBeenCalledWith(arg1, arg2, arg3);
		}));

		it('should update filtered statistics', inject((FilterManagerService, StatisticsService) => {
			// when
			FilterManagerService.updateFilter();

			// then
			expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
		}));

		it('should update filter in local storage', inject((FilterManagerService, StorageService) => {
			// when
			FilterManagerService.updateFilter();

			// then
			expect(StorageService.saveFilter).toHaveBeenCalledWith(
				stateMock.playground.preparation.id,
				stateMock.playground.filter.gridFilters
			);
		}));
	});

	describe('create quality filter', () => {
		beforeEach(inject((FilterManagerService) => {
			spyOn(FilterManagerService, 'addFilterAndDigest').and.returnValue();
		}));

		it('should create valid_records filter', inject((FilterManagerService) => {
			//given
			let column = {
				id: '0000',
				name: 'col1',
			};

			//when
			FilterManagerService.createQualityFilter(VALID_RECORDS, column);

			//then
			expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(VALID_RECORDS, '0000', 'col1');
		}));
		it('should create invalid_records filter', inject((FilterManagerService) => {
			//given
			let column = {
				id: '0000',
				name: 'col1',
			};

			//when
			FilterManagerService.createQualityFilter(INVALID_RECORDS, column);

			//then
			expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(INVALID_RECORDS, '0000', 'col1');
		}));

		it('should create empty_records filter', inject((FilterManagerService) => {
			//given
			let column = {
				id: '0000',
				name: 'col1',
			};

			//when
			FilterManagerService.createQualityFilter(EMPTY_RECORDS, column);

			//then
			expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(EMPTY_RECORDS, '0000', 'col1');
		}));

		it('should create invalid_empty_records filter', inject((FilterManagerService) => {
			//given
			let column = {
				id: '0000',
				name: 'col1',
			};

			//when
			FilterManagerService.createQualityFilter(INVALID_EMPTY_RECORDS, column);

			//then
			expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(QUALITY, '0000', 'col1', { invalid: true, empty: true });
		}));

	});
});
