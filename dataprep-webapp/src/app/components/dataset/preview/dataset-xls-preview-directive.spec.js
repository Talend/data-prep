/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

'use strict';

describe('Dataset upload list directive', function () {
    var scope;
    var createElement;
    var element;

    beforeEach(angular.mock.module('data-prep.dataset-xls-preview'));

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<dataset-xls-preview></dataset-xls-preview>');
            $compile(element)(scope);
            $timeout.flush();
            return element;
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should init grid ', inject(($timeout) => {
        //when
        createElement();
        let ctrl = element.controller('dataset-xls-preview');
        spyOn(ctrl, 'initGrid');
        scope.$digest();
        $timeout.flush(100);

        //then
        expect(ctrl.initGrid).toHaveBeenCalled();
    }));

    it('should init metadata name input', inject(function (DatasetSheetPreviewService) {
        //given
        createElement();
        var nameInput = angular.element('body').find('.dataset-sheet-preview').find('input[ng-model="previewCtrl.metadata.name"]').eq(0)[0];
        expect(nameInput.value).toBeFalsy();

        //when
        DatasetSheetPreviewService.currentMetadata = { name: 'my sheet' };
        scope.$digest();

        //then
        expect(nameInput.value).toBe('my sheet');
    }));

    it('should init sheet listbox', inject(function (DatasetSheetPreviewService) {
        //given
        createElement();
        var sheetSelection = angular.element('body').find('.dataset-sheet-preview').find('select[ng-model="previewCtrl.selectedSheetName"]').eq(0);

        //when
        DatasetSheetPreviewService.currentMetadata = {
            schemaParserResult: {
                sheetContents: [{ name: 'my first sheet' }, { name: 'my second sheet' }, { name: 'my third sheet' }],
            },
        };
        DatasetSheetPreviewService.selectedSheetName = 'my second sheet';
        scope.$digest();

        //then
        expect(sheetSelection.find('option').length).toBe(3);
        expect(sheetSelection.find('option').eq(1)[0].selected).toBeTruthy();
        expect(sheetSelection.find('option').eq(0)[0].label).toBe('my first sheet');
        expect(sheetSelection.find('option').eq(1)[0].label).toBe('my second sheet');
        expect(sheetSelection.find('option').eq(2)[0].label).toBe('my third sheet');
    }));
});
