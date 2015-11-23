describe('Upload File directive', function() {
    'use strict';

    var element, createElement;
    var body = angular.element('body');
    var scope;
    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        createElement = function() {

            scope = $rootScope.$new();
            scope.updateDatasetFile = 'file';
            scope.uploadUpdatedDatasetFile = function(){};

            element = angular.element('<talend-file-selector button-data-icon="E" button-title="REPLACE_FILE_CONTENT" file-model="updateDatasetFile" on-file-change="uploadUpdatedDatasetFile(dataset)">'+
                   '</talend-file-selector>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();

        };
    }));

    it('should trigger click on input', function() {
        //given
        createElement();
        var event = angular.element.Event('click');
        var ctrl = element.controller('talendFileSelector');
        var clicked = false;
        element.find('input').bind('click', function() {
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