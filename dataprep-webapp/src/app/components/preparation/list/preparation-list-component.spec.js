/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation list component', () => {
    'use strict';

    let scope, createElement, element, stateMock;

    const preparations = [
        {
            id: 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            dataSetId: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
            name: 'preparation 1',
            author: 'anonymousUser',
            creationDate: 1427447300000,
            lastModificationDate: 1427447300300,
            steps: [
                '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                '4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
                '8a1c49d1b64270482e8db8232357c6815615b7cf',
                '599725f0e1331d5f8aae24f22cd1ec768b10348d',
            ],
            actions: [
                {
                    action: 'lowercase',
                    parameters: { column_name: 'birth'  },
                },
                {
                    action: 'uppercase',
                    parameters: { column_name: 'country' },
                },
                {
                    action: 'cut',
                    parameters: {
                        pattern: '.',
                        column_name: 'first_item',
                    },
                },
            ],
        },
        {
            id: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
            dataSetId: '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
            name: 'preparation 2',
            author: 'anonymousUser',
            creationDate: 1427447330000,
            lastModificationDate: 1427447330693,
            steps: [
                '47e2444dd1301120b539804507fd307072294048',
                'ae1aebf4b3fa9b983c895486612c02c766305410',
                '24dcd68f2117b9f93662cb58cc31bf36d6e2867a',
                '599725f0e1331d5f8aae24f22cd1ec768b10348d',
            ],
            actions: [
                {
                    action: 'cut',
                    parameters: {
                        pattern: '-',
                        column_name: 'birth',
                    },
                },
                {
                    action: 'fillemptywithdefault',
                    parameters: {
                        default_value: 'N/A',
                        column_name: 'state',
                    }
                },
                {
                    action: 'uppercase',
                    parameters: { column_name: 'lastname' },
                },
            ],
        },
    ];

    const folders = [
        { path: '/tata/titi', name: 'titi' },
        { path: '/tata/toto', name: 'toto' },
        { path: '/tata/tutu', name: 'tutu' },
    ];

    beforeEach(angular.mock.module('data-prep.preparation-list', ($provide) => {
        stateMock = {
            inventory: {
                folder: {
                    content: {
                        folders: folders,
                        preparations: preparations,
                    }
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<preparation-list></preparation-list>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(inject(() => {
        scope.$destroy();
        element.remove();
    }));

    it('should render folder\'s content preparation', inject(() => {
        //when
        createElement();

        //then
        expect(element.find('#preparation-inventory-list').length).toBe(1);
        expect(element.find('inventory-item[type="preparation"]').length).toBe(2);

        const itemTitles = element.find('inventory-item[type="preparation"] .inventory-title');
        expect(itemTitles.eq(0).text()).toBe('preparation 1');
        expect(itemTitles.eq(1).text()).toBe('preparation 2');
    }));

    it('should render folder\'s content children folders', inject(() => {
        //when
        createElement();

        //then
        expect(element.find('#preparation-inventory-list').length).toBe(1);
        expect(element.find('inventory-item[type="folder"]').length).toBe(3);

        const itemTitles = element.find('inventory-item[type="folder"] .inventory-title');
        expect(itemTitles.eq(0).text()).toBe('titi');
        expect(itemTitles.eq(1).text()).toBe('toto');
        expect(itemTitles.eq(2).text()).toBe('tutu');
    }));
});