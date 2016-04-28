/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory state service', () => {
    'use strict';

    let datasets, preparations;

    beforeEach(angular.mock.module('data-prep.services.state'));

    beforeEach(() => {
        datasets = [
            {
                id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
                name: 'customers_jso_light',
                author: 'anonymousUser',
                records: 15,
                nbLinesHeader: 1,
                nbLinesFooter: 0,
                created: '03-30-2015 08:06',
            },
            {
                id: '3b21388c-f54a-4334-9bef-748912d0806f',
                name: 'customers_jso',
                author: 'anonymousUser',
                records: 1000,
                nbLinesHeader: 1,
                nbLinesFooter: 0,
                created: '03-30-2015 07:35',
            },
            {
                id: '124568124-8da46-6635-6b5e-7845748fc54',
                name: 'dataset_without_preparations',
                author: 'anonymousUser',
                records: 1000,
                nbLinesHeader: 1,
                nbLinesFooter: 0,
                created: '03-30-2015 07:35',
            },
        ];
        preparations = [
            {
                id: 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
                dataSetId: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
                author: 'anonymousUser',
                creationDate: 1427447300300,
            },
            {
                id: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
                dataSetId: '3b21388c-f54a-4334-9bef-748912d0806f',
                author: 'anonymousUser',
                creationDate: 1427447330693,
            }
        ];
    });

    describe('preparations', () => {
        it('should set preparations', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparations = null;
            inventoryState.datasets = null;

            //when
            InventoryStateService.setPreparations(preparations);

            //then
            expect(inventoryState.preparations).toBe(preparations);
        }));

        it('should consolidate preparations and datasets', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparations = null;
            inventoryState.datasets = datasets;

            //when
            InventoryStateService.setPreparations(preparations);

            //then
            expect(inventoryState.datasets[0].preparations[0]).toBe(preparations[0]);
            expect(inventoryState.datasets[1].preparations[0]).toBe(preparations[1]);
            expect(inventoryState.datasets[2].preparations.length).toBe(0);

            expect(inventoryState.preparations[0].dataset).toBe(datasets[0]);
            expect(inventoryState.preparations[1].dataset).toBe(datasets[1]);
        }));

        it('should remove a preparation from preparations list', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparations = preparations;

            //when
            InventoryStateService.removePreparation(preparations[0]);

            //then
            expect(inventoryState.preparations.length).toBe(1);
            expect(inventoryState.preparations[0].id).toBe('fbaa18e82e913e97e5f0e9d40f04413412be1126');
        }));
    });

    describe('datasets', () => {
        it('should set datasets', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparations = null;
            inventoryState.datasets = null;

            //when
            InventoryStateService.setDatasets(datasets);

            //then
            expect(inventoryState.datasets).toBe(datasets);
        }));

        it('should consolidate preparations and datasets', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparations = preparations;
            inventoryState.datasets = null;

            //when
            InventoryStateService.setDatasets(datasets);

            //then
            expect(inventoryState.datasets[0].preparations[0]).toBe(preparations[0]);
            expect(inventoryState.datasets[1].preparations[0]).toBe(preparations[1]);
            expect(inventoryState.datasets[2].preparations.length).toBe(0);

            expect(inventoryState.preparations[0].dataset).toBe(datasets[0]);
            expect(inventoryState.preparations[1].dataset).toBe(datasets[1]);
        }));

        it('should remove a dataset from datasets list', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.datasets = datasets;
            expect(inventoryState.datasets.length).toBe(3);

            //when
            InventoryStateService.removeDataset(datasets[0]);

            //then
            expect(inventoryState.datasets.length).toBe(2);
            expect(inventoryState.datasets[0].id).toBe('3b21388c-f54a-4334-9bef-748912d0806f');
            expect(inventoryState.datasets[1].id).toBe('124568124-8da46-6635-6b5e-7845748fc54');
        }));

        it('should update dataset name', inject((inventoryState, InventoryStateService) => {
            // given
            inventoryState.datasets = [
                { id: '1', name: 'toto' },
                { id: '2', name: 'tata' },
                { id: '3', name: 'titi' },
                { id: '4', name: 'tutu' },
            ];

            // when
            InventoryStateService.setDatasetName('2', 'tonton');

            // then
            expect(inventoryState.datasets[1].name).toBe('tonton');
        }));
    });

    describe('folder', () => {
        it('should set folder metadata', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.folder = {};
            const folderPath = '/toto/tata/jso';

            //when
            InventoryStateService.setFolder(folderPath);

            //then
            expect(inventoryState.folder.metadata).toEqual({
                name: 'jso',
                path: folderPath,
            });
        }));

        it('should set root folder metadata with empty path', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.folder = {};

            //when
            InventoryStateService.setFolder('');

            //then
            expect(inventoryState.folder.metadata).toEqual({
                name: 'Home',
                path: '',
            });
        }));

        it('should set root folder metadata with "/" path', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.folder = {};

            //when
            InventoryStateService.setFolder('/');

            //then
            expect(inventoryState.folder.metadata).toEqual({
                name: 'Home',
                path: '',
            });
        }));

        it('should set content', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.folder = {};
            const folderPath = '/toto/tata/jso';
            const content = {
                folders: [],
                preparations: [],
            };

            //when
            InventoryStateService.setFolder(folderPath, content);

            //then
            expect(inventoryState.folder.content).toBe(content);
        }));

        it('should set folder stack', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.folder = {};
            const folderPath = '/toto/tata/jso';

            //when
            InventoryStateService.setFolder(folderPath);

            //then
            expect(inventoryState.foldersStack[0]).toEqual({ name: 'Home', path: '' });
            expect(inventoryState.foldersStack[1]).toEqual({ name: 'toto', path: '/toto' });
            expect(inventoryState.foldersStack[2]).toEqual({ name: 'tata', path: '/toto/tata' });
            expect(inventoryState.foldersStack[3]).toEqual({ name: 'jso', path: '/toto/tata/jso' });
        }));

        it('should set menuChildren', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.menuChildren = [];
            const menuChildren = [
                {
                    'id': 'lookups/simple_lookup',
                    'path': 'lookups/simple_lookup',
                    'name': 'simple_lookup',
                    'creationDate': 1448880158000,
                    'modificationDate': 1448880158000
                }
            ];

            //when
            InventoryStateService.setMenuChildren(menuChildren);

            //then
            expect(inventoryState.menuChildren).toBe(menuChildren);
        }));
    });

    describe('sort/order', () => {
        it('should set datasets sort', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.datasetsSort = '';

            //when
            InventoryStateService.setDatasetsSort('name');

            //then
            expect(inventoryState.datasetsSort).toBe('name');
        }));

        it('should set datasets order', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.datasetsOrder = '';

            //when
            InventoryStateService.setDatasetsOrder('desc');

            //then
            expect(inventoryState.datasetsOrder).toBe('desc');
        }));

        it('should set preparations sort', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparationsSort = '';

            //when
            InventoryStateService.setPreparationsSort('name');

            //then
            expect(inventoryState.preparationsSort).toBe('name');
        }));

        it('should set preparations order', inject((inventoryState, InventoryStateService) => {
            //given
            inventoryState.preparationsOrder = '';

            //when
            InventoryStateService.setPreparationsOrder('desc');

            //then
            expect(inventoryState.preparationsOrder).toBe('desc');
        }));
    });
});