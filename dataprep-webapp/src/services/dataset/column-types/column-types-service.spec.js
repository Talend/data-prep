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

    beforeEach(module('data-prep.services.dataset'));

    beforeEach(inject(function ($rootScope, $injector) {
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

    it('should get types saved types (no second backend call)', inject(function ($rootScope, ColumnTypesService, RestURLs) {
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