describe('Datetime validation directive', function() {
   'use strict';

    var scope, createElement, createElementWithFormat;


    beforeEach(module('data-prep.validation'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function() {
            var element = angular.element('<form name="myForm"><input id="myInput" ng-model="myInput" is-date-time /></form>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };

        createElementWithFormat = function() {
            var element = angular.element('<form name="myForm"><input id="myInput" ng-model="myInput" is-date-time format="DD/MM/YYYY"/></form>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should not validate empty input', function() {
        //when
        createElement();

        //then
        expect(scope.myForm.$invalid).toBeTruthy();
    });

    it('should not validate integer', function() {
        //given
        scope.myInput = 5.2;

        //when
        createElement();

        //then
        expect(scope.myForm.$invalid).toBeTruthy();
    });

    it('should validate a date with default format', function() {
      //given
      scope.myInput = '02/01/2012 10:02:23';

      //when
      createElement();

      //then
      expect(scope.myForm.$invalid).toBeFalsy();
    });

    it('should validate a date with provided format', function() {
      //given
      scope.myInput = '02/01/2012';

      //when
      createElementWithFormat();

      //then
      expect(scope.myForm.$invalid).toBeFalsy();
    });

});