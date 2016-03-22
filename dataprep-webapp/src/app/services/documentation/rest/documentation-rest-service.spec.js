/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Documentation Rest Service', () => {
    'use strict';

    var $httpBackend;

    beforeEach(angular.mock.module('data-prep.services.documentation'));

    beforeEach(inject( ($rootScope, $injector) => {
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should call Documentation rest service ', inject(($rootScope, DocumentationRestService, documentationSearchURL) => {
        //given
        let result = null;
        let docs = '"url, name, description"\n"url, name, description"';
        $httpBackend
            .expectGET(documentationSearchURL + '&keywords=n')
            .respond(200, docs);

        //when
        DocumentationRestService.search('n').then((response) => {
            result = response.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(docs);
    }));
});