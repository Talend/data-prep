/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Column types Service', function () {
    'use strict';

    const types = [
        { id: 'ANY', name: 'any', labelKey: 'ANY' },
        { id: 'STRING', name: 'string', labelKey: 'STRING' },
        { id: 'NUMERIC', name: 'numeric', labelKey: 'NUMERIC' },
        { id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
        { id: 'DOUBLE', name: 'double', labelKey: 'DOUBLE' },
        { id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
        { id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
        { id: 'DATE', name: 'date', labelKey: 'DATE' },
    ];

    const semanticDomains = [
        {
            "id": "AIRPORT",
            "label": "Airport",
            "frequency": 3.03,
        },
        {
            "id": "CITY",
            "label": "City",
            "frequency": 99.24,
        },
    ];

    beforeEach(angular.mock.module('data-prep.services.column-types'));

    describe('get types', () => {
        beforeEach(inject(($q, ColumnTypesRestService) => {
            spyOn(ColumnTypesRestService, 'fetchUrl').and.returnValue($q.when(types));
        }));

        it('should get types from backend', inject(($q, ColumnTypesService, ColumnTypesRestService, RestURLs) => {
            //when
            ColumnTypesService.getTypes();

            //then
            expect(ColumnTypesRestService.fetchUrl).toHaveBeenCalledWith(RestURLs.typesUrl);
        }));

        it('should get saved types (no second backend call)', inject(($rootScope, ColumnTypesService, ColumnTypesRestService) => {
            // given
            ColumnTypesService.getTypes();
            $rootScope.$digest();

            // when
            ColumnTypesService.getTypes();

            //then
            expect(ColumnTypesRestService.fetchUrl.calls.count()).toBe(1);
        }));
    });

    describe('get types', () => {
        beforeEach(inject(($q, ColumnTypesRestService) => {
            spyOn(ColumnTypesRestService, 'fetchUrl').and.returnValue($q.when(semanticDomains));
        }));

        it('should get domains of a dataset', inject(($q, ColumnTypesService, ColumnTypesRestService, RestURLs) => {
            // given
            const inventoryType = 'dataset';
            const inventoryId = '521454abc-54545adef';
            const colId = '0000';
            const url = `${RestURLs.datasetUrl}/${inventoryId}/columns/${colId}/types`;

            //when
            ColumnTypesService.getColSemanticDomains(inventoryType, inventoryId, colId);

            //then
            expect(ColumnTypesRestService.fetchUrl).toHaveBeenCalledWith(url);
        }));

        it('should get domains of a preparation', inject(($q, ColumnTypesService, ColumnTypesRestService, RestURLs) => {
            // given
            const inventoryType = 'preparation';
            const inventoryId = '521454abc-54545adef';
            const colId = '0000';
            const url = `${RestURLs.preparationUrl}/${inventoryId}/columns/${colId}/types`;

            //when
            ColumnTypesService.getColSemanticDomains(inventoryType, inventoryId, colId);

            //then
            expect(ColumnTypesRestService.fetchUrl).toHaveBeenCalledWith(url);
        }));
    });
});
