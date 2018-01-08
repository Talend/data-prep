/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Disable right click directive', function () {
    'use strict';

    var element;
    var createElement;

    beforeEach(angular.mock.module('data-prep.services.utils'));

    beforeEach(inject(function ($rootScope, $compile) {
        createElement = function () {
            element = angular.element('<div disable-right-click></div>');
            $compile(element)($rootScope.$new());
        };
    }));

    it('should prevent default behavior on right click', function () {
        //given
        createElement();
        var event = angular.element.Event('contextmenu');

        spyOn(event, 'preventDefault').and.returnValue();

        //when
        element.trigger(event);

        //then
        expect(event.preventDefault).toHaveBeenCalled();
    });
});
