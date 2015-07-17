/*jshint camelcase: false */

describe('Preparation Service', function () {
    'use strict';

    //var promiseWithProgress;
    var preparationConsolidation, datasetConsolidation;
    var datasets = [{name: 'my dataset'}, {name: 'my second dataset'}, {name: 'my second dataset (1)'}, {name: 'my second dataset (2)'}];
    var preparations = [{id: '4385fa764bce39593a405d91bc88'}, {id: '58444bce39593a405d9456'}, {id: '2545764bce39593a405d91bc8673'}];
    var newPreparationId = '6cd546546548a745';

    beforeEach(module('data-prep.services.preparation'));

    beforeEach(inject(function($q, DatasetListService, PreparationListService, PreparationRestService) {
        preparationConsolidation = $q.when(true);
        datasetConsolidation = $q.when(datasets);

        spyOn(DatasetListService, 'refreshDefaultPreparation').and.returnValue(datasetConsolidation);
        spyOn(PreparationListService, 'refreshMetadataInfos').and.returnValue(preparationConsolidation);

        spyOn(PreparationListService, 'refreshPreparations').and.returnValue($q.when(preparations));
        spyOn(PreparationListService, 'create').and.returnValue($q.when({data: newPreparationId}));
        spyOn(PreparationListService, 'update').and.returnValue($q.when(true));
        spyOn(PreparationListService, 'delete').and.returnValue($q.when(true));

        spyOn(PreparationRestService, 'updateStep').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'appendStep').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getContent').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getDetails').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewDiff').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewUpdate').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'removeStep').and.returnValue($q.when(true));
    }));

    afterEach(inject(function(PreparationListService) {
        PreparationListService.preparations = null;
    }));

    describe('getter/refresher', function() {
        it('should return preparation list from ListService', inject(function (PreparationService, PreparationListService) {
            //given
            PreparationListService.preparations = preparations;

            //when
            var result = PreparationService.preparationsList();

            //then
            expect(result).toBe(preparations);
        }));

        it('should refresh preparations', inject(function ($rootScope, PreparationService, PreparationListService) {
            //when
            PreparationService.refreshPreparations();
            $rootScope.$digest();

            //then
            expect(PreparationListService.refreshPreparations).toHaveBeenCalled();
        }));

        it('should return a promise resolving preparations', inject(function ($rootScope, PreparationService) {
            //given
            var result = null;

            //when
            PreparationService.refreshPreparations()
                .then(function(promiseResult) {
                    result = promiseResult;
                });
            $rootScope.$digest();

            //then
            expect(result).toBe(preparations);
        }));

        it('should consolidate preparations and datasets on refresh', inject(function ($rootScope, PreparationService, PreparationListService, DatasetListService) {
            //when
            PreparationService.refreshPreparations();
            PreparationListService.preparations = preparations; //simulate preparations list update
            $rootScope.$digest();

            //then
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
        }));

        it('should not refresh but return a promise resolving existing preparations if they are already fetched', inject(function ($rootScope, PreparationService, PreparationListService) {
            //given
            PreparationListService.preparations = preparations;
            var result = null;

            //when
            PreparationService.getPreparations()
                .then(function(promiseResult) {
                    result = promiseResult;
                });
            $rootScope.$digest();

            //then
            expect(result).toBe(preparations);
        }));

        it('should not consolidate preparations and datasets when preparations has not been fetched', inject(function ($rootScope, PreparationService, PreparationListService, DatasetListService) {
            //given
            PreparationListService.preparations = preparations;

            //when
            PreparationService.getPreparations();
            $rootScope.$digest();

            //then
            expect(DatasetListService.refreshDefaultPreparation).not.toHaveBeenCalled();
            expect(PreparationListService.refreshMetadataInfos).not.toHaveBeenCalled();
        }));

        it('should fetch preparations if they are not already fetched', inject(function ($rootScope, PreparationService, PreparationListService) {
            //given
            PreparationListService.preparations = null;
            var result = null;

            //when
            PreparationService.getPreparations()
                .then(function(promiseResult) {
                    result = promiseResult;
                });
            $rootScope.$digest();

            //then
            expect(result).toBe(preparations);
        }));

        it('should consolidate preparations and datasets on refresh', inject(function ($rootScope, PreparationService, PreparationListService, DatasetListService) {
            //given
            PreparationListService.preparations = null;

            //when
            PreparationService.getPreparations();
            PreparationListService.preparations = preparations; //simulate preparations update
            $rootScope.$digest();

            //then
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
        }));
    });

    describe('details/content', function() {
        it('should get current preparation content from ListService', inject(function ($rootScope, PreparationService, PreparationRestService) {
            //given
            var version = 'head';
            PreparationService.currentPreparationId = '4385fa764bce39593a405d91bc88';

            //when
            PreparationService.getContent(version);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.getContent).toHaveBeenCalledWith('4385fa764bce39593a405d91bc88', version);
        }));

        it('should get current preparation details from ListService', inject(function ($rootScope, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '4385fa764bce39593a405d91bc88';

            //when
            PreparationService.getDetails();
            $rootScope.$digest();

            //then
            expect(PreparationRestService.getDetails).toHaveBeenCalledWith('4385fa764bce39593a405d91bc88');
        }));
    });

    describe('lifecycle', function() {
        it('should create a new preparation', inject(function ($rootScope, PreparationService, PreparationListService) {
            //given
            PreparationService.currentPreparationId = null;
            var metadata = {id: '2430e5df845ab6034c85'};
            var name = 'my preparation';

            //when
            PreparationService.create(metadata, name);
            $rootScope.$digest();

            //then
            expect(PreparationListService.create).toHaveBeenCalledWith('2430e5df845ab6034c85', 'my preparation');
        }));

        it('should consolidate preparations and datasets on creation', inject(function ($rootScope, PreparationService, PreparationListService, DatasetListService) {
            //given
            PreparationListService.preparations = preparations;
            PreparationService.currentPreparationId = null;

            //when
            PreparationService.create({id: '2430e5df845ab6034c85'}, 'my preparation');
            $rootScope.$digest();

            //then
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
        }));

        it('should keep created preparation id as current preparation', inject(function ($rootScope, PreparationService) {
            //given
            PreparationService.currentPreparationId = null;

            //when
            PreparationService.create({id: '2430e5df845ab6034c85'}, 'my preparation');
            $rootScope.$digest();

            //then
            expect(PreparationService.currentPreparationId).toBe(newPreparationId);
        }));

        it('should update current preparation name', inject(function ($rootScope, PreparationService, PreparationListService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var name = 'my preparation';

            //when
            PreparationService.setName(null, name);
            $rootScope.$digest();

            //then
            expect(PreparationListService.update).toHaveBeenCalledWith('6cd546546548a745', 'my preparation');
        }));

        it('should consolidate preparations and datasets on name update', inject(function ($rootScope, PreparationService, PreparationListService, DatasetListService) {
            //given
            PreparationListService.preparations = preparations;
            PreparationService.currentPreparationId = '6cd546546548a745';
            var name = 'my preparation';

            //when
            PreparationService.setName(name);
            $rootScope.$digest();

            //then
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
        }));

        it('should create a new dataset with provided name if no current preparation is loaded', inject(function ($rootScope, PreparationService, PreparationListService) {
            //given
            PreparationService.currentPreparationId = null;
            var metadata = {id: '2430e5df845ab6034c85'};
            var name = 'my preparation';

            //when
            PreparationService.setName(metadata, name);
            $rootScope.$digest();

            //then
            expect(PreparationListService.create).toHaveBeenCalledWith('2430e5df845ab6034c85', 'my preparation');
        }));

        it('should delete a preparation', inject(function ($rootScope, PreparationService, PreparationListService) {
            //when
            PreparationService.delete(preparations[0]);
            $rootScope.$digest();

            //then
            expect(PreparationListService.delete).toHaveBeenCalledWith(preparations[0]);
        }));

        it('should consolidate preparations and datasets on deletion', inject(function ($rootScope, PreparationService, PreparationListService, DatasetListService) {
            //given
            PreparationListService.preparations = preparations;

            //when
            PreparationService.delete(preparations[0]);
            $rootScope.$digest();

            //then
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
        }));

        it('should delete current preparation', inject(function(PreparationListService, PreparationService) {
            //given
            var preparationToDelete = preparations[2];
            PreparationService.currentPreparationId = preparationToDelete.id;
            PreparationListService.preparations = preparations;

            //when
            PreparationService.deleteCurrentPreparation();

            //then
            expect(PreparationListService.delete).toHaveBeenCalledWith(preparationToDelete);
        }));
    });

    describe('steps', function() {
        it('should delete step', inject(function ($q, PreparationService, PreparationRestService) {
            //given
            var preparationId = '6cd546546548a745';
            var stepId = '45ed65cf48981b51';
            PreparationService.currentPreparationId = preparationId;

            //when
            PreparationService.removeStep(stepId);

            //then
            expect(PreparationRestService.removeStep).toHaveBeenCalledWith(preparationId, stepId);
        }));

        it('should update a preparation step with completed parameters (add column id)', inject(function ($rootScope, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var step = {
                transformation: {
                    stepId : '867654ab15edf576844c4',
                    name: 'deletematch'
                },
                column: {id: '1', name:'firstname'}
            };
            var parameters = {value: 'Toto'};

            //when
            PreparationService.updateStep(step, parameters);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.updateStep).toHaveBeenCalledWith(
                '6cd546546548a745', //prep id
                '867654ab15edf576844c4',  //step id
                'deletematch', //step name
                {value: 'Toto', column_name: 'firstname', column_id: '1'}); //params
        }));

        it('should update a preparation step without parameters (should create it with column id)', inject(function ($rootScope, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var step = {
                transformation: {
                    stepId : '867654ab15edf576844c4',
                    name: 'touppercase'
                },
                column: {id: '1', name:'firstname'}
            };

            //when
            PreparationService.updateStep(step);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.updateStep).toHaveBeenCalledWith(
                '6cd546546548a745', //prep id
                '867654ab15edf576844c4',  //step id
                'touppercase', //step name
                {column_name: 'firstname', column_id: '1'}); //params
        }));

        it('should append step to current preparation with completed parameters (add column id)', inject(function ($rootScope, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var metadata = {id: '2430e5df845ab6034c85'};
            var action = 'cut';
            var column = {id: '1', name: 'firstname'};
            var parameters = {value: 'Toto'};

            //when
            PreparationService.appendStep(metadata, action, column, parameters);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.appendStep).toHaveBeenCalledWith('6cd546546548a745', 'cut', {value: 'Toto', column_name: 'firstname', column_id: '1'});
        }));

        it('should append step to current preparation without parameters (should create it with column id)', inject(function ($rootScope, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var metadata = {id: '2430e5df845ab6034c85'};
            var action = 'uppercase';
            var column = {id: '1', name: 'firstname'};

            //when
            PreparationService.appendStep(metadata, action, column);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.appendStep).toHaveBeenCalledWith('6cd546546548a745', 'uppercase', {column_name: 'firstname', column_id: '1'});
        }));

        it('should create a new preparation with generic name on append step if no preparation is loaded', inject(function ($rootScope, PreparationService, PreparationListService) {
            //given
            PreparationService.currentPreparationId = null;
            var metadata = {id: '2430e5df845ab6034c85'};
            var action = 'uppercase';
            var column = {id: 'firstname'};

            //when
            PreparationService.appendStep(metadata, action, column);
            $rootScope.$digest();

            //then
            expect(PreparationListService.create).toHaveBeenCalledWith('2430e5df845ab6034c85', 'Preparation draft');
        }));

        it('should complete parameters and return true if the parameters are different', inject(function (PreparationService) {
            //given
            var step = {
                column: {
                    id: '1',
                    name: 'firstname'
                },
                actionParameters: {
                    parameters: {value: '--', column_name: 'firstname', column_id: '1'}
                }
            };
            var newParams = {value: '.'};

            //when
            var result = PreparationService.paramsHasChanged(step, newParams);

            //then
            expect(result).toBe(true);
        }));

        it('should complete parameters and return false if the parameters are the same', inject(function (PreparationService) {
            //given
            var step = {
                column: {
                    id: '1',
                    name: 'firstname'
                },
                actionParameters: {
                    parameters: {value: '--', column_id: '1', column_name: 'firstname'}
                }
            };
            var newParams = {value: '--'};

            //when
            var result = PreparationService.paramsHasChanged(step, newParams);

            //then
            expect(result).toBe(false);
        }));

        it('should create parameters and return false when step has no parameters', inject(function (PreparationService) {
            //given
            var step = {
                column: {
                    id: '0',
                    name: 'firstname'
                },
                actionParameters: {
                    parameters: {column_id: '0', column_name: 'firstname'}
                }
            };

            //when
            var result = PreparationService.paramsHasChanged(step, null);

            //then
            expect(result).toBe(false);
        }));
    });

    describe('preview', function() {
        it('should get diff preview', inject(function ($q, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var currentStep = {id: '86574251524'};
            var previewStep = {id: '65487874887'};
            var recordsTdpId = [1,2,3];
            var canceler = $q.defer();

            //when
            PreparationService.getPreviewDiff(currentStep, previewStep, recordsTdpId, canceler);

            //then
            expect(PreparationRestService.getPreviewDiff).toHaveBeenCalledWith('6cd546546548a745', currentStep, previewStep, recordsTdpId, canceler);
        }));

        it('should get diff preview', inject(function ($q, PreparationService, PreparationRestService) {
            //given
            PreparationService.currentPreparationId = '6cd546546548a745';
            var currentStep = {id: '86574251524'};
            var updateStep = {id: '65487874887'};
            var newParams = {value: 'toto'};
            var recordsTdpId = [1,2,3];
            var canceler = $q.defer();

            //when
            PreparationService.getPreviewUpdate(currentStep, updateStep, newParams, recordsTdpId, canceler);

            //then
            expect(PreparationRestService.getPreviewUpdate).toHaveBeenCalledWith('6cd546546548a745', currentStep, updateStep, newParams, recordsTdpId, canceler);
        }));
    });

});