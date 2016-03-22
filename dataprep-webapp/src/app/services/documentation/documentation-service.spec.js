/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Documentation Service', () => {
    'use strict';

    let results = {
        data: '"https://help.talend.com/","What is a recipe?","dataset, preparation"\n"https://help.talend.com/","Export","recipe"'
    };

    beforeEach(angular.mock.module('data-prep.services.documentation'));

    beforeEach(inject(($q, DocumentationRestService) => {
        spyOn(DocumentationRestService, 'search').and.returnValue($q.when(results));
    }));

    it('should call documentation search rest service and process data', inject(($rootScope, DocumentationService) => {
        //given
        let result= null;
        let expectedResult = [
            {url: 'https://help.talend.com/', name: 'What is a <span class="highlighted">recipe</span>?', description: 'dataset, preparation', tooltipName: 'What is a recipe?'},
            {url: 'https://help.talend.com/', name: 'Export', description: '<span class="highlighted">recipe</span>', tooltipName: 'Export'}
        ];

        //when
        DocumentationService.search('recipe').then((response) => {
            result = response;
        });

        $rootScope.$digest();

        //then
        expect(result).toEqual(expectedResult);
    }));

});