/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation choice params directive', function () {
    'use strict';
    var scope, createElement;

    beforeEach(angular.mock.module('data-prep.transformation-form'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function() {
            var element = angular.element('<transform-choice-param parameter="parameter"></transform-choice-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with simple choice', function() {
        //given
        scope.parameter = {
            name: 'myChoice',
            label: 'my choice',
            configuration: {
                values: [
                    {value: 'noParamChoice1', label: 'noParamChoice1'},
                    {value: 'noParamChoice2', label: 'noParamChoice2_label'}
                ]
            }
        };

        //when
        var element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('my choice:');
        expect(element.find('.param-input').length).toBe(1);
        expect(element.find('.param-input').eq(0).find('select').length).toBe(1);
        expect(element.find('.param-input').eq(0).find('option').length).toBe(2);
        expect(element.find('.param-input').eq(0).find('option').eq(0).text()).toBe('noParamChoice1');
        expect(element.find('.param-input').eq(0).find('option').eq(0).attr('value')).toBe('string:noParamChoice1');
        expect(element.find('.param-input').eq(0).find('option').eq(1).text()).toBe('noParamChoice2_label');
        expect(element.find('.param-input').eq(0).find('option').eq(1).attr('value')).toBe('string:noParamChoice2');
    });

    it('should render an action with choice containing parameters', function() {
        //given
        scope.parameter = {
            name: 'my choice',
            configuration: {
                values: [
                    {value: 'noParamChoice'},
                    {value: 'twoParams',
                        parameters: [
                            {
                                name: 'param1',
                                label: 'Param 1',
                                type: 'string',
                                'inputType': 'text',
                                default: '.'
                            },
                            {
                                name: 'param2',
                                label: 'Param 2',
                                type: 'float',
                                'inputType': 'number',
                                default: '5'
                            }
                        ]
                    }
                ]
            }
        };
        var element = createElement();

        //when
        scope.parameter.value = scope.parameter.configuration.values[0].value;
        scope.$apply();

        //then
        expect(element.find('.param-name').length).toBe(1); // choice name only

        //when
        scope.parameter.value = scope.parameter.configuration.values[1].value;
        scope.$apply();

        //then
        expect(element.find('.param-name').length).toBe(3); // choice name + 2 input params name
        expect(element.find('.param-name').eq(1).text().trim()).toBe('Param 1:');
        expect(element.find('.param-name').eq(2).text().trim()).toBe('Param 2:');

        expect(element.find('.param-input').length).toBe(3); // choice + 2 input params
        expect(element.find('.param-input').eq(1).find('input[type="text"]').length).toBe(1);
        expect(element.find('.param-input').eq(2).find('input[type="number"]').length).toBe(1);
    });
});
