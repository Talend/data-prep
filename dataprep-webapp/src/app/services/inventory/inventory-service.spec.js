/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Service', () => {
    'use strict';

    let results = {
        data: {
            folders: [{name : 'folder test', lastModificationDate: 1}],
            preparations: [{name : 'prep test', lastModificationDate: 2}],
            datasets: [{
                name : 'dataset test',
                lastModificationDate: 3,
                author: 'toto' ,
                created: 1,
                records: 100,
                path: 'home',
                type: 'csv'
            }]

        }
    };

    beforeEach(angular.mock.module('data-prep.services.inventory'));

    beforeEach(inject(($q, InventoryRestService) => {
        spyOn(InventoryRestService, 'search').and.returnValue($q.when(results));
    }));

    it('should call inventory search rest service and process data', inject(($rootScope, InventoryService) => {
        //given
        let result= null;
        let expectedResult = [
            {inventoryType: 'dataset', author: 'toto', created: 1, records: 100, name: 'dataset <span class="highlighted">test</span>', path: 'home', type: 'csv', originalItem: results.data.datasets[0], lastModificationDate: 3, tooltipName: 'dataset test'},
            {name : 'prep <span class="highlighted">test</span>', lastModificationDate: 2, inventoryType: 'preparation', tooltipName: 'prep test'},
            {name : 'folder <span class="highlighted">test</span>', lastModificationDate: 1, inventoryType: 'folder', tooltipName: 'folder test'}
        ];

        //when
        InventoryService.search('test').then((response) => {
            result = response;
        });

        $rootScope.$digest();

        //then
        expect(result).toEqual(expectedResult);
    }));
});