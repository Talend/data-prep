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

    const thcResult = `
    "https://help.talend.com/pages/viewpage.action?pageId=266309178","What is a recipe?"," ... data, called datasets, and the directions are the set of functions applied to the dataset. recipe.png Visually, the recipe is the topdown sequence of functions in the left collapsible panel. A recipe is linked to the dataset through a preparation. Do not look for a save button: every update ...  ... "
    "https://help.talend.com/pages/viewpage.action?pageId=266309061","Export the results of your recipe"," ... Once your recipe is complete, you may want to export the sample dataset you have cleaned ... "
    "https://help.talend.com/pages/viewpage.action?pageId=266309169","What is a dataset?"," ... dataset holds the raw data that can be used as the raw material for one or more recipes. It is presented as a table on which you can apply recipes without affecting the original data. As they are not altered by the recipes, datasets can be reused across preparations.    Related concepts ... "
    "https://help.talend.com/pages/viewpage.action?pageId=266309176","What is a preparation?"," ... preparation is what links a dataset and a recipe together: it is the final outcome that you want to achieve with your data. You can ... "
    "https://help.talend.com/pages/viewpage.action?pageId=266309172","What is a function?"," ... applied on datasets, they do not modify the original data. Applied functions are recorded, in sequence, into recipes.    Related concepts  What is a dataset? https://help.talend.com/pages/viewpage.action?pageId=266309169 What is a preparation? https://help.talend.com/pages/viewpage.action?pageId=266309176 What is a recipe ... "
    `;

    beforeEach(angular.mock.module('data-prep.services.documentation'));

    beforeEach(inject(($q, DocumentationRestService) => {
        spyOn(DocumentationRestService, 'search').and.returnValue($q.when({data: thcResult}));
    }));

    it('should call documentation search rest service and process data', inject(($rootScope, DocumentationService) => {
        //given
        let result = null;
        let expectedResult = [
            {
                url: 'https://help.talend.com/pages/viewpage.action?pageId=266309178',
                name: 'What is a <span class="highlighted">recipe</span>?',
                description: ' ... data, called datasets, and the directions are the set of functions applied to the dataset. <span class="highlighted">recipe</span>.png Visually, the <span class="highlighted">recipe</span> is the topdown sequence of functions in the left collapsible panel. A <span class="highlighted">recipe</span> is linked to the dataset through a preparation. Do not look for a save button: every update ...  ... ',
                tooltipName: 'What is a recipe?',
                inventoryType: 'documentation'
            },
            {
                url: 'https://help.talend.com/pages/viewpage.action?pageId=266309061',
                name: 'Export the results of your <span class="highlighted">recipe</span>',
                description: ' ... Once your <span class="highlighted">recipe</span> is complete, you may want to export the sample dataset you have cleaned ... ',
                tooltipName: 'Export the results of your recipe',
                inventoryType: 'documentation'
            },
            {
                url: 'https://help.talend.com/pages/viewpage.action?pageId=266309169',
                name: 'What is a dataset?',
                description: ' ... dataset holds the raw data that can be used as the raw material for one or more <span class="highlighted">recipe</span>s. It is presented as a table on which you can apply <span class="highlighted">recipe</span>s without affecting the original data. As they are not altered by the <span class="highlighted">recipe</span>s, datasets can be reused across preparations.    Related concepts ... ',
                tooltipName: 'What is a dataset?',
                inventoryType: 'documentation'
            },
            {
                url: 'https://help.talend.com/pages/viewpage.action?pageId=266309176',
                name: 'What is a preparation?',
                description: ' ... preparation is what links a dataset and a <span class="highlighted">recipe</span> together: it is the final outcome that you want to achieve with your data. You can ... ',
                tooltipName: 'What is a preparation?',
                inventoryType: 'documentation'
            },
            {
                url: 'https://help.talend.com/pages/viewpage.action?pageId=266309172',
                name: 'What is a function?',
                description: ' ... applied on datasets, they do not modify the original data. Applied functions are recorded, in sequence, into <span class="highlighted">recipe</span>s.    Related concepts  What is a dataset? https://help.talend.com/pages/viewpage.action?pageId=266309169 What is a preparation? https://help.talend.com/pages/viewpage.action?pageId=266309176 What is a <span class="highlighted">recipe</span> ... ',
                tooltipName: 'What is a function?',
                inventoryType: 'documentation'
            }
        ];

        //when
        DocumentationService.search('recipe').then((response) => result = response);
        $rootScope.$digest();

        //then
        expect(result).toEqual(expectedResult);
    }));

});