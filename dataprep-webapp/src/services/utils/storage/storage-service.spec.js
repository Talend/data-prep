describe('Storage service', function() {
    'use strict';

    beforeEach(module('data-prep.services.utils'));

    afterEach(inject(function($window) {
        $window.localStorage.clear();
    }));

    describe('aggregation', function() {
        it('should add aggregation in local storage', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();

            //when
            StorageService.setAggregation(datasetId, preparationId, columnId, aggregation);

            //then
            expect($window.localStorage.getItem(expectedKey)).toBe(JSON.stringify(aggregation));
        }));

        it('should generate key without dataset id', inject(function($window, StorageService) {
            //given
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation..72515d3212cf565b624.0001';
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();

            //when
            StorageService.setAggregation(null, preparationId, columnId, aggregation);

            //then
            expect($window.localStorage.getItem(expectedKey)).toBe(JSON.stringify(aggregation));
        }));

        it('should generate key without preparation id', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684..0001';
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();

            //when
            StorageService.setAggregation(datasetId, null, columnId, aggregation);

            //then
            expect($window.localStorage.getItem(expectedKey)).toBe(JSON.stringify(aggregation));
        }));

        it('should get aggregation from local storage', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            $window.localStorage.setItem(expectedKey, JSON.stringify(aggregation));

            //when
            var aggregationFromStorage = StorageService.getAggregation(datasetId, preparationId, columnId);

            //then
            expect(aggregationFromStorage).toEqual(aggregation);
        }));

        it('should remove aggregation from local storage', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            $window.localStorage.setItem(expectedKey, JSON.stringify(aggregation));

            //when
            StorageService.removeAggregation(datasetId, preparationId, columnId);

            //then
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();
        }));

        it('should remove all aggregations for the dataset/preparation from local storage', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var key1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            var key2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0002';
            var otherPreparationKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.f65412585ab156b4663.0002';
            $window.localStorage.setItem(key1, JSON.stringify(aggregation));
            $window.localStorage.setItem(key2, JSON.stringify(aggregation));
            $window.localStorage.setItem(otherPreparationKey, JSON.stringify(aggregation));

            //when
            StorageService.removeAllAggregations(datasetId, preparationId);

            //then
            expect($window.localStorage.getItem(key1)).toBeFalsy();
            expect($window.localStorage.getItem(key2)).toBeFalsy();
            expect($window.localStorage.getItem(otherPreparationKey)).toBeTruthy();
        }));

        it('should save all dataset aggregations for the preparation', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var aggregation1 = JSON.stringify({
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            });
            var aggregation2 = JSON.stringify({
                aggregation: 'SUM',
                aggregationColumnId: '0003'
            });
            var otherAggregation = JSON.stringify({
                aggregation: 'MIN',
                aggregationColumnId: '0003'
            });

            var key1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684..0001';
            var key2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684..0002';
            var otherKey = 'org.talend.dataprep.aggregation.9b87564ef564e651.56ef46541e32251a25.0002';
            $window.localStorage.setItem(key1, aggregation1);
            $window.localStorage.setItem(key2, aggregation2);
            $window.localStorage.setItem(otherKey, otherAggregation);

            //when
            StorageService.savePreparationAggregationsFromDataset(datasetId, preparationId);

            //then
            var prepKey1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            var prepKey2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0002';
            expect($window.localStorage.getItem(prepKey1)).toBe(aggregation1);
            expect($window.localStorage.getItem(prepKey2)).toBe(aggregation2);
        }));

        it('should move all preparation aggregations to the new preparation id', inject(function($window, StorageService) {
            //given
            var datasetId = '87a646f763bd545b684';
            var oldPreparationId = '72515d3212cf565b624';
            var newPreparationId = '8ef6254d6214554bb68';
            var aggregation1 = JSON.stringify({
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            });
            var aggregation2 = JSON.stringify({
                aggregation: 'SUM',
                aggregationColumnId: '0003'
            });
            var otherAggregation = JSON.stringify({
                aggregation: 'MIN',
                aggregationColumnId: '0003'
            });

            var oldPrepKey1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            var oldPrepKey2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0002';
            var otherKey = 'org.talend.dataprep.aggregation.9b87564ef564e651.56ef46541e32251a25.0002';
            $window.localStorage.setItem(oldPrepKey1, aggregation1);
            $window.localStorage.setItem(oldPrepKey2, aggregation2);
            $window.localStorage.setItem(otherKey, otherAggregation);

            //when
            StorageService.moveAggregations(datasetId, oldPreparationId, newPreparationId);

            //then
            var newPrepKey1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.8ef6254d6214554bb68.0001';
            var newPrepKey2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.8ef6254d6214554bb68.0002';
            expect($window.localStorage.getItem(oldPrepKey1)).toBeFalsy();
            expect($window.localStorage.getItem(oldPrepKey2)).toBeFalsy();
            expect($window.localStorage.getItem(newPrepKey1)).toBe(aggregation1);
            expect($window.localStorage.getItem(newPrepKey2)).toBe(aggregation2);
            expect($window.localStorage.getItem(otherKey)).toBe(otherAggregation);
        }));
    });
});