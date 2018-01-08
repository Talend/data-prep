/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Upload File directive', function () {
    'use strict';

    var element;
    var createElement;
    var body = angular.element('body');
    var scope;
    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $compile) {
        createElement = function () {
            scope = $rootScope.$new();
            scope.updateDatasetFile = 'file';
            scope.uploadUpdatedDatasetFile = function () {};

            element = angular.element('<talend-file-selector button-data-icon="E" button-title="REPLACE_FILE_CONTENT" file-model="updateDatasetFile" on-file-change="uploadUpdatedDatasetFile(dataset)">' +
                '</talend-file-selector>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should trigger click on input', function () {
        //given
        createElement();
        var event = angular.element.Event('click');
        var ctrl = element.controller('talendFileSelector');
        var clicked = false;
        element.find('input').bind('click', function () {
            clicked = true;
        });

        //when
        element.find('span').trigger(event);
        //then
        expect(ctrl.buttonDataIcon).toBe('E');
        expect(ctrl.buttonTitle).toBe('REPLACE_FILE_CONTENT');
        expect(ctrl.fileModel).toBe('file');
        expect(clicked).toBeTruthy();
    });
});
