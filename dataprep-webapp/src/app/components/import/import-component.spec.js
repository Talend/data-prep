/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import directive', () => {
    'use strict';

    var scope, createElement, element, ctrl, StateMock;

    beforeEach(angular.mock.module('data-prep.import'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('data-prep.import', function ($provide) {
        StateMock = {
            import: {
                importTypes:[
                    {
                        "locationType":"hdfs",
                        "contentType":"application/vnd.remote-ds.hdfs",
                        "parameters":[
                            {
                                "name":"name",
                                "type":"string",
                                "implicit":false,
                                "canBeBlank":false,
                                "format":"",
                                "default":"",
                                "description":"Name",
                                "label":"Enter the dataset name:"
                            },
                            {
                                "name":"url",
                                "type":"string",
                                "implicit":false,
                                "canBeBlank":false,
                                "format":"hdfs://host:port/file",
                                "default":"",
                                "description":"URL",
                                "label":"Enter the dataset URL:"
                            }
                        ],
                        "defaultImport":false,
                        "label":"From HDFS",
                        "title":"Add HDFS dataset"
                    }
                ]
            }
        };
        $provide.constant('state', StateMock);
    }));


    beforeEach(inject(($rootScope, $compile, $componentController) => {
        scope = $rootScope.$new();
        createElement = () => {
            let html = angular.element('<import></import>');
            element = $compile(html)(scope);
            scope.$digest();
            return element;
        };

        ctrl = $componentController(
            'import',
            {$scope: scope}
        );
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render import', inject( () => {
        //when
        createElement();
        ctrl.showModal = false;
        ctrl.datasetNameModal = false;
        scope.$digest();

        //then
        expect(element.find('#help-import').length).toBe(1);
        expect(element.find('talend-button-dropdown').length).toBe(1);

        expect(element.find('#datasetFile').length).toBe(1);

        expect(angular.element('body').find('talend-modal').length).toBe(2);

        expect(element.find('dataset-xls-preview').length).toBe(1);
    }));
});