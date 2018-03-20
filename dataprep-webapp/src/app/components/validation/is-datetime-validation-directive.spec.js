/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Datetime validation directive', function () {
    'use strict';

    var scope;
    var createElement;
    var createElementWithFormat;

    beforeEach(angular.mock.module('data-prep.validation'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function () {
            var element = angular.element('<form name="myForm"><input id="myInput" ng-model="myInput" is-date-time /></form>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };

        createElementWithFormat = function () {
            var element = angular.element('<form name="myForm"><input id="myInput" ng-model="myInput" is-date-time format="DD/MM/YYYY"/></form>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should not validate empty input', function () {
        //when
        createElement();

        //then
        expect(scope.myForm.$invalid).toBeTruthy();
    });

    it('should not validate integer', function () {
        //given
        scope.myInput = 5.2;

        //when
        createElement();

        //then
        expect(scope.myForm.$invalid).toBeTruthy();
    });

    it('should validate a date with default format', function () {
        //given
        scope.myInput = '2012-12-31 10:02';

        //when
        createElement();

        //then
        expect(scope.myForm.$invalid).toBeFalsy();
    });

    it('should validate a date with provided format', function () {
        //given
        scope.myInput = '31/12/2012';

        //when
        createElementWithFormat();

        //then
        expect(scope.myForm.$invalid).toBeFalsy();
    });
});
