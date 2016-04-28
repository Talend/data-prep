/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Rest Service', () => {
    'use strict';

    let $httpBackend;

    beforeEach(angular.mock.module('data-prep.services.inventory'));

    beforeEach(inject(($injector, RestURLs) => {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should call inventory search rest service ', inject(($rootScope, $q, InventoryRestService, RestURLs) => {
        //given
        let result = null;
        const expectedResult = {
            folders: [],
            preparations: [],
            datasets: []
        };
        $httpBackend
            .expectGET(RestURLs.searchUrl + '?path=/&name=test')
            .respond(200, expectedResult);

        //when
        InventoryRestService.search('test', $q.defer())
            .then((response) => { result = response.data });

        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(expectedResult);
    }));

});