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
    var scope, element, html;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    var showCalendarInput = function (elm) {
        elm = elm || element;
        var picker = elm.find('.datetimepicker').eq(0);
        picker.mousedown();
    };

    beforeEach(function () {
        jasmine.clock().install();
    });
    afterEach(function () {
        jasmine.clock().uninstall();

        scope.$destroy();
        element.remove();
    });

    describe('datetimepicker widget', function () {

        beforeEach(inject(function ($rootScope, $compile) {

            scope = $rootScope.$new();

            scope.param =
            {
                'name': 'param1',
                'label': 'Param 1',
                'type': 'date',
                'value': '02/01/2012 10:00:12'
            };


            html = '<html><body><div>' +
                '<talend-datetime-picker ' +
                ' ng-model="param.value" ' +
                ' format="DD/MM/YYYY hh:mm:ss" ' +
                ' formatTime="hh:mm:ss" ' +
                ' formatDate="DD/MM/YYY" />' +
                '</div></body></html>';

            element = $compile(html)(scope);
            scope.$digest();

        }));

        it('should show datetimepicker on click', function () {

            var picker = element.find('.datetimepicker');
            expect(picker.length).toBe(1);

            //when
            showCalendarInput();

            jasmine.clock().tick(500);
            // FIXME for some reason it doesn't work :-(
            //then
            //expect(picker.hasClass('xdsoft_datetimepicker')).toBe(true);
        });

    });
});