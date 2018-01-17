/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Type validation directive', function () {
    'use strict';

    var scope;
    var createElement;

    beforeEach(angular.mock.module('data-prep.validation'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function (directiveScope) {
            var element = angular.element('<form name="myForm"><input id="myInput" ng-model="myInput" type="{{inputType}}" is-type="{{type}}" /></form>');
            $compile(element)(directiveScope);
            directiveScope.$digest();
            return element;
        };
    }));

    it('should validate empty input', function () {
        //given
        scope.inputType = 'text';
        scope.type = 'string';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeFalsy();
    });

    it('should validate integer type', function () {
        //given
        scope.inputType = 'number';
        scope.type = 'integer';
        scope.myInput = 5;

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeFalsy();
    });

    it('should invalidate integer type with double', function () {
        //given
        scope.inputType = 'number';
        scope.type = 'integer';
        scope.myInput = 5.2;

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeTruthy();
    });

    it('should invalidate integer type with letter', function () {
        //given
        scope.inputType = 'text';
        scope.type = 'integer';
        scope.myInput = '5a';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeTruthy();
    });

    it('should validate numeric type with integer', function () {
        //given
        scope.inputType = 'number';
        scope.type = 'numeric';
        scope.myInput = 5;

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeFalsy();
    });

    it('should validate numeric type with double', function () {
        //given
        scope.inputType = 'number';
        scope.type = 'numeric';
        scope.myInput = 5.5;

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeFalsy();
    });

    it('should invalidate numeric type with letter', function () {
        //given
        scope.inputType = 'text';
        scope.type = 'numeric';
        scope.myInput = '5a';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeTruthy();
    });

    it('should validate text type', function () {
        //given
        scope.inputType = 'text';
        scope.type = 'string';
        scope.myInput = '5a';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$error.isTypeValidation).toBeFalsy();
    });
});
