describe('Transformation cluster params directive', function () {
    'use strict';
    var scope, createElement;

    var clusterDetails = function () {
        return {
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
        };
    };

    beforeEach(module('data-prep.transformation-form'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();
        scope.details = clusterDetails();

        createElement = function() {
            var element = angular.element('<transform-cluster-params details="details"></transform-cluster-params>');
            $compile(element)(scope);
            $timeout.flush();
            scope.$digest();
            return element;
        };
    }));

    it('should render titles', function() {
        //when
        var element = createElement();

        //then
        expect(element.find('thead th').eq(0).find('input[type="checkbox"]').length).toBe(1);
        expect(element.find('thead th').eq(1).text().trim()).toBe('We found these values');
        expect(element.find('thead th').eq(2).text().trim()).toBe('And we\'ll keep this value');
    });

    it('should render clusters', function() {
        //when
        var element = createElement();

        //then
        var firstRow = element.find('.cluster-body > .cluster-line').eq(0);
        expect(firstRow.find('>td').eq(0).find('input[type="checkbox"]').length).toBe(1);
        expect(firstRow.find('>td').eq(1).find('input[type="checkbox"]').length).toBe(3);
        expect(firstRow.find('>td').eq(2).find('input[type="text"]').length).toBe(1);

        var secondRow = element.find('.cluster-body > .cluster-line').eq(1);
        expect(secondRow.find('>td').eq(0).find('input[type="checkbox"]').length).toBe(1);
        expect(secondRow.find('>td').eq(1).find('input[type="checkbox"]').length).toBe(5);
        expect(secondRow.find('>td').eq(2).find('input[type="text"]').length).toBe(1);
    });

    it('should uncheck global activation checkbox', function() {
        //given
        var element = createElement();
        var allCheck = element.find('thead th').eq(0).find('input[type="checkbox"]').eq(0);

        var firstRow = element.find('.cluster-body > .cluster-line').eq(0);
        var secondRow = element.find('.cluster-body > .cluster-line').eq(1);
        var firstRowCheckbox = firstRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);
        var secondRowCheckbox = secondRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);

        expect(allCheck.is(':checked')).toBe(true);
        expect(firstRowCheckbox.is(':checked')).toBe(true);
        expect(secondRowCheckbox.is(':checked')).toBe(true);

        //when
        firstRowCheckbox.click();
        scope.$digest();

        //then
        expect(allCheck.is(':checked')).toBe(false);
        expect(firstRowCheckbox.is(':checked')).toBe(false);
        expect(secondRowCheckbox.is(':checked')).toBe(true);
    });

    it('should uncheck all cluster activation checkbox', function() {
        //given
        var element = createElement();
        var allCheck = element.find('thead th').eq(0).find('input[type="checkbox"]').eq(0);

        var firstRow = element.find('.cluster-body > .cluster-line').eq(0);
        var secondRow = element.find('.cluster-body > .cluster-line').eq(1);
        var firstRowCheckbox = firstRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);
        var secondRowCheckbox = secondRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);

        expect(allCheck.is(':checked')).toBe(true);
        expect(firstRowCheckbox.is(':checked')).toBe(true);
        expect(secondRowCheckbox.is(':checked')).toBe(true);

        //when
        allCheck.click();
        scope.$digest();

        //then
        expect(firstRowCheckbox.is(':checked')).toBe(false);
        expect(secondRowCheckbox.is(':checked')).toBe(false);
    });

    it('should check all cluster activation checkbox', function() {
        //given
        var element = createElement();
        var allCheck = element.find('thead th').eq(0).find('input[type="checkbox"]').eq(0);

        var firstRow = element.find('.cluster-body > .cluster-line').eq(0);
        var secondRow = element.find('.cluster-body > .cluster-line').eq(1);
        var firstRowCheckbox = firstRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);
        var secondRowCheckbox = secondRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);

        allCheck.click();
        scope.$digest();

        expect(allCheck.is(':checked')).toBe(false);
        expect(firstRowCheckbox.is(':checked')).toBe(false);
        expect(secondRowCheckbox.is(':checked')).toBe(false);

        //when
        allCheck.click();
        scope.$digest();

        //then
        expect(allCheck.is(':checked')).toBe(true);
        expect(firstRowCheckbox.is(':checked')).toBe(true);
        expect(secondRowCheckbox.is(':checked')).toBe(true);
    });

    it('should update style on "active --> inactive" cluster row', function() {
        //given
        var element = createElement();
        var firstRow = element.find('.cluster-body > .cluster-line').eq(0);
        var firstRowCheckbox = firstRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);

        expect(firstRow.hasClass('disabled')).toBe(false);
        expect(firstRow.find('input:not(.cluster-activation)').is(':disabled')).toBe(false);

        //when
        firstRowCheckbox.click();
        scope.$digest();

        //then
        expect(firstRow.hasClass('disabled')).toBe(true);
        expect(firstRow.find('input:not(.cluster-activation)').is(':disabled')).toBe(true);
        expect(firstRow.find('select:not(.cluster-activation)').is(':disabled')).toBe(true); //editable select
    });

    it('should update style on "inactive --> active" cluster row', function() {
        //given
        var element = createElement();
        var firstRow = element.find('.cluster-body > .cluster-line').eq(0);
        var firstRowCheckbox = firstRow.find('>td').eq(0).find('input[type="checkbox"]').eq(0);

        firstRowCheckbox.click();
        scope.$digest();

        expect(firstRow.hasClass('disabled')).toBe(true);
        expect(firstRow.find('input:not(.cluster-activation)').is(':disabled')).toBe(true);

        //when
        firstRowCheckbox.click();
        scope.$digest();

        //then
        expect(firstRow.hasClass('disabled')).toBe(false);
        expect(firstRow.find('input:not(.cluster-activation)').is(':disabled')).toBe(false);
        expect(firstRow.find('select:not(.cluster-activation)').is(':disabled')).toBe(false); //editable select
    });
});