/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import i18n from '../../../../i18n/en.json';

describe('Playground state service', () => {
    'use strict';

    let recipeStateMock;
    let gridStateMock;
    let filterStateMock;
    let parametersStateMock;

    beforeEach(angular.mock.module('data-prep.services.state', ($provide) => {
        recipeStateMock = {};
        gridStateMock = {};
        filterStateMock = {};
        parametersStateMock = {};
        $provide.constant('recipeState', recipeStateMock);
        $provide.constant('gridState', gridStateMock);
        $provide.constant('filterState', filterStateMock);
        $provide.constant('parametersState', parametersStateMock);
    }));

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

    beforeEach(inject((GridStateService, RecipeStateService, FilterStateService, LookupStateService, SuggestionsStateService, ParametersStateService) => {
        spyOn(GridStateService, 'setData').and.returnValue();
        spyOn(GridStateService, 'setFilter').and.returnValue();
        spyOn(GridStateService, 'reset').and.returnValue();
        spyOn(FilterStateService, 'addGridFilter').and.returnValue();
        spyOn(FilterStateService, 'updateGridFilter').and.returnValue();
        spyOn(FilterStateService, 'removeGridFilter').and.returnValue();
        spyOn(FilterStateService, 'removeAllGridFilters').and.returnValue();
        spyOn(FilterStateService, 'reset').and.returnValue();
        spyOn(FilterStateService, 'enableFilters').and.returnValue();
        spyOn(FilterStateService, 'disableFilters').and.returnValue();
        spyOn(LookupStateService, 'reset').and.returnValue();
        spyOn(LookupStateService, 'setVisibility').and.returnValue();
        spyOn(SuggestionsStateService, 'reset').and.returnValue();
        spyOn(ParametersStateService, 'hide').and.returnValue();
        spyOn(ParametersStateService, 'show').and.returnValue();
        spyOn(ParametersStateService, 'update').and.returnValue();
        spyOn(ParametersStateService, 'reset').and.returnValue();
        spyOn(RecipeStateService, 'reset').and.returnValue();
    }));

    describe('readonly mode', () => {
        it('should set readonly mode in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isReadOnly).toBe(false);

            //when
            PlaygroundStateService.setReadOnlyMode(true);

            //then
            expect(playgroundState.isReadOnly).toBe(true);
        }));
    });

    describe('saving preparation', () => {
        it('should set NameValidation visibility', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isNameValidationVisible).toBeFalsy();

            //when
            PlaygroundStateService.setIsNameValidationVisible(true);

            //then
            expect(playgroundState.isNameValidationVisible).toBe(true);
        }));

        it('should set preparation picker visibility', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isPreprationPickerVisible).toBeFalsy();

            //when
            PlaygroundStateService.setIsPreprationPickerVisible(true);

            //then
            expect(playgroundState.isPreprationPickerVisible).toBe(true);
        }));

        it('should set saving preparation folders', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.savingPreparationFolders).toBeFalsy();

            //when
            PlaygroundStateService.setSavingPreparationFolders({folders :[]});

            //then
            expect(playgroundState.savingPreparationFolders).toEqual({folders :[]});
        }));

        it('should set saving preparation folders loading', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isSavingPreparationFoldersLoading).toBeFalsy();

            //when
            PlaygroundStateService.setIsSavingPreparationFoldersLoading(true);

            //then
            expect(playgroundState.isSavingPreparationFoldersLoading).toBe(true);
        }));
    });

    describe('dataset', () => {
        it('should set dataset metadata in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            const dataset = {
                id: '958cb63f235e4565',
            };
            expect(playgroundState.dataset).not.toBe(dataset);

            //when
            PlaygroundStateService.setDataset(dataset);

            //then
            expect(playgroundState.dataset).toBe(dataset);
        }));
    });

    describe('sample type', () => {
        it('should init sample type', inject((playgroundState) => {
            // then
            expect(playgroundState.sampleType).toBe('HEAD');
        }));

        it('should set sample type flag', inject((playgroundState, PlaygroundStateService) => {
            // given
            expect(playgroundState.sampleType).toBe('HEAD');

            // when
            PlaygroundStateService.setSampleType('FILTER');

            // then
            expect(playgroundState.sampleType).toBe('FILTER');
        }));
    });

    describe('preparation', () => {
        it('should set preparation in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.preparation).toBeFalsy();
            const preparation = {
                id: '3d245846bc46f51',
            };

            //when
            PlaygroundStateService.setPreparation(preparation);

            //then
            expect(playgroundState.preparation).toBe(preparation);
        }));

        it('should set preparation name in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.preparationName).toBeFalsy();
            const preparationName = 'the preparation name';

            //when
            PlaygroundStateService.setPreparationName(preparationName);

            //then
            expect(playgroundState.preparationName).toBe(preparationName);
        }));

        it('should set name edition flag in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.nameEditionMode).toBeFalsy();

            //when
            PlaygroundStateService.setNameEditionMode(true);

            //then
            expect(playgroundState.nameEditionMode).toBe(true);
        }));

        it('should set saving in progress flag', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isSavingPreparation).toBeFalsy();

            //when
            PlaygroundStateService.setIsSavingPreparation(true);

            //then
            expect(playgroundState.isSavingPreparation).toBe(true);
        }));

        it('should set loading flag', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isLoading).toBeFalsy();

            //when
            PlaygroundStateService.setIsLoading(true);

            //then
            expect(playgroundState.isLoading).toBe(true);
        }));

        it('should set Lookup Visibility flag', inject((playgroundState, LookupStateService, PlaygroundStateService) => {
            //given
            playgroundState.grid = {
                selectedColumns :  [{ id: '0000'}, { id: '0001'}],
            };

            //when
            PlaygroundStateService.setLookupVisibility(true);

            //then
            expect(playgroundState.grid.selectedColumns).toEqual([{ id: '0000'}]);
            expect(LookupStateService.setVisibility).toHaveBeenCalledWith(true);
        }));

        it('should set the step in edition mode', inject((playgroundState, LookupStateService, PlaygroundStateService) => {
            //given
            playgroundState.stepInEditionMode = null;
            const step = {};

            //when
            PlaygroundStateService.setStepInEditionMode(step);

            //then
            expect(playgroundState.stepInEditionMode).toBe(step);
        }));
    });

    describe('data', () => {
        it('should set data in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.data).toBeFalsy();
            const data = {
                records: [],
            };

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(playgroundState.data).toBe(data);
        }));

        it('should set data in grid', inject((playgroundState, PlaygroundStateService, GridStateService) => {
            //given
            expect(GridStateService.setData).not.toHaveBeenCalled();
            const data = {
                records: [],
            };

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(GridStateService.setData).toHaveBeenCalledWith(data);
        }));

        it('should set filters again on data change in grid to refresh statefull filters (ex: invalid filters)', inject((playgroundState, PlaygroundStateService, GridStateService) => {
            //given
            expect(GridStateService.setData).not.toHaveBeenCalled();
            const data = { records: [] };
            const filters = [{}, {}];
            filterStateMock.gridFilters = filters;
            filterStateMock.enabled = true;

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
        }));

        it('should set data with disabled filter', inject((playgroundState, PlaygroundStateService, GridStateService) => {
            //given
            expect(GridStateService.setData).not.toHaveBeenCalled();
            const data = { records: [] };
            const filters = [{}, {}];
            filterStateMock.gridFilters = filters;
            filterStateMock.enabled = false;

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(GridStateService.setFilter).toHaveBeenCalledWith([], data);
        }));
    });

    describe('column statistics', () => {
        it('should set fetch in progress flag in state', inject((playgroundState, PlaygroundStateService) => {
            //given
            expect(playgroundState.isFetchingStats).toBeFalsy();

            //when
            PlaygroundStateService.setIsFetchingStats(true);

            //then
            expect(playgroundState.isFetchingStats).toBe(true);
        }));

        it('should update columns statistics', inject((playgroundState, PlaygroundStateService) => {
            //given
            playgroundState.data = {
                metadata: {
                    columns: [
                        { id: '0000', statistics: {} },
                        { id: '0001', statistics: {} },
                        { id: '0002', statistics: {} },
                    ],
                },
            };
            const newMetadata = {
                columns: [
                    { id: '0000', statistics: { frequencyTable: [{ data: '5.0', occurrences: 98 }] } },
                    { id: '0001', statistics: { frequencyTable: [{ data: 'Toto', occurrences: 5 }] } },
                    { id: '0002', statistics: { frequencyTable: [{ data: '', occurrences: 66 }] } },
                ],
                records: 256,
            };

            //when
            PlaygroundStateService.updateDatasetStatistics(newMetadata);

            //then
            expect(playgroundState.data.metadata.columns[0].statistics).toBe(newMetadata.columns[0].statistics);
            expect(playgroundState.data.metadata.columns[1].statistics).toBe(newMetadata.columns[1].statistics);
            expect(playgroundState.data.metadata.columns[2].statistics).toBe(newMetadata.columns[2].statistics);
        }));

        it('should update columns quality infos', inject((playgroundState, PlaygroundStateService) => {
            //given
            playgroundState.data = {
                metadata: {
                    columns: [
                        { id: '0000', statistics: {} },
                        { id: '0001', statistics: {} },
                        { id: '0002', statistics: {} },
                    ],
                },
            };
            const newMetadata = {
                columns: [
                    { id: '0000', quality: { valid: 253, invalid: 3, empty: 0 } },
                    { id: '0001', quality: { valid: 7, invalid: 25, empty: 18 } },
                    { id: '0002', quality: { valid: 10, invalid: 0, empty: 110 } },
                ],
            };

            //when
            PlaygroundStateService.updateDatasetStatistics(newMetadata);

            //then
            expect(playgroundState.data.metadata.columns[0].quality).toBe(newMetadata.columns[0].quality);
            expect(playgroundState.data.metadata.columns[1].quality).toBe(newMetadata.columns[1].quality);
            expect(playgroundState.data.metadata.columns[2].quality).toBe(newMetadata.columns[2].quality);
        }));

        it('should update dataset records number', inject((playgroundState, PlaygroundStateService) => {
            //given
            playgroundState.dataset = {
                id: '958cb63f235e4565',
                records: 10,
            };
            expect(playgroundState.dataset.records).toBe(10);

            //when
            PlaygroundStateService.updateDatasetRecord(15);

            //then
            expect(playgroundState.dataset.records).toBe(15);
        }));
    });

    describe('parameters', () => {
        it('should hide parameters when they are visible', inject((PlaygroundStateService, ParametersStateService) => {
            //given
            parametersStateMock.visible = true;

            //when
            PlaygroundStateService.toggleDatasetParameters();

            //then
            expect(ParametersStateService.hide).toHaveBeenCalled();
        }));

        it('should show parameters when they are not visible', inject((PlaygroundStateService, ParametersStateService) => {
            //given
            parametersStateMock.visible = false;

            //when
            PlaygroundStateService.toggleDatasetParameters();

            //then
            expect(ParametersStateService.show).toHaveBeenCalled();
        }));

        it('should update parameters when they are not visible', inject((playgroundState, PlaygroundStateService, ParametersStateService) => {
            //given
            const dataset = { id: '56a74a6425432cf57b87' };
            playgroundState.dataset = dataset;
            parametersStateMock.visible = false;

            //when
            PlaygroundStateService.toggleDatasetParameters();

            //then
            expect(ParametersStateService.update).toHaveBeenCalledWith(dataset);
        }));
    });

    describe('filters', () => {
        describe('add', () => {
            it('should add filter in filter list', inject((PlaygroundStateService, FilterStateService) => {
                //given
                const filter = { column: '0001' };

                //when
                PlaygroundStateService.addGridFilter(filter);

                //then
                expect(FilterStateService.addGridFilter).toHaveBeenCalledWith(filter);
            }));

            it('should apply filters in grid', inject((playgroundState, PlaygroundStateService, GridStateService, FilterStateService) => {
                //given
                const filter = { column: '0001' };
                const filters = [{}, {}];
                const data = { records: [] };
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.addGridFilter(filter);

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
                expect(FilterStateService.enableFilters).toHaveBeenCalledWith();
            }));
        });

        describe('update', () => {
            it('should update filter in filter list', inject((PlaygroundStateService, FilterStateService) => {
                //given
                const oldFilter = { column: '0001' };
                const newFilter = { column: '0002' };

                //when
                PlaygroundStateService.updateGridFilter(oldFilter, newFilter);

                //then
                expect(FilterStateService.updateGridFilter).toHaveBeenCalledWith(oldFilter, newFilter);
            }));

            it('should apply filters in grid', inject((playgroundState, PlaygroundStateService, GridStateService, FilterStateService) => {
                //given
                const oldFilter = { column: '0001' };
                const newFilter = { column: '0002' };
                const filters = [{}, {}];
                const data = { records: [] };
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.updateGridFilter(oldFilter, newFilter);

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
                expect(FilterStateService.enableFilters).toHaveBeenCalledWith();
            }));
        });

        describe('remove', () => {
            it('should add remove from filter list', inject((PlaygroundStateService, FilterStateService) => {
                //given
                const filter = { column: '0001' };

                //when
                PlaygroundStateService.removeGridFilter(filter);

                //then
                expect(FilterStateService.removeGridFilter).toHaveBeenCalledWith(filter);
            }));

            it('should apply filters in grid on single remove', inject((playgroundState, PlaygroundStateService, GridStateService) => {
                //given
                const filter = { column: '0001' };
                const filters = [{}, {}];
                const data = { records: [] };
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.removeGridFilter(filter);

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
            }));

            it('should remove all filters from filter list', inject((PlaygroundStateService, FilterStateService) => {
                //when
                PlaygroundStateService.removeAllGridFilters();

                //then
                expect(FilterStateService.removeAllGridFilters).toHaveBeenCalled();
            }));

            it('should apply filters in grid on remove all', inject((playgroundState, PlaygroundStateService, GridStateService) => {
                //given
                const filters = [{}, {}];
                const data = { records: [] };
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.removeAllGridFilters();

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
            }));
        });

        describe('toogle', () => {
            it('should enable filters', inject((playgroundState, PlaygroundStateService, FilterStateService, GridStateService) => {
                //given
                const filters = [{}, {}];
                const data = { records: [] };
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.enableFilters();
                //then
                expect(FilterStateService.enableFilters).toHaveBeenCalled();
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filterStateMock.gridFilters, playgroundState.data);
            }));

            it('should disable filters', inject((playgroundState, PlaygroundStateService, FilterStateService, GridStateService) => {
                //given
                const filters = [{}, {}];
                const data = { records: [] };
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.disableFilters();

                //then
                expect(FilterStateService.disableFilters).toHaveBeenCalledWith();
                expect(GridStateService.setFilter).toHaveBeenCalledWith([], playgroundState.data);
            }));
        });
    });

    describe('reset', () => {
        it('should reset playground', inject((playgroundState, PlaygroundStateService) => {
            //given
            playgroundState.data = {};
            playgroundState.dataset = {};
            playgroundState.lookupData = {};
            playgroundState.nameEditionMode = true;
            playgroundState.preparation = {};
            playgroundState.isFetchingStats = true;
            playgroundState.isLoading = true;
            playgroundState.isSavingPreparation = true;
            playgroundState.isReadOnly = true;
            playgroundState.stepInEditionMode = {};
            playgroundState.isNameValidationVisible = true;
            playgroundState.isPreprationPickerVisible = true;
            playgroundState.savingPreparationFolders = {};
            playgroundState.isSavingPreparationFoldersLoading = true;

            //when
            PlaygroundStateService.reset();

            //then
            expect(playgroundState.data).toBe(null);
            expect(playgroundState.dataset).toBe(null);
            expect(playgroundState.nameEditionMode).toBe(false);
            expect(playgroundState.lookupData).toBe(null);
            expect(playgroundState.preparation).toBe(null);
            expect(playgroundState.isFetchingStats).toBe(false);
            expect(playgroundState.isLoading).toBe(false);
            expect(playgroundState.isSavingPreparation).toBe(false);
            expect(playgroundState.isReadOnly).toBe(false);
            expect(playgroundState.stepInEditionMode).toBe(null);
            expect(playgroundState.isNameValidationVisible).toBe(false);
            expect(playgroundState.isPreprationPickerVisible).toBe(false);
            expect(playgroundState.savingPreparationFolders).toBe(null);
            expect(playgroundState.isSavingPreparationFoldersLoading).toBe(false);
        }));

        it('should reset sub-states', inject((playgroundState, PlaygroundStateService, RecipeStateService, GridStateService, FilterStateService, LookupStateService, SuggestionsStateService, ParametersStateService) => {
            //given
            expect(RecipeStateService.reset).not.toHaveBeenCalled();
            expect(GridStateService.reset).not.toHaveBeenCalled();
            expect(FilterStateService.reset).not.toHaveBeenCalled();
            expect(LookupStateService.reset).not.toHaveBeenCalled();
            expect(SuggestionsStateService.reset).not.toHaveBeenCalled();
            expect(ParametersStateService.reset).not.toHaveBeenCalled();

            //when
            PlaygroundStateService.reset();

            //then
            expect(RecipeStateService.reset).toHaveBeenCalled();
            expect(GridStateService.reset).toHaveBeenCalled();
            expect(FilterStateService.reset).toHaveBeenCalled();
            expect(LookupStateService.reset).toHaveBeenCalled();
            expect(SuggestionsStateService.reset).toHaveBeenCalled();
            expect(ParametersStateService.reset).toHaveBeenCalled();
        }));
    });
});
