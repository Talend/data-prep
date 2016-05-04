/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset list controller', () => {

    let createController, scope, stateMock;

    const datasets = [
        { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)' },
        { id: 'ab45f893d8e923', name: 'Us states' },
        { id: 'cf98d83dcb9437', name: 'Customers (1K lines)' }
    ];

    beforeEach(angular.mock.module('data-prep.dataset-list', ($provide) => {
        stateMock = {
            inventory: { datasets: datasets }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $componentController, StateService, MessageService) => {
        scope = $rootScope.$new();

        createController = () => $componentController('datasetList', { $scope: scope });

        spyOn(MessageService, 'error').and.returnValue();
        spyOn(MessageService, 'success').and.returnValue();
        spyOn(StateService, 'setDatasetName').and.returnValue();
    }));

    describe('update dataset content', () => {
        it('should upload selected file', inject((UpdateWorkflowService) => {
            //given
            spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue();

            var ctrl = createController();
            var dataset = { id: '13b484a9380fa54c02' };
            var updatedDatasetFile = {};
            ctrl.updateDatasetFile = [updatedDatasetFile];

            //when
            ctrl.uploadUpdatedDatasetFile(dataset);

            //then
            expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith(updatedDatasetFile, dataset);
        }));
    });

    describe('remove dataset', () => {
        beforeEach(inject(($q, TalendConfirmService, DatasetService) => {
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());
            spyOn(DatasetService, 'delete').and.returnValue($q.when());
        }));

        it('should ask confirmation before deletion', inject(($q, TalendConfirmService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            //when
            ctrl.remove(dataset);

            //then
            expect(TalendConfirmService.confirm).toHaveBeenCalledWith({ disableEnter: true }, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
                type: 'dataset',
                name: 'Customers (50 lines)'
            });
        }));

        it('should remove dataset', inject(($q, DatasetService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            //when
            ctrl.remove(dataset);
            scope.$digest();

            //then
            expect(DatasetService.delete).toHaveBeenCalledWith(dataset);
        }));

        it('should show confirmation message', inject((MessageService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            //when
            ctrl.remove(dataset);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {
                type: 'dataset',
                name: 'Customers (50 lines)'
            });
        }));
    });

    describe('rename dataset', () => {
        describe('invalid name', () => {
            it('should NOT rename with falsy name', inject(($q, DatasetService) => {
                //given
                const ctrl = createController();
                const name = 'dataset name';
                const dataset = { name: name };

                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, '');
                scope.$digest();

                //then
                expect(dataset.name).toBe(name);
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

        it('should NOT rename with blank name', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const name = 'dataset name';
            const dataset = { name: name };

                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, ' ');
                scope.$digest();

                //then
                expect(dataset.name).toBe(name);
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

            it('should NOT rename with an already existing name', inject(($q, DatasetService, MessageService) => {
                //given
                const ctrl = createController();
                const name = 'foo';
                const dataset = {name: name};

                spyOn(DatasetService, 'getDatasetByName').and.returnValue({ id: 'ab45f893d8e923', name: 'Us states' });
                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, 'Us states');
                scope.$digest();

                //then
                expect(dataset.name).toBe(name);
                expect(DatasetService.update).not.toHaveBeenCalled();
                expect(MessageService.error).toHaveBeenCalledWith('DATASET_NAME_ALREADY_USED_TITLE', 'DATASET_NAME_ALREADY_USED');
            }));
        });

        describe('with current pending rename', () => {
            it('should NOT rename', inject(($q, DatasetService) => {
                //given
                const ctrl = createController();
                const dataset = { id: '461465' };
                const name = 'new dataset name';
                ctrl.renamingList.push(dataset);

                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, name);

                //then
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

            it('should show a warning', inject(($q, MessageService) => {
                //given
                const ctrl = createController();
                const dataset = {id: '461465'};
                const name = 'new dataset name';
                ctrl.renamingList.push(dataset);

                spyOn(MessageService, 'warning').and.returnValue();

                //when
                ctrl.rename(dataset, name);

                //then
                expect(MessageService.warning).toHaveBeenCalledWith(
                    'DATASET_CURRENTLY_RENAMING_TITLE',
                    'DATASET_CURRENTLY_RENAMING'
                );
            }));
        });

        it('should rename dataset', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const dataset = { name: 'my old name' };
            const name = 'new dataset name';

            spyOn(DatasetService, 'rename').and.returnValue($q.when());

            //when
            ctrl.rename(dataset, name);

            //then
            expect(DatasetService.rename).toHaveBeenCalledWith(dataset, name);
        }));

        it('should show confirmation message', inject(($q, DatasetService, MessageService) => {
            //given
            const ctrl = createController();
            const dataset = {name: 'my old name'};
            const name = 'new dataset name';

            spyOn(DatasetService, 'rename').and.returnValue($q.when());

            //when
            ctrl.rename(dataset, name);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('DATASET_RENAME_SUCCESS_TITLE', 'DATASET_RENAME_SUCCESS');
        }));

        it('should manage "renaming" list', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const dataset = { name: 'my old name' };
            const name = 'new dataset name';

            spyOn(DatasetService, 'rename').and.returnValue($q.when());
            expect(ctrl.renamingList.indexOf(dataset) > -1).toBe(false);

            //when
            ctrl.rename(dataset, name);
            expect(ctrl.renamingList.indexOf(dataset) > -1).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.renamingList.indexOf(dataset) > -1).toBe(false);
        }));
    });

    describe('certification', () => {
        beforeEach(inject(($q, DatasetService) => {
            spyOn(DatasetService, 'processCertification').and.returnValue($q.when());
        }));

        it('should process certification', inject((DatasetService) => {
            //given
            const ctrl = createController();

            //when
            ctrl.processCertification(datasets[0]);

            //then
            expect(DatasetService.processCertification).toHaveBeenCalledWith(datasets[0]);
        }));
    });

    describe('clone', () => {
        beforeEach(inject(($q, DatasetService) => {
            spyOn(DatasetService, 'clone').and.returnValue($q.when());
        }));

        it('should call clone function', inject((DatasetService) => {
            //given
            const dataset = { id: '13a82cf7a16b87' };
            const ctrl = createController();
            expect(DatasetService.clone).not.toHaveBeenCalled();

            //when
            ctrl.clone(dataset);

            //then
            expect(DatasetService.clone).toHaveBeenCalledWith(dataset);
        }));

        it('should show success message on clone success', inject((MessageService) => {
            //given
            const dataset = { id: '13a82cf7a16b87' };
            const ctrl = createController();
            expect(MessageService.success).not.toHaveBeenCalled();

            //when
            ctrl.clone(dataset);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('COPY_SUCCESS_TITLE', 'COPY_SUCCESS');
        }));
    });
});
