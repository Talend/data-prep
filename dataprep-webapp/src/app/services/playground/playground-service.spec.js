describe('Playground Service', () => {
    'use strict';

    const datasetColumnsWithoutStatistics = {
        columns: [{ id: '0001', statistics: { frequencyTable: [] } }],
        records: [],
        data: [],
    };
    const datasetColumns = {
        columns: [{ id: '0001', statistics: { frequencyTable: [{ 'toto': 2 }] } }],
        records: [],
        data: [],
    };
    const datasetMetadata = {
        records: 19,
        columns: [{ id: '0001', statistics: { frequencyTable: [{ 'toto': 2 }] } }],
    };

    const preparationMetadata = {
        metadata: {
            columns: [{ id: '0001', statistics: { frequencyTable: [{ 'toto': 2 }] } }]
        },
        records: [{}],
    };
    const preparationMetadataWithoutStatistics = {
        metadata: {
            columns: [{ id: '0001', statistics: { frequencyTable: [] } }]
        },
        records: [],
    };

    let createdPreparation;
    const stateMock = {};
    const lastActiveStep = { inactive: false };

    beforeEach(angular.mock.module('data-prep.services.playground', ($provide) => {
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            'PREPARATION': 'Preparation'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($q, $state, StateService, DatasetService, RecipeService, DatagridService,
                       PreparationService, TransformationCacheService,
                       HistoryService, PreviewService, ExportService) => {
        stateMock.playground = {
            preparationName: ''
        };
        createdPreparation = {
            id: '32cd7869f8426465e164ab85',
            name: 'created preparation name',
        };

        spyOn($state, 'go').and.returnValue();
        spyOn(DatagridService, 'updateData').and.returnValue();
        spyOn(DatasetService, 'getContent').and.returnValue($q.when(datasetColumns));
        spyOn(DatasetService, 'updateParameters').and.returnValue($q.when());
        spyOn(HistoryService, 'addAction').and.returnValue();
        spyOn(HistoryService, 'clear').and.returnValue();
        spyOn(PreparationService, 'create').and.returnValue($q.when(createdPreparation));
        spyOn(PreparationService, 'setHead').and.returnValue($q.when());
        spyOn(PreparationService, 'setName').and.returnValue($q.when(createdPreparation));
        spyOn(PreviewService, 'reset').and.returnValue();
        spyOn(RecipeService, 'disableStepsAfter').and.returnValue();
        spyOn(RecipeService, 'refresh').and.returnValue($q.when());
        spyOn(StateService, 'resetPlayground').and.returnValue();
        spyOn(StateService, 'setCurrentData').and.returnValue();
        spyOn(StateService, 'setCurrentDataset').and.returnValue();
        spyOn(StateService, 'setCurrentPreparation').and.returnValue();
        spyOn(StateService, 'setPreparationName').and.returnValue();
        spyOn(StateService, 'setNameEditionMode').and.returnValue();
        spyOn(StateService, 'showRecipe').and.returnValue();
        spyOn(StateService, 'hideRecipe').and.returnValue();
        spyOn(TransformationCacheService, 'invalidateCache').and.returnValue();
        spyOn(ExportService, 'reset').and.returnValue();
    }));

    describe('update preparation', () => {
        it('should set new name to the preparation name', inject(($rootScope, PlaygroundService, PreparationService, StateService) => {
            // given
            const name = 'My preparation';
            const newName = 'My new preparation name';

            PlaygroundService.preparationName = name;
            stateMock.playground.dataset = { id: '123d120394ab0c53' };
            stateMock.playground.preparation = { id: 'e85afAa78556d5425bc2' };

            // when
            PlaygroundService.createOrUpdatePreparation(newName);
            $rootScope.$digest();

            // then
            expect(PreparationService.create).not.toHaveBeenCalled();
            expect(PreparationService.setName).toHaveBeenCalledWith('e85afAa78556d5425bc2', newName);
            expect(StateService.setPreparationName).toHaveBeenCalledWith(createdPreparation.name);
        }));

        describe('history', () => {
            let undo, redo;
            const oldName = 'My preparation';
            const newName = 'My new preparation name';

            beforeEach(inject(($rootScope, PlaygroundService, HistoryService) => {
                // given
                PlaygroundService.preparationName = oldName;
                stateMock.playground.dataset = { id: '123d120394ab0c53' };
                stateMock.playground.preparation = { id: 'e85afAa78556d5425bc2', name: oldName };

                // when
                PlaygroundService.createOrUpdatePreparation(newName);
                $rootScope.$digest();

                // then
                undo = HistoryService.addAction.calls.argsFor(0)[0];
                redo = HistoryService.addAction.calls.argsFor(0)[1];
            }));

            it('should add undo/redo actions after append transformation', inject((HistoryService) => {
                // then
                expect(HistoryService.addAction).toHaveBeenCalled();
            }));

            it('should change preparation name on UNDO', inject((PreparationService) => {
                // given
                expect(PreparationService.setName.calls.count()).toBe(1);

                // when
                undo();

                // then
                expect(PreparationService.setName.calls.count()).toBe(2);
                expect(PreparationService.setName.calls.argsFor(1)).toEqual([stateMock.playground.preparation.id, oldName]);
            }));

            it('should change preparation name on REDO', inject((PreparationService) => {
                // given
                expect(PreparationService.setName.calls.count()).toBe(1);

                // when
                redo();

                // then
                expect(PreparationService.setName.calls.count()).toBe(2);
                expect(PreparationService.setName.calls.argsFor(1)).toEqual([stateMock.playground.preparation.id, newName]);
            }));
        });
    });

    describe('load dataset', () => {
        const dataset = {
            id: 'e85afAa78556d5425bc2',
            name: 'dataset name'
        };
        let assertNewPreparationInitialization;

        beforeEach(inject(($rootScope, RecipeService, TransformationCacheService,
                           HistoryService,
                           PreviewService, StateService, ExportService) => {
            spyOn($rootScope, '$emit').and.returnValue();

            assertNewPreparationInitialization = () => {
                expect(StateService.resetPlayground).toHaveBeenCalled();
                expect(StateService.setCurrentDataset).toHaveBeenCalledWith(dataset);
                expect(StateService.setCurrentData).toHaveBeenCalledWith(datasetColumns);
                expect(RecipeService.refresh).toHaveBeenCalled();
                expect(TransformationCacheService.invalidateCache).toHaveBeenCalled();
                expect(HistoryService.clear).toHaveBeenCalled();
                expect(PreviewService.reset).toHaveBeenCalledWith(false);
                expect(ExportService.reset).toHaveBeenCalled();
            };
        }));

        it('should init playground', inject(($rootScope, PlaygroundService, PreparationService) => {
            // given
            expect(PreparationService.preparationName).toBeFalsy();

            // when
            PlaygroundService.initPlayground(dataset);
            $rootScope.$digest();

            // then
            assertNewPreparationInitialization();
        }));

        it('should manage loading spinner', inject(($rootScope, PlaygroundService) => {
            // given
            expect($rootScope.$emit).not.toHaveBeenCalled();

            // when
            PlaygroundService.initPlayground(dataset);
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
            $rootScope.$digest();

            // then
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
        }));

        it('should reset preparation name', inject(($rootScope, PlaygroundService, StateService) => {
            // given
            PlaygroundService.preparationName = 'preparation name';

            // when
            PlaygroundService.initPlayground(dataset);
            $rootScope.$digest();

            // then
            expect(StateService.setPreparationName).toHaveBeenCalledWith(dataset.name);
        }));

        it('should start playground unboarding tour', inject(($rootScope, $timeout, PlaygroundService, OnboardingService) => {
            // given
            spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);
            spyOn(OnboardingService, 'startTour').and.returnValue();
            PlaygroundService.preparationName = 'preparation name';

            // when
            PlaygroundService.initPlayground(dataset);
            $rootScope.$digest();
            $timeout.flush(300);

            // then
            expect(OnboardingService.shouldStartTour).toHaveBeenCalledWith('playground');
            expect(OnboardingService.startTour).toHaveBeenCalledWith('playground');
        }));

        it('should NOT start playground unboarding tour', inject(($rootScope, $timeout, PlaygroundService, OnboardingService) => {
            // given
            spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false);
            spyOn(OnboardingService, 'startTour').and.returnValue();
            PlaygroundService.preparationName = 'preparation name';

            // when
            PlaygroundService.initPlayground(dataset);
            $rootScope.$digest();
            $timeout.flush(300);

            // then
            expect(OnboardingService.shouldStartTour).toHaveBeenCalledWith('playground');
            expect(OnboardingService.startTour).not.toHaveBeenCalled();
        }));

        it('should hide recipe', inject(($rootScope, PlaygroundService, StateService) => {
            // given
            const dataset = { id: '1' };
            expect(StateService.hideRecipe).not.toHaveBeenCalled();

            // when
            PlaygroundService.initPlayground(dataset);
            $rootScope.$digest();

            // then
            expect(StateService.hideRecipe).toHaveBeenCalled();
        }));
    });

    describe('load preparation', () => {
        const data = {
            columns: [{ id: '0001' }],
            records: [{ id: '0', firstname: 'toto' }, { id: '1', firstname: 'tata' }, { id: '2', firstname: 'titi' }],
        };
        let assertDatasetLoadInitialized, assertDatasetLoadNotInitialized;

        beforeEach(inject(($rootScope, $q, StateService,
                           PreparationService, RecipeService,
                           TransformationCacheService, HistoryService, PreviewService) => {
            spyOn($rootScope, '$emit').and.returnValue();
            spyOn(PreparationService, 'getContent').and.returnValue($q.when(data));

            assertDatasetLoadInitialized = (metadata, data) => {
                expect(StateService.resetPlayground).toHaveBeenCalled();
                expect(StateService.setCurrentDataset).toHaveBeenCalledWith(metadata);
                expect(StateService.setCurrentData).toHaveBeenCalledWith(data);
                expect(RecipeService.refresh).toHaveBeenCalled();
                expect(TransformationCacheService.invalidateCache).toHaveBeenCalled();
                expect(HistoryService.clear).toHaveBeenCalled();
                expect(PreviewService.reset).toHaveBeenCalledWith(false);
            };
            assertDatasetLoadNotInitialized = () => {
                expect(StateService.resetPlayground).not.toHaveBeenCalled();
                expect(StateService.setCurrentDataset).not.toHaveBeenCalled();
                expect(StateService.setCurrentData).not.toHaveBeenCalled();
                expect(RecipeService.refresh).not.toHaveBeenCalled();
                expect(TransformationCacheService.invalidateCache).not.toHaveBeenCalled();
                expect(HistoryService.clear).not.toHaveBeenCalled();
                expect(PreviewService.reset).not.toHaveBeenCalled();
            };
        }));

        it('should load existing preparation when it is not already loaded', inject(($rootScope, PlaygroundService) => {
            // given
            const preparation = {
                id: '6845521254541',
                dataset: { id: '1' }
            };
            stateMock.playground.preparation = { id: '5746518486846' };

            // when
            PlaygroundService.load(preparation);
            $rootScope.$apply();

            // then
            assertDatasetLoadInitialized(preparation.dataset, data);
        }));

        it('should manage loading spinner on preparation load', inject(($rootScope, PlaygroundService) => {
            // given
            const preparation = {
                id: '6845521254541',
                dataset: { id: '1' }
            };
            stateMock.playground.preparation = { id: '5746518486846' };

            // when
            PlaygroundService.load(preparation);
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
            $rootScope.$apply();

            // then
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
        }));

        it('should load existing preparation with simulated dataset metadata when its metadata is not set yet', inject(($rootScope, PlaygroundService) => {
            // given
            const preparation = {
                id: '6845521254541',
                dataSetId: '1'
            };
            stateMock.playground.preparation = { id: '5746518486846' };

            // when
            PlaygroundService.load(preparation);
            $rootScope.$apply();

            // then
            assertDatasetLoadInitialized({ id: '1' }, data);
        }));

        it('should NOT change playground if the preparation to load is already loaded', inject(($rootScope, PlaygroundService) => {
            // given
            const preparation = {
                id: '6845521254541',
                dataset: { id: '1', name: 'my dataset' }
            };
            stateMock.playground.dataset = {};
            stateMock.playground.preparation = preparation;

            // when
            PlaygroundService.load(preparation);
            $rootScope.$apply();

            // then
            assertDatasetLoadNotInitialized();
            expect($rootScope.$emit).not.toHaveBeenCalled();
        }));

        it('should load preparation content at a specific step', inject(($rootScope, StateService, PlaygroundService, RecipeService, DatagridService, PreviewService) => {
            // given
            const step = {
                column: { id: '0000' },
                transformation: { stepId: 'a4353089cb0e039ac2' }
            };
            const metadata = { id: '1', name: 'my dataset' };
            const preparation = { id: '2542154454' };
            stateMock.playground.dataset = metadata;
            stateMock.playground.preparation = preparation;

            // when
            PlaygroundService.loadStep(step);
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
            $rootScope.$apply();

            // then
            expect(StateService.resetPlayground).not.toHaveBeenCalled();
            expect(StateService.setCurrentDataset).not.toHaveBeenCalled();
            expect(RecipeService.refresh).not.toHaveBeenCalled();
            expect(RecipeService.disableStepsAfter).toHaveBeenCalledWith(step);
            expect(PreviewService.reset).toHaveBeenCalledWith(false);
            expect(DatagridService.updateData).toHaveBeenCalledWith(data);
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
        }));

        it('should show recipe', inject(($rootScope, PlaygroundService, StateService) => {
            // given
            expect(StateService.showRecipe).not.toHaveBeenCalled();
            const preparation = {
                id: '6845521254541',
                dataset: { id: '1' }
            };

            // when
            PlaygroundService.load(preparation);
            $rootScope.$digest();

            // then
            expect(StateService.showRecipe).toHaveBeenCalled();
        }));
    });

    describe('fetch dataset statistics', () => {
        beforeEach(inject((StateService, StatisticsService) => {
            spyOn(StatisticsService, 'updateStatistics').and.returnValue();
            spyOn(StateService, 'updateDatasetStatistics').and.returnValue();
            spyOn(StateService, 'updateDatasetRecord').and.returnValue();
        }));

        it('should get metadata and set its statistics in state', inject(($rootScope, $q, PlaygroundService, DatasetService, StateService) => {
            // given
            spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetMetadata));
            stateMock.playground.dataset = { id: '1324d56456b84ef154', records: 15 };
            stateMock.playground.preparation = null;

            // when
            PlaygroundService.updateStatistics();
            $rootScope.$digest();

            // then
            expect(DatasetService.getMetadata).toHaveBeenCalledWith('1324d56456b84ef154');
            expect(StateService.updateDatasetStatistics).toHaveBeenCalledWith(datasetMetadata);
            expect(StateService.updateDatasetRecord).toHaveBeenCalledWith(19);
        }));

        it('should trigger statistics update', inject(($rootScope, $q, DatasetService, PlaygroundService, StatisticsService) => {
            // given
            spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetMetadata));
            stateMock.playground.dataset = { id: '1324d56456b84ef154' };
            stateMock.playground.preparation = null;


            // when
            PlaygroundService.updateStatistics();
            $rootScope.$digest();

            // then
            expect(StatisticsService.updateStatistics).toHaveBeenCalled();
        }));

        it('should reject promise when the statistics are not computed yet', inject(($rootScope, $q, PlaygroundService, DatasetService, StateService) => {
            // given
            let rejected = false;
            spyOn(DatasetService, 'getMetadata').and.returnValue($q.when(datasetColumnsWithoutStatistics));
            stateMock.playground.dataset = { id: '1324d56456b84ef154' };
            stateMock.playground.preparation = null;

            // when
            PlaygroundService.updateStatistics()
                .catch(() => {
                    rejected = true;
                });
            $rootScope.$digest();

            // then
            expect(StateService.updateDatasetStatistics).not.toHaveBeenCalled();
            expect(rejected).toBe(true);
        }));
    });

    describe('fetch preparation statistics', () => {
        beforeEach(inject((StateService, StatisticsService) => {
            spyOn(StatisticsService, 'updateStatistics').and.returnValue();
            spyOn(StateService, 'updateDatasetStatistics').and.returnValue();
            spyOn(StateService, 'updateDatasetRecord').and.returnValue();

            stateMock.playground.preparation = { id: 'abc' };
            stateMock.playground.dataset = { id: '1324d56456b84ef154', records: 15 };
        }));

        it('should get metadata and set its statistics in state', inject(($rootScope, $q, PlaygroundService, PreparationService, StateService) => {
            // given
            spyOn(PreparationService, 'getContent').and.returnValue($q.when(preparationMetadata));

            // when
            PlaygroundService.updateStatistics();
            $rootScope.$digest();

            // then
            expect(PreparationService.getContent).toHaveBeenCalledWith('abc', 'head');
            expect(StateService.updateDatasetStatistics).toHaveBeenCalledWith(preparationMetadata.metadata);
            expect(StateService.updateDatasetRecord).toHaveBeenCalledWith(1);
        }));

        it('should trigger statistics update', inject(($rootScope, $q, PreparationService, PlaygroundService, StatisticsService) => {
            // given
            spyOn(PreparationService, 'getContent').and.returnValue($q.when(preparationMetadata));

            // when
            PlaygroundService.updateStatistics();
            $rootScope.$digest();

            // then
            expect(StatisticsService.updateStatistics).toHaveBeenCalled();
        }));

        it('should reject promise when the statistics are not computed yet', inject(($rootScope, $q, PlaygroundService, PreparationService, StateService) => {
            // given
            let rejected = false;
            spyOn(PreparationService, 'getContent').and.returnValue($q.when(preparationMetadataWithoutStatistics));

            // when
            PlaygroundService.updateStatistics()
                .catch(() => {
                    rejected = true;
                });
            $rootScope.$digest();

            // then
            expect(StateService.updateDatasetStatistics).not.toHaveBeenCalled();
            expect(rejected).toBe(true);
        }));
    });

    describe('transformation steps', () => {
        let preparationHeadContent, metadata;
        const lastStepId = 'a151e543456413ef51';
        const previousLastStepId = '3248fa65e45f588cb464';
        const lastStep = { transformation: { stepId: lastStepId } };
        const previousLastStep = { transformation: { stepId: previousLastStepId } };
        beforeEach(inject(($rootScope, $q, PlaygroundService, PreparationService, RecipeService) => {
            preparationHeadContent = {
                'records': [{
                    'firstname': 'Grover',
                    'avgAmount': '82.4',
                    'city': 'BOSTON',
                    'birth': '01-09-1973',
                    'registration': '17-02-2008',
                    'id': '1',
                    'state': 'AR',
                    'nbCommands': '41',
                    'lastname': 'Quincy'
                }, {
                    'firstname': 'Warren',
                    'avgAmount': '87.6',
                    'city': 'NASHVILLE',
                    'birth': '11-02-1960',
                    'registration': '18-08-2007',
                    'id': '2',
                    'state': 'WA',
                    'nbCommands': '17',
                    'lastname': 'Johnson'
                }]
            };

            metadata = { id: 'e85afAa78556d5425bc2' };
            stateMock.playground.dataset = metadata;

            spyOn($rootScope, '$emit').and.returnValue();
            spyOn(PreparationService, 'appendStep').and.returnValue($q.when());
            spyOn(PreparationService, 'updateStep').and.returnValue($q.when());
            spyOn(PreparationService, 'removeStep').and.returnValue($q.when());
            spyOn(PreparationService, 'getContent').and.returnValue($q.when(preparationHeadContent));
            spyOn(RecipeService, 'getLastStep').and.returnValue(lastStep);
            spyOn(RecipeService, 'getPreviousStep').and.callFake((step) => {
                return step === lastStep ? previousLastStep : null;
            });
        }));

        describe('append', () => {
            it('should create a preparation when there is no preparation yet', inject(($rootScope, PlaygroundService, PreparationService) => {
                // given
                stateMock.playground.dataset = {
                    id: '76a415cf854d8654',
                    name: 'my dataset name'
                };
                stateMock.playground.preparation = null;
                const action = 'uppercase';
                const parameters = {
                    param1: 'param1Value',
                    param2: 4,
                    scope: 'column',
                    column_id: '0001',
                    column_name: 'firstname'
                };

                expect(createdPreparation.draft).toBeFalsy();

                // when
                PlaygroundService.appendStep(action, parameters);
                stateMock.playground.preparation = createdPreparation; //emulate created preparation set in state
                $rootScope.$digest();

                // then
                expect(createdPreparation.draft).toBe(true);
                expect(PreparationService.create).toHaveBeenCalledWith('76a415cf854d8654', 'my dataset name Preparation');
            }));

            it('should append step to the new created preparation', inject(($rootScope, PlaygroundService, PreparationService) => {
                // given
                stateMock.playground.dataset = { id: '76a415cf854d8654' };
                stateMock.playground.preparation = null;
                const action = 'uppercase';
                const parameters = {
                    param1: 'param1Value',
                    param2: 4,
                    scope: 'column',
                    column_id: '0001',
                    column_name: 'firstname'
                };
                const actionParameters = {
                    action: action,
                    parameters: parameters
                };

                expect(createdPreparation.draft).toBeFalsy();

                // when
                PlaygroundService.appendStep(action, parameters);
                stateMock.playground.preparation = createdPreparation; //emulate created preparation set in state
                $rootScope.$digest();

                // then
                expect(PreparationService.appendStep).toHaveBeenCalledWith(createdPreparation.id, actionParameters);
            }));

            it('should append step to an existing preparation', inject(($rootScope, PlaygroundService, PreparationService) => {
                // given
                stateMock.playground.preparation = { id: '15de46846f8a46' };
                const action = 'uppercase';
                const parameters = {
                    param1: 'param1Value',
                    param2: 4,
                    scope: 'column',
                    column_id: '0001',
                    column_name: 'firstname'
                };
                const actionParameters = {
                    action: action,
                    parameters: parameters
                };

                // when
                PlaygroundService.appendStep(action, parameters);
                $rootScope.$digest();

                // then
                expect(PreparationService.appendStep).toHaveBeenCalledWith('15de46846f8a46', actionParameters);
            }));

            it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
                // given
                stateMock.playground.preparation = { id: '15de46846f8a46' };
                const action = 'uppercase';
                const parameters = {
                    param1: 'param1Value',
                    param2: 4,
                    scope: 'column',
                    column_id: '0001',
                    column_name: 'firstname'
                };

                // when
                PlaygroundService.appendStep(action, parameters);
                expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
                $rootScope.$digest();

                // then
                expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
            }));

            it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
                // given
                stateMock.playground.preparation = { id: '15de46846f8a46' };
                const action = 'uppercase';
                const parameters = {
                    param1: 'param1Value',
                    param2: 4,
                    scope: 'column',
                    column_id: '0001',
                    column_name: 'firstname'
                };

                // when
                PlaygroundService.appendStep(action, parameters);
                $rootScope.$digest();

                // then
                expect(RecipeService.refresh).toHaveBeenCalled();
            }));

            it('should refresh datagrid with head content', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
                // given
                stateMock.playground.preparation = { id: '15de46846f8a46' };
                const action = 'uppercase';
                const parameters = {
                    param1: 'param1Value',
                    param2: 4,
                    scope: 'column',
                    column_id: '0001',
                    column_name: 'firstname',
                };

                // when
                PlaygroundService.appendStep(action, parameters);
                $rootScope.$digest();

                // then
                expect(PreparationService.getContent).toHaveBeenCalledWith('15de46846f8a46', 'head');
                expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
                expect(PreviewService.reset).toHaveBeenCalledWith(false);
            }));

            describe('history', () => {
                let undo;
                const preparationId = '15de46846f8a46';

                beforeEach(inject((RecipeService) => {
                    spyOn(RecipeService, 'getLastActiveStep').and.returnValue(lastStep); //loaded step is the last step
                }));

                beforeEach(inject(($rootScope, PlaygroundService, HistoryService) => {
                    // given
                    stateMock.playground.preparation = { id: preparationId };
                    const action = 'uppercase';
                    const parameters = {
                        param1: 'param1Value',
                        param2: 4,
                        scope: 'column',
                        column_id: '0001',
                        column_name: 'firstname'
                    };
                    expect(HistoryService.addAction).not.toHaveBeenCalled();

                    // when
                    PlaygroundService.appendStep(action, parameters);
                    $rootScope.$digest();

                    // then
                    undo = HistoryService.addAction.calls.argsFor(0)[0];
                }));

                it('should add undo/redo actions after append transformation', inject((HistoryService) => {
                    // then
                    expect(HistoryService.addAction).toHaveBeenCalled();
                }));

                it('should set preparation head to previous head on UNDO', inject(($rootScope, PreparationService) => {
                    // given
                    expect(PreparationService.setHead).not.toHaveBeenCalled();

                    // when
                    undo();

                    // then
                    expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, previousLastStepId);
                }));

                it('should refresh recipe on UNDO', inject(($rootScope, DatagridService, RecipeService) => {
                    // given
                    expect(RecipeService.refresh.calls.count()).toBe(1);

                    // when
                    undo();
                    $rootScope.$digest();

                    // then
                    expect(RecipeService.refresh.calls.count()).toBe(2);
                }));

                it('should refresh datagrid content on UNDO', inject(($rootScope, PreparationService, DatagridService) => {
                    // given
                    expect(PreparationService.getContent.calls.count()).toBe(1);
                    expect(DatagridService.updateData.calls.count()).toBe(1);

                    // when
                    undo();
                    $rootScope.$digest();

                    // then
                    expect(PreparationService.getContent.calls.count()).toBe(2);
                    expect(PreparationService.getContent.calls.argsFor(1)[0]).toBe('15de46846f8a46');
                    expect(PreparationService.getContent.calls.argsFor(1)[1]).toBe('head');
                    expect(DatagridService.focusedColumn).toBeFalsy();
                    expect(DatagridService.updateData.calls.count()).toBe(2);
                    expect(DatagridService.updateData.calls.argsFor(1)[0]).toBe(preparationHeadContent);
                }));
            });
        });

        describe('update', () => {
            const lastActiveIndex = 5;
            const lastActiveStep = {
                column: { id: '0000' },
                transformation: { stepId: '24a457bc464e645' },
                actionParameters: {
                    action: 'touppercase'
                }
            };
            const oldParameters = { value: 'toto', column_id: '0001' };
            const stepToUpdate = {
                column: { id: '0001' },
                transformation: { stepId: '98a7565e4231fc2c7' },
                actionParameters: {
                    action: 'delete_on_value',
                    parameters: oldParameters
                }
            };

            beforeEach(inject((RecipeService) => {
                spyOn(RecipeService, 'getActiveThresholdStepIndex').and.returnValue(lastActiveIndex);
                spyOn(RecipeService, 'getStep').and.callFake((index) => {
                    if (index === lastActiveIndex) {
                        return lastActiveStep;
                    }
                    return stepToUpdate;
                });
            }));

            it('should not update preparation step when parameters are not changed', inject(($rootScope, PlaygroundService, PreparationService) => {
                // given
                stateMock.playground.preparation = { id: '456415ae348e6046dc' };
                const parameters = { value: 'toto', column_id: '0001' };

                // when
                PlaygroundService.updateStep(stepToUpdate, parameters);

                // then
                expect(PreparationService.updateStep).not.toHaveBeenCalled();
            }));

            it('should update preparation step when parameters are different', inject(($rootScope, PlaygroundService, PreparationService) => {
                // given
                stateMock.playground.preparation = { id: '456415ae348e6046dc' };
                const parameters = { value: 'tata', column_id: '0001' };

                // when
                PlaygroundService.updateStep(stepToUpdate, parameters);

                // then
                expect(PreparationService.updateStep).toHaveBeenCalledWith('456415ae348e6046dc', stepToUpdate, parameters);
            }));

            it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
                // given
                stateMock.playground.preparation = { id: '456415ae348e6046dc' };
                const parameters = { value: 'tata', column_id: '0001' };

                // when
                PlaygroundService.updateStep(stepToUpdate, parameters);
                expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
                $rootScope.$digest();

                // then
                expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
            }));

            it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
                // given
                stateMock.playground.preparation = { id: '456415ae348e6046dc' };
                const parameters = { value: 'tata', column_id: '0001' };

                // when
                PlaygroundService.updateStep(stepToUpdate, parameters);
                $rootScope.$digest();

                // then
                expect(RecipeService.refresh).toHaveBeenCalled();
            }));

            it('should load previous last active step', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
                // given
                stateMock.playground.preparation = { id: '456415ae348e6046dc' };
                const parameters = { value: 'tata', column_id: '0001' };

                // when
                PlaygroundService.updateStep(stepToUpdate, parameters);
                $rootScope.$digest();

                // then
                expect(PreparationService.getContent).toHaveBeenCalledWith('456415ae348e6046dc', lastActiveStep.transformation.stepId);
                expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
                expect(PreviewService.reset).toHaveBeenCalledWith(false);
            }));

            describe('history', () => {
                let undo;
                const preparationId = '456415ae348e6046dc';

                beforeEach(inject((RecipeService) => {
                    spyOn(RecipeService, 'getLastActiveStep').and.returnValue(lastActiveStep); //loaded step is not the last step
                }));

                beforeEach(inject(($rootScope, PlaygroundService, HistoryService) => {
                    // given
                    stateMock.playground.preparation = { id: preparationId };
                    const parameters = { value: 'tata', column_id: '0001' };

                    // when
                    PlaygroundService.updateStep(stepToUpdate, parameters);
                    $rootScope.$digest();

                    // then
                    undo = HistoryService.addAction.calls.argsFor(0)[0];
                }));

                it('should add undo/redo actions after update transformation', inject((HistoryService) => {
                    // then
                    expect(HistoryService.addAction).toHaveBeenCalled();
                }));

                it('should set preparation head to previous head on UNDO', inject(($rootScope, PreparationService) => {
                    // given
                    expect(PreparationService.setHead).not.toHaveBeenCalled();

                    // when
                    undo();

                    // then
                    expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, lastStepId);
                }));

                it('should refresh recipe on UNDO', inject(($rootScope, RecipeService) => {
                    // given
                    expect(RecipeService.refresh.calls.count()).toBe(1);

                    // when
                    undo();
                    $rootScope.$digest();

                    // then
                    expect(RecipeService.refresh.calls.count()).toBe(2);
                }));

                it('should refresh datagrid content at the last active step on UNDO', inject(($rootScope, PreparationService, DatagridService, RecipeService) => {
                    // given
                    expect(PreparationService.getContent.calls.count()).toBe(1);
                    expect(DatagridService.updateData.calls.count()).toBe(1);

                    // when
                    undo();
                    spyOn(RecipeService, 'getActiveThresholdStep').and.returnValue(); //emulate last active step different to the step to load
                    $rootScope.$digest();

                    // then
                    expect(PreparationService.getContent.calls.count()).toBe(2);
                    expect(PreparationService.getContent.calls.argsFor(1)[0]).toBe(preparationId);
                    expect(PreparationService.getContent.calls.argsFor(1)[1]).toBe(lastActiveStep.transformation.stepId);
                    expect(DatagridService.updateData.calls.count()).toBe(2);
                    expect(DatagridService.updateData.calls.argsFor(1)[0]).toBe(preparationHeadContent);
                }));
            });
        });

        describe('remove', () => {
            const stepToDeleteId = '98a7565e4231fc2c7';
            const stepToDelete = {
                column: { id: '0001' },
                transformation: { stepId: stepToDeleteId },
                actionParameters: {
                    action: 'delete_on_value',
                    parameters: { value: 'toto', column_id: '0001' }
                }
            };
            const preparationId = '43ab15436f12e3456';

            const allActionsFromStepToDelete = [
                { action: 'tolowercase', parameters: { column_id: '0003' } },
                { action: 'deleteempty', parameters: { column_id: '0003' } },
                { action: 'touppercase', parameters: { column_id: '0004' } }
            ];

            beforeEach(inject((RecipeService) => {
                spyOn(RecipeService, 'getAllActionsFrom').and.returnValue(allActionsFromStepToDelete);
            }));

            it('should remove preparation step', inject(($rootScope, PlaygroundService, PreparationService) => {
                // given
                stateMock.playground.preparation = { id: preparationId };

                // when
                PlaygroundService.removeStep(stepToDelete);

                // then
                expect(PreparationService.removeStep).toHaveBeenCalledWith(preparationId, stepToDeleteId);
            }));

            it('should show/hide loading', inject(($rootScope, PlaygroundService) => {
                // given
                stateMock.playground.preparation = { id: preparationId };

                // when
                PlaygroundService.removeStep(stepToDelete);
                expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
                $rootScope.$digest();

                // then
                expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
            }));

            it('should refresh recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
                // given
                stateMock.playground.preparation = { id: preparationId };

                // when
                PlaygroundService.removeStep(stepToDelete);
                $rootScope.$digest();

                // then
                expect(RecipeService.refresh).toHaveBeenCalled();
            }));

            it('should update datagrid', inject(($rootScope, PlaygroundService, PreparationService, DatagridService, PreviewService) => {
                // given
                stateMock.playground.preparation = { id: preparationId };

                // when
                PlaygroundService.removeStep(stepToDelete);
                $rootScope.$digest();

                // then
                expect(PreparationService.getContent).toHaveBeenCalledWith(preparationId, 'head');
                expect(DatagridService.focusedColumn).toBeFalsy();
                expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
                expect(PreviewService.reset).toHaveBeenCalledWith(false);
            }));

            describe('history', () => {
                let undo;

                beforeEach(inject((RecipeService) => {
                    spyOn(RecipeService, 'getLastActiveStep').and.returnValue(lastStep); //loaded step is the last step
                }));

                beforeEach(inject(($rootScope, HistoryService, PlaygroundService) => {
                    // given
                    stateMock.playground.preparation = { id: preparationId };
                    expect(HistoryService.addAction).not.toHaveBeenCalled();

                    // when
                    PlaygroundService.removeStep(stepToDelete);
                    $rootScope.$digest();

                    // then
                    undo = HistoryService.addAction.calls.argsFor(0)[0];
                }));

                it('should add undo/redo actions after remove transformation', inject((HistoryService) => {
                    // then
                    expect(HistoryService.addAction).toHaveBeenCalled();
                }));

                it('should set preparation head to previous head on UNDO', inject((PreparationService) => {
                    // given
                    expect(PreparationService.setHead).not.toHaveBeenCalled();

                    // when
                    undo();

                    // then
                    expect(PreparationService.setHead).toHaveBeenCalledWith(preparationId, lastStepId);
                }));

                it('should refresh recipe on UNDO', inject(($rootScope, RecipeService) => {
                    // given
                    expect(RecipeService.refresh.calls.count()).toBe(1);

                    // when
                    undo();
                    $rootScope.$digest();

                    // then
                    expect(RecipeService.refresh.calls.count()).toBe(2);
                }));

                it('should refresh datagrid content on UNDO', inject(($rootScope, PreparationService, DatagridService, PreviewService) => {
                    // when
                    undo();
                    $rootScope.$digest();

                    // then
                    expect(PreparationService.getContent).toHaveBeenCalledWith(preparationId, 'head');
                    expect(DatagridService.updateData).toHaveBeenCalledWith(preparationHeadContent);
                    expect(PreviewService.reset).toHaveBeenCalledWith(false);
                }));
            });
        });
    });

    describe('preparation name edition mode', () => {

        beforeEach(inject(($q, PreparationService, RecipeService) => {
            spyOn(PreparationService, 'getContent').and.returnValue($q.when({ columns: [{}] }));
            spyOn(PreparationService, 'appendStep').and.callFake(() => {
                RecipeService.getRecipe().push({});
                return $q.when(true);
            });
        }));

        it('should turn on edition mode on dataset playground init', inject(($rootScope, PlaygroundService, StateService) => {
            // given
            expect(StateService.setNameEditionMode).not.toHaveBeenCalled();
            const dataset = { id: '1' };

            // when
            PlaygroundService.initPlayground(dataset);
            $rootScope.$digest();

            // then
            expect(StateService.setNameEditionMode).toHaveBeenCalledWith(true);
        }));

        it('should turn off edition mode playground init', inject(($rootScope, PlaygroundService, StateService) => {
            // given
            expect(StateService.setNameEditionMode).not.toHaveBeenCalled();
            const preparation = {
                id: '6845521254541',
                dataset: { id: '1' }
            };

            // when
            PlaygroundService.load(preparation);
            $rootScope.$digest();

            // then
            expect(StateService.setNameEditionMode).toHaveBeenCalledWith(false);
        }));
    });

    describe('update preview', () => {
        beforeEach(inject(($q, RecipeService, PreviewService) => {
            spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when(true));
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue(lastActiveStep);
            const preparationId = '64f3543cd466f545';
            stateMock.playground.preparation = { id: preparationId };
        }));


        it('should call update preview', inject(($rootScope, PreviewService, PlaygroundService) => {
            // given
            $rootScope.$digest();
            const step = {
                column: { id: '0', name: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_id: '0', column_name: 'state', scope: 'column' }
                }
            };
            const parameters = { pattern: '--' };
            // when
            PlaygroundService.updatePreview(step, parameters);
            $rootScope.$digest();

            // then
            expect(PreviewService.getPreviewUpdateRecords).toHaveBeenCalledWith(
                stateMock.playground.preparation.id,
                lastActiveStep,
                step,
                { pattern: '--', column_id: '0', column_name: 'state', scope: 'column' });
        }));

        it('should do nothing on update preview if the step is inactive', inject(($rootScope, PreviewService, PlaygroundService) => {
            // given
            const step = {
                column: { id: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_name: 'state' }
                },
                inactive: true
            };
            const parameters = { pattern: '--' };

            // when
            PlaygroundService.updatePreview(step, parameters);
            $rootScope.$digest();

            // then
            expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
        }));

        it('should do nothing on update preview if the params have not changed', inject(($rootScope, PreviewService, PlaygroundService) => {
            // given
            const step = {
                column: { id: '0', name: 'state' },
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: { pattern: '.', column_id: '0', column_name: 'state' }
                }
            };
            const parameters = { pattern: '.' };

            // when
            PlaygroundService.updatePreview(step, parameters);
            $rootScope.$digest();

            // then
            expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
        }));
    });

    describe('dataset parameters', () => {
        let assertNewPlaygroundIsInitWith, assertPreparationStepIsLoadedWith;

        beforeEach(inject((StateService, RecipeService, TransformationCacheService, HistoryService, PreviewService, ExportService, DatagridService) => {
            assertNewPlaygroundIsInitWith = (dataset) => {
                expect(StateService.resetPlayground).toHaveBeenCalled();
                expect(StateService.setCurrentDataset).toHaveBeenCalledWith(dataset);
                expect(StateService.setCurrentData).toHaveBeenCalledWith(datasetColumns);
                expect(RecipeService.refresh).toHaveBeenCalled();
                expect(TransformationCacheService.invalidateCache).toHaveBeenCalled();
                expect(HistoryService.clear).toHaveBeenCalled();
                expect(PreviewService.reset).toHaveBeenCalledWith(false);
                expect(ExportService.reset).toHaveBeenCalled();
            };

            assertPreparationStepIsLoadedWith = (dataset, data, step) => {
                expect(DatagridService.updateData).toHaveBeenCalledWith(data);
                expect(RecipeService.disableStepsAfter).toHaveBeenCalledWith(step);
                expect(PreviewService.reset).toHaveBeenCalled();
            };
        }));

        it('should perform parameters update on current dataset', inject((PlaygroundService, DatasetService) => {
            // given
            const parameters = { separator: ';', encoding: 'UTF-8' };
            const dataset = { id: '32549c18046cd54b265' };
            stateMock.playground.dataset = dataset;
            stateMock.playground.preparation = null;

            expect(DatasetService.updateParameters).not.toHaveBeenCalled();

            // when
            PlaygroundService.changeDatasetParameters(parameters);

            // then
            expect(DatasetService.updateParameters).toHaveBeenCalledWith(dataset, parameters);
        }));

        it('should reinit playground with dataset after parameters update', inject(($rootScope, PlaygroundService) => {
            // given
            const parameters = { separator: ';', encoding: 'UTF-8' };
            const dataset = { id: '32549c18046cd54b265' };
            stateMock.playground.dataset = dataset;
            stateMock.playground.preparation = null;

            // when
            PlaygroundService.changeDatasetParameters(parameters);
            $rootScope.$digest();

            // then
            assertNewPlaygroundIsInitWith(dataset);
        }));

        it('should reinit playground with preparation at active step after parameters update', inject(($rootScope, $q, PlaygroundService, RecipeService, PreparationService) => {
            // given
            const parameters = { separator: ';', encoding: 'UTF-8' };
            const dataset = { id: '32549c18046cd54b265' };

            // given : state mock
            stateMock.playground.dataset = dataset;
            stateMock.playground.preparation = { id: '35d8cf964aa81b58' };

            // given : preparation content mock
            const data = { metadata: { columns: [] }, records: [] };
            spyOn(PreparationService, 'getContent').and.returnValue($q.when(data));

            // given : step mocks
            const step = { transformation: { stepId: '5874de8432c543' } };
            spyOn(RecipeService, 'getActiveThresholdStepIndex').and.returnValue(5);
            spyOn(RecipeService, 'getStep').and.callFake((index) => {
                if (index === 5) {
                    return step;
                }
                return null;
            });

            // when
            PlaygroundService.changeDatasetParameters(parameters);
            $rootScope.$digest();

            // then
            assertPreparationStepIsLoadedWith(dataset, data, step);
        }));
    });

    describe('on step append', () => {
        beforeEach(inject(($q, PreparationService, RecipeService) => {
            spyOn(PreparationService, 'getContent').and.returnValue($q.when({ columns: [{}] }));
            spyOn(PreparationService, 'appendStep').and.callFake(() => {
                RecipeService.getRecipe().push({});
                return $q.when();
            });
            spyOn(RecipeService, 'getLastStep').and.returnValue({
                transformation: { stepId: 'a151e543456413ef51' }
            });
            spyOn(RecipeService, 'getPreviousStep').and.returnValue({
                transformation: { stepId: '84f654a8e64fc5' }
            });
        }));

        describe('route change', () => {
            it('should change route to preparation route on first step', inject(($rootScope, $state, PlaygroundService) => {
                // given
                expect($state.go).not.toHaveBeenCalled();
                stateMock.playground.dataset = { id: '123456' };

                const action = 'uppercase';
                const column = { id: 'firstname' };
                const parameters = { param1: 'param1Value', param2: 4 };

                // when
                PlaygroundService.appendStep(action, column, parameters);
                stateMock.playground.preparation = createdPreparation;
                $rootScope.$digest();

                // then
                expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: createdPreparation.id });
            }));
        });

        describe('recipe display', () => {
            it('should show recipe on first step', inject(($rootScope, PlaygroundService, StateService) => {
                // given
                expect(StateService.showRecipe).not.toHaveBeenCalled();
                stateMock.playground.dataset = { id: '123456' };

                const action = 'uppercase';
                const column = { id: 'firstname' };
                const parameters = { param1: 'param1Value', param2: 4 };

                // when
                PlaygroundService.appendStep(action, column, parameters);
                stateMock.playground.preparation = createdPreparation;
                $rootScope.$digest();

                // then
                expect(StateService.showRecipe).toHaveBeenCalled();
            }));

            it('should NOT force recipe display on second step', inject(($rootScope, PlaygroundService, RecipeService, StateService) => {
                // given
                stateMock.playground.preparation = { id: '123456' };
                expect(StateService.showRecipe).not.toHaveBeenCalled();
                RecipeService.getRecipe().push({});

                const action = 'uppercase';
                const column = { id: 'firstname' };
                const parameters = { param1: 'param1Value', param2: 4 };

                // when
                PlaygroundService.appendStep(action, column, parameters);
                $rootScope.$digest();

                // then
                expect(StateService.showRecipe).not.toHaveBeenCalled();
            }));

            it('should show recipe and display onboarding on third step if the tour has not been completed yet', inject(($rootScope, $timeout, PlaygroundService, StateService, OnboardingService) => {
                // given
                stateMock.playground.dataset = { id: '123456' };
                spyOn(OnboardingService, 'startTour').and.returnValue();
                spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true); //not completed

                const action = 'uppercase';
                const column = { id: 'firstname' };
                const parameters = { param1: 'param1Value', param2: 4 };

                // given : first action call
                PlaygroundService.appendStep(action, column, parameters);
                stateMock.playground.preparation = createdPreparation;
                $rootScope.$digest();
                $timeout.flush(300);

                // given : second action call
                PlaygroundService.appendStep(action, column, parameters);
                $timeout.flush(300);
                $rootScope.$digest();

                expect(StateService.showRecipe.calls.count()).toBe(1); //called on 1st action
                expect(OnboardingService.startTour).not.toHaveBeenCalled();

                // when
                PlaygroundService.appendStep(action, column, parameters);
                $rootScope.$digest();
                $timeout.flush(300);

                // then
                expect(StateService.showRecipe.calls.count()).toBe(2);
                expect(OnboardingService.startTour).toHaveBeenCalled();
            }));

            it('should NOT show recipe and display onboarding on third step if the tour has already been completed', inject(($rootScope, $timeout, PlaygroundService, StateService, OnboardingService) => {
                // given
                stateMock.playground.dataset = { id: '123456' };
                spyOn(OnboardingService, 'startTour').and.returnValue();
                spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false); //already completed

                const action = 'uppercase';
                const column = { id: 'firstname' };
                const parameters = { param1: 'param1Value', param2: 4 };

                // given : first action call
                PlaygroundService.appendStep(action, column, parameters);
                stateMock.playground.preparation = createdPreparation;
                $rootScope.$digest();
                $timeout.flush(300);

                // given : second action call
                PlaygroundService.appendStep(action, column, parameters);
                $timeout.flush(300);
                $rootScope.$digest();

                expect(StateService.showRecipe.calls.count()).toBe(1); //called on 1st action
                expect(OnboardingService.startTour).not.toHaveBeenCalled();

                // when
                PlaygroundService.appendStep(action, column, parameters);
                $rootScope.$digest();
                $timeout.flush(300);

                // then
                expect(StateService.showRecipe.calls.count()).toBe(1);
                expect(OnboardingService.startTour).not.toHaveBeenCalled();
            }));
        })
    });

    describe('copy steps', () => {
        beforeEach(inject(($rootScope, $q, PreparationService) => {
            spyOn(PreparationService, 'copySteps').and.returnValue($q.when());
            spyOn(PreparationService, 'getContent').and.returnValue($q.when());
            spyOn($rootScope, '$emit').and.returnValue($q.when());
        }));

        it('should copy steps', inject(($rootScope, PlaygroundService, PreparationService) => {
            // given
            stateMock.playground.preparation = { id: '79db821355a65cd96' };
            expect(PreparationService.copySteps).not.toHaveBeenCalled();

            // when
            PlaygroundService.copySteps('13cf24597f9b6ba542');
            $rootScope.$digest();

            // then
            expect(PreparationService.copySteps).toHaveBeenCalledWith('79db821355a65cd96', '13cf24597f9b6ba542');
        }));

        it('should update recipe', inject(($rootScope, PlaygroundService, RecipeService) => {
            // given
            stateMock.playground.preparation = { id: '79db821355a65cd96' };
            expect(RecipeService.refresh).not.toHaveBeenCalled();

            // when
            PlaygroundService.copySteps('13cf24597f9b6ba542');
            $rootScope.$digest();

            // then
            expect(RecipeService.refresh).toHaveBeenCalled();
        }));

        it('should update datagrid', inject(($rootScope, PlaygroundService, PreparationService) => {
            // given
            stateMock.playground.preparation = { id: '79db821355a65cd96' };
            expect(PreparationService.getContent).not.toHaveBeenCalled();

            // when
            PlaygroundService.copySteps('13cf24597f9b6ba542');
            $rootScope.$digest();

            // then
            expect(PreparationService.getContent).toHaveBeenCalledWith('79db821355a65cd96', 'head');
        }));

        it('should manage loader', inject(($rootScope, PlaygroundService) => {
            // given
            stateMock.playground.preparation = { id: '79db821355a65cd96' };
            expect($rootScope.$emit).not.toHaveBeenCalled();

            // when
            PlaygroundService.copySteps('13cf24597f9b6ba542');
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
            $rootScope.$digest();

            // then
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
        }));
    });
});
