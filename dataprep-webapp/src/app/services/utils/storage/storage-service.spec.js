/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Storage service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    afterEach(inject(($window) => {
        $window.localStorage.clear();
    }));

    describe('feedback', () => {
        const FEEDBACK_USER_MAIL_KEY = 'org.talend.dataprep.feedback_user_mail';

        it('should return feedback user mail in local storage', inject(($window, StorageService) => {
            // given
            expect(StorageService.getFeedbackUserMail()).toEqual('');
            $window.localStorage.setItem(FEEDBACK_USER_MAIL_KEY, '"abc@d.fr"');

            // when
            const email = StorageService.getFeedbackUserMail();

            // then
            expect(email).toEqual('abc@d.fr');
        }));

        it('should save feedback user mail in local storage', inject(($window, StorageService) => {
            // when
            StorageService.saveFeedbackUserMail('abc@d.fr');

            // then
            expect($window.localStorage.getItem(FEEDBACK_USER_MAIL_KEY)).toEqual('"abc@d.fr"');
        }));
    });

    describe('aggregation', () => {
        it('should add aggregation in local storage', inject(($window, StorageService) => {
            // given
            const datasetId = '87a646f763bd545b684';
            const preparationId = '72515d3212cf565b624';
            const columnId = '0001';
            const aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            const expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();

            // when
            StorageService.setAggregation(datasetId, preparationId, columnId, aggregation);

            // then
            expect($window.localStorage.getItem(expectedKey)).toBe(JSON.stringify(aggregation));
        }));

        it('should generate key without dataset id', inject(($window, StorageService) => {
            // given
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation..72515d3212cf565b624.0001';
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();

            // when
            StorageService.setAggregation(null, preparationId, columnId, aggregation);

            // then
            expect($window.localStorage.getItem(expectedKey)).toBe(JSON.stringify(aggregation));
        }));

        it('should generate key without preparation id', inject(($window, StorageService) => {
            // given
            var datasetId = '87a646f763bd545b684';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684..0001';
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();

            // when
            StorageService.setAggregation(datasetId, null, columnId, aggregation);

            // then
            expect($window.localStorage.getItem(expectedKey)).toBe(JSON.stringify(aggregation));
        }));

        it('should get aggregation from local storage', inject(($window, StorageService) => {
            // given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            $window.localStorage.setItem(expectedKey, JSON.stringify(aggregation));

            // when
            var aggregationFromStorage = StorageService.getAggregation(datasetId, preparationId, columnId);

            // then
            expect(aggregationFromStorage).toEqual(aggregation);
        }));

        it('should remove aggregation from local storage', inject(($window, StorageService) => {
            // given
            var datasetId = '87a646f763bd545b684';
            var preparationId = '72515d3212cf565b624';
            var columnId = '0001';
            var aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            var expectedKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            $window.localStorage.setItem(expectedKey, JSON.stringify(aggregation));

            // when
            StorageService.removeAggregation(datasetId, preparationId, columnId);

            // then
            expect($window.localStorage.getItem(expectedKey)).toBeFalsy();
        }));

        it('should remove all aggregations for the dataset/preparation from local storage', inject(($window, StorageService) => {
            // given
            const datasetId = '87a646f763bd545b684';
            const preparationId = '72515d3212cf565b624';
            const aggregation = {
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            };

            const key1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            const key2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0002';
            const otherPreparationKey = 'org.talend.dataprep.aggregation.87a646f763bd545b684.f65412585ab156b4663.0002';
            $window.localStorage.setItem(key1, JSON.stringify(aggregation));
            $window.localStorage.setItem(key2, JSON.stringify(aggregation));
            $window.localStorage.setItem(otherPreparationKey, JSON.stringify(aggregation));

            // when
            StorageService.removeAllAggregations(datasetId, preparationId);

            // then
            expect($window.localStorage.getItem(key1)).toBeFalsy();
            expect($window.localStorage.getItem(key2)).toBeFalsy();
            expect($window.localStorage.getItem(otherPreparationKey)).toBeTruthy();
        }));

        it('should save all dataset aggregations for the preparation', inject(($window, StorageService) => {
            // given
            const datasetId = '87a646f763bd545b684';
            const preparationId = '72515d3212cf565b624';
            const aggregation1 = JSON.stringify({
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            });
            const aggregation2 = JSON.stringify({
                aggregation: 'SUM',
                aggregationColumnId: '0003'
            });
            const otherAggregation = JSON.stringify({
                aggregation: 'MIN',
                aggregationColumnId: '0003'
            });

            const key1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684..0001';
            const key2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684..0002';
            const otherKey = 'org.talend.dataprep.aggregation.9b87564ef564e651.56ef46541e32251a25.0002';
            $window.localStorage.setItem(key1, aggregation1);
            $window.localStorage.setItem(key2, aggregation2);
            $window.localStorage.setItem(otherKey, otherAggregation);

            // when
            StorageService.savePreparationAggregationsFromDataset(datasetId, preparationId);

            // then
            const prepKey1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            const prepKey2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0002';
            expect($window.localStorage.getItem(prepKey1)).toBe(aggregation1);
            expect($window.localStorage.getItem(prepKey2)).toBe(aggregation2);
        }));

        it('should move all preparation aggregations to the new preparation id', inject(($window, StorageService) => {
            // given
            const datasetId = '87a646f763bd545b684';
            const oldPreparationId = '72515d3212cf565b624';
            const newPreparationId = '8ef6254d6214554bb68';
            const aggregation1 = JSON.stringify({
                aggregation: 'MAX',
                aggregationColumnId: '0002'
            });
            const aggregation2 = JSON.stringify({
                aggregation: 'SUM',
                aggregationColumnId: '0003'
            });
            const otherAggregation = JSON.stringify({
                aggregation: 'MIN',
                aggregationColumnId: '0003'
            });

            const oldPrepKey1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0001';
            const oldPrepKey2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.72515d3212cf565b624.0002';
            const otherKey = 'org.talend.dataprep.aggregation.9b87564ef564e651.56ef46541e32251a25.0002';
            $window.localStorage.setItem(oldPrepKey1, aggregation1);
            $window.localStorage.setItem(oldPrepKey2, aggregation2);
            $window.localStorage.setItem(otherKey, otherAggregation);

            // when
            StorageService.moveAggregations(datasetId, oldPreparationId, newPreparationId);

            // then
            const newPrepKey1 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.8ef6254d6214554bb68.0001';
            const newPrepKey2 = 'org.talend.dataprep.aggregation.87a646f763bd545b684.8ef6254d6214554bb68.0002';
            expect($window.localStorage.getItem(oldPrepKey1)).toBeFalsy();
            expect($window.localStorage.getItem(oldPrepKey2)).toBeFalsy();
            expect($window.localStorage.getItem(newPrepKey1)).toBe(aggregation1);
            expect($window.localStorage.getItem(newPrepKey2)).toBe(aggregation2);
            expect($window.localStorage.getItem(otherKey)).toBe(otherAggregation);
        }));
    });

    describe('lookup', () => {
        const LOOKUP_DATASETS_KEY = 'org.talend.dataprep.lookup_datasets';
        const LOOKUP_DATASETS_SORT_KEY = 'org.talend.dataprep.lookup_datasets_sort';
        const LOOKUP_DATASETS_ORDER_KEY = 'org.talend.dataprep.lookup_datasets_order';

        it('should get Lookup Datasets', inject(($window, StorageService) => {
            // given
            expect(StorageService.getLookupDatasets()).toEqual([]);
            $window.localStorage.setItem(LOOKUP_DATASETS_KEY, JSON.stringify([{ id: 'abc' }]));

            // when
            const datasets = StorageService.getLookupDatasets();

            // then
            expect(datasets).toEqual([{ id: 'abc' }]);
        }));

        it('should save Lookup Datasets', inject(($window, StorageService) => {
            // when
            StorageService.setLookupDatasets([{ id: 'abc' }]);

            // then
            expect(JSON.parse($window.localStorage.getItem(LOOKUP_DATASETS_KEY))).toEqual([{ id: 'abc' }]);
        }));

        it('should get Lookup Datasets sort', inject(($window, StorageService) => {
            // given
            expect(StorageService.getLookupDatasetsSort()).toBeUndefined();
            $window.localStorage.setItem(LOOKUP_DATASETS_SORT_KEY, '"name"');

            // when
            const sort = StorageService.getLookupDatasetsSort();

            // then
            expect(sort).toEqual('name');
        }));

        it('should save Lookup Datasets sort', inject(($window, StorageService) => {
            // when
            StorageService.setLookupDatasetsSort('name');

            // then
            expect($window.localStorage.getItem(LOOKUP_DATASETS_SORT_KEY)).toEqual('"name"');
        }));

        it('should get Lookup Datasets order', inject(($window, StorageService) => {
            // given
            expect(StorageService.getLookupDatasetsOrder()).toBeUndefined();
            $window.localStorage.setItem(LOOKUP_DATASETS_ORDER_KEY, '"desc"');

            // when
            const order = StorageService.getLookupDatasetsOrder();

            // then
            expect(order).toEqual('desc');
        }));

        it('should save Lookup Datasets order', inject(($window, StorageService) => {
            // when
            StorageService.setLookupDatasetsOrder('desc');

            // then
            expect($window.localStorage.getItem(LOOKUP_DATASETS_ORDER_KEY)).toEqual('"desc"');
        }));
    });

    describe('sort/order', () => {
        const DATASETS_SORT_KEY = 'org.talend.dataprep.datasets.sort';
        const DATASETS_ORDER_KEY = 'org.talend.dataprep.datasets.order';
        const PREPARATIONS_SORT_KEY = 'org.talend.dataprep.preparations.sort';
        const PREPARATIONS_ORDER_KEY = 'org.talend.dataprep.preparations.order';

        it('should get Datasets sort', inject(($window, StorageService) => {
            // given
            expect(StorageService.getDatasetsSort()).toBeUndefined();
            $window.localStorage.setItem(DATASETS_SORT_KEY, '"name"');

            // when
            const sort = StorageService.getDatasetsSort();

            // then
            expect(sort).toEqual('name');
        }));

        it('should save Datasets sort', inject(($window, StorageService) => {
            // when
            StorageService.setDatasetsSort('name');

            // then
            expect($window.localStorage.getItem(DATASETS_SORT_KEY)).toEqual('"name"');
        }));

        it('should get Datasets order', inject(($window, StorageService) => {
            // given
            expect(StorageService.getDatasetsOrder()).toBeUndefined();
            $window.localStorage.setItem(DATASETS_ORDER_KEY, '"desc"');

            // when
            const order = StorageService.getDatasetsOrder();

            // then
            expect(order).toEqual('desc');
        }));

        it('should save Datasets order', inject(($window, StorageService) => {
            // when
            StorageService.setDatasetsOrder('desc');

            // then
            expect($window.localStorage.getItem(DATASETS_ORDER_KEY)).toEqual('"desc"');
        }));

        it('should get Preparations sort', inject(($window, StorageService) => {
            // given
            expect(StorageService.getPreparationsSort()).toBeUndefined();
            $window.localStorage.setItem(PREPARATIONS_SORT_KEY, '"name"');

            // when
            const sort = StorageService.getPreparationsSort();

            // then
            expect(sort).toEqual('name');
        }));

        it('should save Preparations sort', inject(($window, StorageService) => {
            // when
            StorageService.setPreparationsSort('name');

            // then
            expect($window.localStorage.getItem(PREPARATIONS_SORT_KEY)).toEqual('"name"');
        }));

        it('should get Preparations order', inject(($window, StorageService) => {
            // given
            expect(StorageService.getPreparationsOrder()).toBeUndefined();
            $window.localStorage.setItem(PREPARATIONS_ORDER_KEY, '"desc"');

            // when
            const order = StorageService.getPreparationsOrder();

            // then
            expect(order).toEqual('desc');
        }));

        it('should save Preparations order', inject(($window, StorageService) => {
            // when
            StorageService.setPreparationsOrder('desc');

            // then
            expect($window.localStorage.getItem(PREPARATIONS_ORDER_KEY)).toEqual('"desc"');
        }));
    });
});