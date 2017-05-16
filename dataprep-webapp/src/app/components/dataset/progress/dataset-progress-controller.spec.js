/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset progress controller', () => {
    let createController;
    let scope;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.dataset-progress', ($provide) => {
		stateMock = {
			dataset: {
				uploadingDataset:  null,
			},
		};
		$provide.constant('state', stateMock);
	}));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController(
                'datasetProgress',
                { $scope: scope }
            );
        };
    }));

    describe('progression getter', () => {
        it('should return the actual progression', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 66 };

            const ctrl = createController();

            //then
            expect(ctrl.progression).toBe(66);
        }));

        it('should correct an invalid progression (>100)', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 666 };

            const ctrl = createController();

            //then
            expect(ctrl.progression).toBe(100);
        }));

        it('should correct an invalid progression (<0)', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: -42 };

            const ctrl = createController();

            //then
            expect(ctrl.progression).toBe(0);
        }));

        it('should correct an invalid progression (NaN)', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 'nop' };

            const ctrl = createController();

            //then
            expect(ctrl.progression).toBe(0);
        }));
    });

    describe('isUploading getter', () => {
        it('should return true if there is an uploading dataset', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 66 };

            const ctrl = createController();

            //then
            expect(ctrl.isUploading).toBe(true);
        }));

        it('should return false if there is no uploading dataset', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = null;

            const ctrl = createController();

            //then
            expect(ctrl.isUploading).toBe(false);
        }));
    });


    describe('isUploadComplete getter', () => {
        it('should return false if there is an incomplete upload', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 66 };

            const ctrl = createController();

            //then
            expect(ctrl.isUploadComplete).toBe(false);
        }));

        it('should return false if there is no uploading dataset', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = null;

            const ctrl = createController();

            //then
            expect(ctrl.isUploadComplete).toBe(false);
        }));


        it('should return true if the progression is 100%', inject(() => {
            //given
            stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 100 };

            const ctrl = createController();

            //then
            expect(ctrl.isUploadComplete).toBe(true);
        }));
    });
});
