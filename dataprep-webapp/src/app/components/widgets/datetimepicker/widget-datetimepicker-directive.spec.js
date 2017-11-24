/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

'use strict';

describe('Datetimepicker directive', function () {
    var scope;
    var element;
    var html;
    var createElement;
    var controller;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('data-prep.services.utils'));

    beforeEach(function () {
        jasmine.clock().install();
    });

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.minModel = 1465895872052;
        scope.onMouseBlur = () => {
        };

        createElement = function () {
            element = angular.element(`<html><body><div>
                            <talend-datetime-picker ng-model="minModel"
                                                    on-mouse-blur="onMouseBlur()">
                            </talend-datetime-picker>
                            </div></body></html>`);
            $compile(element)(scope);
            scope.$digest();

            controller = element.find('talend-datetime-picker').controller('talendDatetimePicker');
        };
    }));

    afterEach(function () {
        jasmine.clock().uninstall();
        scope.$destroy();
        element.remove();
    });

    it('should render input element', function () {
        //when
        createElement();

        //then
        expect(element.find('.datetimepicker').length).toBe(1);
    });

    it('should trigger onBlur callback', function () {
        //given
        createElement();
        spyOn(controller, 'onBlur').and.returnValue();
        scope.$digest();

        //when
        element.find('.datetimepicker').eq(0).blur();
        scope.$digest();

        //then
        expect(controller.onBlur).toHaveBeenCalled();
    });
});
