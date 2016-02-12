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

    var $httpBackend;
    var types = [
        {'id': 'ANY', 'name': 'any', 'labelKey': 'ANY'},
        {'id': 'STRING', 'name': 'string', 'labelKey': 'STRING'},
        {'id': 'NUMERIC', 'name': 'numeric', 'labelKey': 'NUMERIC'},
        {'id': 'INTEGER', 'name': 'integer', 'labelKey': 'INTEGER'},
        {'id': 'DOUBLE', 'name': 'double', 'labelKey': 'DOUBLE'},
        {'id': 'FLOAT', 'name': 'float', 'labelKey': 'FLOAT'},
        {'id': 'BOOLEAN', 'name': 'boolean', 'labelKey': 'BOOLEAN'},
        {'id': 'DATE', 'name': 'date', 'labelKey': 'DATE'}
    ];

    beforeEach(angular.mock.module('data-prep.services.dataset'));

    beforeEach(inject(function ($rootScope, $injector, RestURLs) {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should get types from backend call', inject(function ($rootScope, ColumnTypesService, RestURLs) {
        //given
        var result = null;
        $httpBackend
            .expectGET(RestURLs.typesUrl)
            .respond(200, {data: types});

        //when
        ColumnTypesService.getTypes().then(function (response) {
            result = response.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(types);
    }));

    it('should get saved types (no second backend call)', inject(function ($rootScope, ColumnTypesService, RestURLs) {
        //given
        var result = null;
        $httpBackend
            .expectGET(RestURLs.typesUrl)
            .respond(200, {data: types});

        ColumnTypesService.getTypes();
        $httpBackend.flush();
        $rootScope.$digest();

        //when
        ColumnTypesService.getTypes().then(function (response) {
            result = response.data;
        });
        $rootScope.$digest();

        //then
        expect(result).toEqual(types);
    }));
});