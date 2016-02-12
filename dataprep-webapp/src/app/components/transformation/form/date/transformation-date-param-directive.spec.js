/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation date param directive', function () {
    'use strict';
    var scope, createElement;

    beforeEach(angular.mock.module('data-prep.transformation-form'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function() {
            var element = angular.element('<transform-date-param parameter="parameter"></transform-date-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with parameter', function() {
        //given
        scope.parameter = {
            'name': 'param1',
            'label': 'Param 1',
            'type': 'date',
            'default': '02/01/2012 10:00:12'
        };

        //when
        var element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('Param 1:');
        expect(element.find('.datetimepicker').val()).toBe('02/01/2012 10:00:12');
    });
});
