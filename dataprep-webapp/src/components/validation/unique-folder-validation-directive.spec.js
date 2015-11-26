describe('unique folder validation directive', function() {
    'use strict';

    var scope, createElement;

    beforeEach(module('data-prep.validation'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function(directiveScope) {
            var element = angular.element('<form name="myForm"><input id="myInput" ng-model="myInput" unique-folder="folders" /></form>');
            $compile(element)(directiveScope);
            directiveScope.$digest();
            return element;
        };
    }));

    it('should invalidate empty input', function() {
        //given
        scope.folders = [];
        scope.myInput = '';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.uniqueFolderValidation).toBeTruthy();
    });

    it('should validate folder', function() {
        //given
        scope.folders = [];
        scope.myInput = '1';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.uniqueFolderValidation).toBeFalsy();
    });

    it('should invalidate existing folder', function() {
        //given
        scope.folders = [{name :'1'}];
        scope.myInput = '1';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.uniqueFolderValidation).toBeTruthy();
    });

});