/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('folder selection component', () => {
    let createElement;
    let scope;
    let element;
    let tree;

    beforeEach(angular.mock.module('data-prep.folder-selection'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            FOLDER_PATH: '(Path: {{path}})',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new(true);
        scope.isLoading = false;
        scope.tree = {
            folder: { // HOME
                id: '1',
                name: 'HOME',
                path: '/',
            },
            children: [
                {
                    folder: { // /folder1
                        id: '2',
                        name: 'folder1',
                        path: '/folder1',
                    },
                    children: [
                        {
                            folder: { // /folder1/subfolder1
                                id: '4',
                                name: 'subfolder1',
                                path: '/folder1/subfolder1',
                            },
                            children: [],
                        },
                        {
                            folder: { // /folder1/subfolder2
                                id: '5',
                                name: 'subfolder2',
                                path: '/folder1/subfolder2',
                            },
                            children: [],
                        },
                    ],
                },
                {
                    folder: { // /folder2
                        id: '3',
                        name: 'folder2',
                        path: '/folder2',
                    },
                    children: [
                        {
                            folder: { // /folder2/subfolder3
                                id: '6',
                                name: 'subfolder3',
                                path: '/folder2/subfolder3',
                            },
                            children: [],
                        },
                    ],
                },
            ],
        };
        createElement = () => {
            element = angular.element(`<folder-selection ng-model="selectedFolder" tree="tree" is-loading="isLoading"></folder-selection>`);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render input search', () => {
        //when
        createElement();

        //then
        expect(element.find('input').length).toBe(1);
    });

    it('should render loader', () => {
        //when
        createElement();
        scope.isLoading = true;
        scope.$digest();

        //then
        expect(element.find('loader').length).toBe(1);
    });

    it('should render folder tree', () => {
        //given
        scope.selectedFolder = { id: '5' };

        //when
        createElement();

        //then
        expect(element.find('.folder-tree-node').length).toBe(5);
        expect(element.find('.folder-tree-node').eq(3).hasClass('folder-selected')).toBe(true);
        expect(element.find('.folder-tree-node').eq(0).text().trim()).toBe('HOME');
        expect(element.find('.folder-tree-node').eq(1).text().trim()).toBe('folder1');
        expect(element.find('.folder-tree-node').eq(2).text().trim()).toBe('subfolder1');
        expect(element.find('.folder-tree-node').eq(3).text().trim()).toBe('subfolder2');
        expect(element.find('.folder-tree-node').eq(4).text().trim()).toBe('folder2');
    });

    it('should render search items', () => {
        //given
        scope.selectedFolder = { id: '5' };
        createElement();

        const ctrl = element.controller('folderSelection');
        ctrl.searchFolderQuery = 'folder1';

        //when
        ctrl.performSearch();
        scope.$digest();

        //then
        expect(element.find('.folder-tree-node').length).toBe(2);
        expect(element.find('.folder-tree-node').eq(0).text().trim()).toBe('folder1 (Path: /folder1)');
        expect(element.find('.folder-tree-node').eq(1).text().trim()).toBe('subfolder1 (Path: /folder1/subfolder1)');
    });
});
