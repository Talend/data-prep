describe('Storage service', function() {
    'use strict';

    beforeEach(module('data-prep.services.utils'));

    afterEach(inject(function($window) {
        $window.localStorage.clear();
    }));

    describe('feedback', function() {

        it('should return feedback user mail in local storage', inject(function($window, StorageService) {
            //given
           var mail = 'abc@d.fr';
            var FEEDBACK_USER_MAIL_KEY = 'org.talend.dataprep.feedback_user_mail';

            expect(StorageService.getFeedbackUserMail()).toEqual('');

            $window.localStorage.setItem(FEEDBACK_USER_MAIL_KEY, JSON.stringify(mail));

            //when
            StorageService.getFeedbackUserMail(FEEDBACK_USER_MAIL_KEY);

            //then
            expect(StorageService.getFeedbackUserMail()).toEqual(mail);
        }));

        it('should save feedback user mail in local storage', inject(function($window, StorageService) {
            //given
            var mail = 'abc@d.fr';
            var FEEDBACK_USER_MAIL_KEY = 'org.talend.dataprep.feedback_user_mail';

            //when
            StorageService.saveFeedbackUserMail(mail);

            //then
            expect(JSON.parse($window.localStorage.getItem(FEEDBACK_USER_MAIL_KEY))).toEqual(mail);
        }));
    });
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

    describe('localStorage management', function() {
        var LOOKUP_DATASETS_KEY = 'org.talend.dataprep.lookup_datasets';
        var LOOKUP_DATASETS_SORT_KEY = 'org.talend.dataprep.lookup_datasets_sort';
        var LOOKUP_DATASETS_ORDER_KEY = 'org.talend.dataprep.lookup_datasets_order';
        var DATASETS_SORT_KEY = 'org.talend.dataprep.datasets.sort';
        var DATASETS_ORDER_KEY = 'org.talend.dataprep.datasets.order';

        it('should get Lookup Datasets', inject(function($window, StorageService) {

            //given
            expect(StorageService.getLookupDatasets()).toEqual([]);

            $window.localStorage.setItem(LOOKUP_DATASETS_KEY, JSON.stringify([{id :'abc'}]));

            //then
            expect(StorageService.getLookupDatasets()).toEqual([{id :'abc'}]);
        }));

        it('should save Lookup Datasets', inject(function($window, StorageService) {
            //when
            StorageService.setLookupDatasets([{id :'abc'}]);

            //then
            expect(JSON.parse($window.localStorage.getItem(LOOKUP_DATASETS_KEY))).toEqual([{id :'abc'}]);
        }));

        it('should get Lookup Datasets sort', inject(function($window, StorageService) {

            //given
            expect(StorageService.getLookupDatasetsSort()).toEqual(null);

            $window.localStorage.setItem(LOOKUP_DATASETS_SORT_KEY, 'name');

            //then
            expect(StorageService.getLookupDatasetsSort()).toEqual('name');
        }));

        it('should save Lookup Datasets sort', inject(function($window, StorageService) {
            //when
            StorageService.setLookupDatasetsSort('name');

            //then
            expect($window.localStorage.getItem(LOOKUP_DATASETS_SORT_KEY)).toEqual('name');
        }));

        it('should get Lookup Datasets order', inject(function($window, StorageService) {

            //given
            expect(StorageService.getLookupDatasetsOrder()).toEqual(null);

            $window.localStorage.setItem(LOOKUP_DATASETS_ORDER_KEY, 'desc');

            //then
            expect(StorageService.getLookupDatasetsOrder()).toEqual('desc');
        }));

        it('should save Lookup Datasets order', inject(function($window, StorageService) {
            //when
            StorageService.setLookupDatasetsOrder('desc');

            //then
            expect($window.localStorage.getItem(LOOKUP_DATASETS_ORDER_KEY)).toEqual('desc');
        }));

        it('should get Datasets sort', inject(function($window, StorageService) {

            //given
            expect(StorageService.getDatasetsSort()).toEqual(null);

            $window.localStorage.setItem(DATASETS_SORT_KEY, 'name');

            //then
            expect(StorageService.getDatasetsSort()).toEqual('name');
        }));

        it('should save Datasets sort', inject(function($window, StorageService) {
            //when
            StorageService.setDatasetsSort('name');

            //then
            expect($window.localStorage.getItem(DATASETS_SORT_KEY)).toEqual('name');
        }));

        it('should get Datasets order', inject(function($window, StorageService) {

            //given
            expect(StorageService.getDatasetsOrder()).toEqual(null);

            $window.localStorage.setItem(DATASETS_ORDER_KEY, 'desc');

            //then
            expect(StorageService.getDatasetsOrder()).toEqual('desc');
        }));

        it('should save Datasets order', inject(function($window, StorageService) {
            //when
            StorageService.setDatasetsOrder('desc');

            //then
            expect($window.localStorage.getItem(DATASETS_ORDER_KEY)).toEqual('desc');
        }));

    });
});