describe('Transformation params directive', function () {
    'use strict';
    var scope, createElement, extractedParams;

    beforeEach(module('data-prep.transformation-form'));
    beforeEach(module('htmlTemplates'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        extractedParams = null;
        scope = $rootScope.$new();
        scope.onSubmit = function (args) {
            extractedParams = args.params;
        };

        createElement = function () {
            var element = angular.element('<transform-params transformation="transformation" on-submit="onSubmit"></transform-params>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with parameters', function () {
        //given
        scope.transformation = {
            name: 'menuWithParam',
            label: 'menu with param',
            parameters: [
                {
                    'name': 'param1',
                    'label': 'Param 1',
                    'type': 'string',
                    'inputType': 'text',
                    'default': '.'
                },
                {
                    'name': 'param2',
                    'label': 'Param 2',
                    'type': 'integer',
                    'inputType': 'number',
                    'default': '5'
                }
            ]
        };

        //when
        var element = createElement();

        //then
        expect(element.find('.param-name').length).toBe(2);
        expect(element.find('.param-name').eq(0).text().trim()).toBe('Param 1:');
        expect(element.find('.param-name').eq(1).text().trim()).toBe('Param 2:');
        expect(element.find('.param-input').length).toBe(2);
        expect(element.find('.param-input').eq(0).find('input[type="text"]').length).toBe(1);
        expect(element.find('.param-input').eq(1).find('input[type="number"]').length).toBe(1);
    });

    it('should render an action with simple choice', function () {
        //given
        scope.transformation = {
            name: 'menuXithParam',
            label: 'menu with param',
            parameters: [
                {
                    'name': 'myChoice',
                    'label': 'my choice',
                    'type': 'select',
                    'configuration': {
                        'values': [
                            {name: 'noParamChoice1', value: 'noParamChoice1'},
                            {name: 'noParamChoice2', value: 'noParamChoice2'}
                        ]
                    },
                    'default': ''
                }
            ]
        };

        //when
        var element = createElement();

        //then
        var paramChoice = element.find('.param').eq(0);
        expect(paramChoice.find('.param-name').length).toBe(1);
        expect(paramChoice.find('.param-name').eq(0).text().trim()).toBe('my choice:');
        expect(paramChoice.find('.param-input').length).toBe(1);
        expect(paramChoice.find('.param-input').eq(0).find('select').length).toBe(1);
        expect(paramChoice.find('.param-input').eq(0).find('option').length).toBe(2);
        expect(paramChoice.find('.param-input').eq(0).find('option').eq(0).text()).toBe('noParamChoice1');
        expect(paramChoice.find('.param-input').eq(0).find('option').eq(1).text()).toBe('noParamChoice2');
    });

    it('should render an action with choice containing parameters', function () {
        //given
        scope.transformation = {
            name: 'menu with param',
            parameters: [
                {
                    'name': 'my choice',
                    'label': 'my choice',
                    'type': 'select',
                    'configuration': {
                        'values': [
                            {name: 'noParamChoice', value: 'noParamChoice'},
                            {
                                name: 'twoParams',
                                value: 'twoParams',
                                parameters: [
                                    {
                                        label: 'Param 1',
                                        name: 'param1',
                                        type: 'string',
                                        default: '.'
                                    },
                                    {
                                        label: 'Param 2',
                                        name: 'param2',
                                        type: 'float',
                                        default: '5'
                                    }
                                ]
                            }
                        ]
                    },
                    'default': ''
                }
            ]
        };
        var element = createElement();
        var paramChoice = element.find('.param').eq(0);

        //when
        scope.transformation.parameters[0].value = scope.transformation.parameters[0].configuration.values[0];
        scope.$digest();

        //then
        expect(paramChoice.find('.param-name').length).toBe(1); // choice name only

        //when
        scope.transformation.parameters[0].value = scope.transformation.parameters[0].configuration.values[1].value;
        scope.$digest();

        //then
        expect(paramChoice.find('.param-name').length).toBe(3); // choice name + 2 input params name
        expect(paramChoice.find('.param-name').eq(1).text().trim()).toBe('Param 1:');
        expect(paramChoice.find('.param-name').eq(2).text().trim()).toBe('Param 2:');

        expect(paramChoice.find('.param-input').length).toBe(3); // choice + 2 input params
        expect(paramChoice.find('.param-input').eq(1).find('input[type="text"]').length).toBe(1);
        expect(paramChoice.find('.param-input').eq(2).find('input[type="number"]').length).toBe(1);
    });

    it('should render an action with cluster parameters', function () {
        //given
        scope.transformation = {
            name: 'menu with param',
            cluster: {
                titles: [
                    'We found these values',
                    'And we\'ll keep this value'
                ],
                clusters: [
                    {
                        parameters: [
                            {
                                name: 'Texa',
                                type: 'boolean',
                                description: 'parameter.Texa.desc',
                                label: 'parameter.Texa.label',
                                default: 'true'
                            },
                            {
                                name: 'Tixass',
                                type: 'boolean',
                                description: 'parameter.Tixass.desc',
                                label: 'parameter.Tixass.label',
                                default: 'true'
                            },
                            {
                                name: 'Tex@s',
                                type: 'boolean',
                                description: 'parameter.Tex@s.desc',
                                label: 'parameter.Tex@s.label',
                                default: 'true'
                            }
                        ],
                        'replace': {
                            name: 'replaceValue',
                            type: 'string',
                            description: 'parameter.replaceValue.desc',
                            label: 'parameter.replaceValue.label',
                            default: 'Texas'
                        }
                    },
                    {
                        parameters: [
                            {
                                name: 'Massachusetts',
                                type: 'boolean',
                                description: 'parameter.Massachusetts.desc',
                                label: 'parameter.Massachusetts.label',
                                default: 'false'
                            },
                            {
                                name: 'Masachusetts',
                                type: 'boolean',
                                description: 'parameter.Masachusetts.desc',
                                label: 'parameter.Masachusetts.label',
                                default: 'true'
                            },
                            {
                                name: 'Massachussetts',
                                type: 'boolean',
                                description: 'parameter.Massachussetts.desc',
                                label: 'parameter.Massachussetts.label',
                                default: 'true'
                            },
                            {
                                name: 'Massachusets',
                                type: 'boolean',
                                description: 'parameter.Massachusets.desc',
                                label: 'parameter.Massachusets.label',
                                default: 'true'
                            },
                            {
                                name: 'Masachussets',
                                type: 'boolean',
                                description: 'parameter.Masachussets.desc',
                                label: 'parameter.Masachussets.label',
                                default: 'true'
                            }
                        ],
                        replace: {
                            name: 'replaceValue',
                            type: 'string',
                            description: 'parameter.replaceValue.desc',
                            label: 'parameter.replaceValue.label',
                            default: 'Massachussets'
                        }
                    }
                ]
            }
        };

        //when
        var element = createElement();

        //then
        expect(element.find('.cluster').length).toBe(1);
    });
});
