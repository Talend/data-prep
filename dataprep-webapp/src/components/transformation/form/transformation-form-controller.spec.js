/*jshint camelcase: false */

describe('Transform params controller', function () {
    'use strict';

    var createController, scope, extractedParams, transformation;

    beforeEach(module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        extractedParams = null;
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformFormCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.transformation = transformation;
            ctrlFn.instance.onSubmit = function(args) {
                extractedParams = args.params;
            };
            return ctrlFn();
        };
    }));

    it('should extract param', function() {
        //given
        transformation = {
            name: 'uppercase',
            category: 'case',
            parameters: [
                {name: 'param1', type: 'text', default: ''},
                {name: 'param2', type: 'integer', default: '5'}
            ]
        };
        var ctrl = createController(transformation);
        ctrl.transformation.parameters[0].value = 'param1Value';
        ctrl.transformation.parameters[1].value = 4;

        //when
        ctrl.transformWithParam();

        //then
        expect(extractedParams).toEqual({ param1: 'param1Value', param2: 4 });
    });

    it('should extract param with default value', function() {
        //given
        transformation = {
            name: 'uppercase',
            category: 'case',
            parameters: [
                {name: 'param1', type: 'text', default: ''},
                {name: 'param2', type: 'integer', default: '5'}
            ]
        };
        var ctrl = createController(transformation);
        //when
        ctrl.transformWithParam();

        //then
        expect(extractedParams).toEqual({ param1: '', param2: '5'});
    });

    it('should extract simple choice param', function() {
        //given
        transformation = {
            name: 'split',
            category: 'split',
            parameters: [
                {
                    name: 'mode',
                    type: 'select',
                    configuration: {
                        values: [
                            {name: 'regex', value: 'regex'},
                            {name: 'index', value: 'index'}
                        ]
                    }
                }
            ]
        };
        var ctrl = createController();
        ctrl.transformation.parameters[0].value = ctrl.transformation.parameters[0].configuration.values[1].value;

        //when
        ctrl.transformWithParam();

        //then
        expect(extractedParams).toEqual({ mode: 'index'});
    });

    it('should extract parameterized choice params', function() {
        //given
        transformation = {
            name: 'split',
            category: 'split',
            parameters: [
                {
                    name: 'mode',
                    type: 'select',
                    configuration: {
                        values: [
                            {
                                name: 'regex',
                                value: 'regex',
                                parameters : [
                                    {name: 'regex', type: 'text', default: '', value: 'param1Value'},
                                    {name: 'comment', type: 'text', default: '', value: 'my comment'}
                                ]
                            },
                            {name: 'index', value: 'index'}
                        ]
                    }
                }
            ]
        };
        var ctrl = createController();
        ctrl.transformation.parameters[0].value = ctrl.transformation.parameters[0].configuration.values[0].value;

        //when
        ctrl.transformWithParam();

        //then
        expect(extractedParams).toEqual({ mode: 'regex', regex: 'param1Value', comment: 'my comment'});
    });

    it('should extract parameters and parameterized choice transformation select', function() {

        //given
        transformation = {
            name: 'split',
            category: 'split',
            parameters: [
                {name: 'param1', type: 'text', default: ''},
                {name: 'param2', type: 'integer', default: '5'},
                {
                    name: 'mode',
                    type: 'select',
                    configuration: {
                        values: [
                            {
                                name: 'regex',
                                value: 'regex',
                                parameters : [
                                    {name: 'regex', type: 'text', default: '', value: 'param1Value'},
                                    {name: 'comment', type: 'text', default: '', value: 'my comment'}
                                ]
                            },
                            {name: 'index', value: 'index'}
                        ]
                    }
                }
            ]
        };
        var ctrl = createController();
        ctrl.transformation.parameters[2].value = ctrl.transformation.parameters[2].configuration.values[0].value;
        ctrl.transformation.parameters[0].value = 'param1Value';
        ctrl.transformation.parameters[1].value = 4;

        //when
        ctrl.transformWithParam();

        //then
        expect(extractedParams).toEqual({ mode: 'regex', regex: 'param1Value', comment: 'my comment', param1: 'param1Value', param2: 4});
    });

    it('should extract cluster parameters', function() {
        //given
        transformation = {
            name: 'cluster',
            category: 'quickfix',
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
        var ctrl = createController();
        ctrl.transformation.cluster.clusters[0].active = false;
        ctrl.transformation.cluster.clusters[1].active = true;
        ctrl.transformation.cluster.clusters[1].parameters[0].value = false;
        ctrl.transformation.cluster.clusters[1].parameters[1].value = true;
        ctrl.transformation.cluster.clusters[1].parameters[2].value = true;
        ctrl.transformation.cluster.clusters[1].parameters[3].value = true;
        ctrl.transformation.cluster.clusters[1].parameters[4].value = true;
        ctrl.transformation.cluster.clusters[1].replace.value = 'Toto';

        //when
        ctrl.transformWithParam();

        //then
        expect(extractedParams).toEqual({
            'Masachusetts': 'Toto',
            'Massachussetts': 'Toto',
            'Massachusets': 'Toto',
            'Masachussets': 'Toto'
        });
    });

    it('should extract param and call ctrl onSubmitHoverOn function', function() {
        //given
        transformation = {
            name: 'uppercase',
            category: 'case',
            parameters: [
                {name: 'param1', type: 'text', default: ''},
                {name: 'param2', type: 'integer', default: '5'}
            ]
        };
        var ctrl = createController();
        ctrl.transformation.parameters[0].value = 'param1Value';
        ctrl.transformation.parameters[1].value = 4;

        var hoverArgs = {};
        ctrl.onSubmitHoverOn = function(args) {
            hoverArgs = args;
        };

        //when
        ctrl.submitHoverOn();

        //then
        expect(hoverArgs.params).toEqual({ param1: 'param1Value', param2: 4 });
    });

    it('should extract param and call ctrl onSubmitHoverOff function', function() {
        //given
        transformation = {
            name: 'uppercase',
            category: 'case',
            parameters: [
                {name: 'param1', type: 'text', default: ''},
                {name: 'param2', type: 'integer', default: '5'}
            ]
        };
        var ctrl = createController();
        ctrl.transformation.parameters[0].value = 'param1Value';
        ctrl.transformation.parameters[1].value = 4;

        var hoverArgs = {};
        ctrl.onSubmitHoverOff = function(args) {
            hoverArgs = args;
        };

        //when
        ctrl.submitHoverOff();

        //then
        expect(hoverArgs.params).toEqual({ param1: 'param1Value', param2: 4 });
    });
});