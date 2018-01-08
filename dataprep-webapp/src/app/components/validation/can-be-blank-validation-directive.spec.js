/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Not Blank Validation directive', function () {
    'use strict';

    var scope;
    var createElement;

    beforeEach(angular.mock.module('data-prep.validation'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function (directiveScope) {
            var element = angular.element('<form name="myForm"><input name="myInput" ng-model="myInput" can-be-blank="{{canBeBlank}}" /></form>');
            $compile(element)(directiveScope);
            directiveScope.$digest();
            return element;
        };
    }));

    it('should validate empty input when it can be blank', function () {
        //given
        scope.canBeBlank = true;
        scope.myInput = '';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$invalid).toBeFalsy();
    });

    it('should invalidate input when it cannot be blank and input is empty', function () {
        //given
        scope.canBeBlank = false;
        scope.myInput = '';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$invalid).toBeTruthy();
    });

    it('should validate input when it cannot be blank and input is not empty', function () {
        //given
        scope.canBeBlank = false;
        scope.myInput = 'city';

        //when
        createElement(scope);

        //then
        expect(scope.myForm.$invalid).toBeFalsy();
    });
});
