/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset parameters directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'DATASET_PARAMETERS': 'Dataset parameters',
            'DATASET_PARAMETERS_ENCODING': 'Encoding',
            'DATASET_PARAMETERS_SEPARATOR': 'Separator'
        });
        $translateProvider.preferredLanguage('en');
    }));
    beforeEach(angular.mock.module('data-prep.dataset-parameters'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.validate = jasmine.createSpy('validation');
        scope.configuration = {
            separators: [
                {label: ';', value: ';'},
                {label: ',', value: ','},
                {label: '<space>', value: ' '},
                {label: '<tab>', value: '\t'}
            ],
            encodings: ['UTF-8', 'UTF-16', 'ISO-8859-1']
        };
        scope.parameters = {separator: ';', encoding: 'UTF-8'};

        createElement = function() {
            var html = '<dataset-parameters ' +
                'processing="processing" ' +
                'dataset="dataset" ' +
                'on-parameters-change="validate(dataset, parameters)" ' +
                'configuration="configuration" ' +
                'parameters="parameters"></dataset-parameters>';

            element = angular.element(html);
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    describe('render', function() {
        it('should render title', function() {
            //when
            createElement();

            //then
            expect(element.find('.dataset-parameters-title').eq(0).text()).toBe('Dataset parameters');
        });

        it('should render encodings', function() {
            //when
            createElement();

            //then
            var encodingContainer = element.find('.dataset-parameters-encoding').eq(0);
            expect(encodingContainer.find('.dataset-parameters-label').eq(0).text().trim()).toBe('Encoding :');

            var encodingOptions = encodingContainer.find('.dataset-parameters-input > select > option');
            expect(encodingOptions.length).toBe(3);
            expect(encodingOptions.eq(0).attr('value')).toBe('string:UTF-8');
            expect(encodingOptions.eq(0).text()).toBe('UTF-8');
            expect(encodingOptions.eq(1).attr('value')).toBe('string:UTF-16');
            expect(encodingOptions.eq(1).text()).toBe('UTF-16');
            expect(encodingOptions.eq(2).attr('value')).toBe('string:ISO-8859-1');
            expect(encodingOptions.eq(2).text()).toBe('ISO-8859-1');
        });

        describe('separator', function() {
            it('should NOT render separators on NON csv', function() {
                //given
                scope.dataset = {type: 'other'};

                //when
                createElement();

                //then
                expect(element.find('.dataset-parameters-separator').length).toBe(0);
            });

            it('should render separators on csv dataset', function() {
                //given
                scope.dataset = {type: 'text/csv'};

                //when
                createElement();

                //then
                var separatorContainer = element.find('.dataset-parameters-separator').eq(0);
                expect(separatorContainer.find('.dataset-parameters-label').eq(0).text().trim()).toBe('Separator :');

                var separatorOptions = separatorContainer.find('.dataset-parameters-input > select > option');
                expect(separatorOptions.length).toBe(5);
                expect(separatorOptions.eq(0).attr('value')).toBe('');
                expect(separatorOptions.eq(0).text()).toBe('Other');
                expect(separatorOptions.eq(1).attr('value')).toBe('string:;');
                expect(separatorOptions.eq(1).text()).toBe(';');
                expect(separatorOptions.eq(2).attr('value')).toBe('string:,');
                expect(separatorOptions.eq(2).text()).toBe(',');
                expect(separatorOptions.eq(3).attr('value')).toBe('string: ');
                expect(separatorOptions.eq(3).text()).toBe('<space>');
                expect(separatorOptions.eq(4).attr('value')).toBe('string:\t');
                expect(separatorOptions.eq(4).text()).toBe('<tab>');
            });

            it('should render custom separator input only when separator is not in the configuration list', function() {
                //given
                scope.dataset = {type: 'text/csv'};
                createElement();

                var separatorContainer = element.find('.dataset-parameters-separator').eq(0);
                expect(separatorContainer.find('.dataset-parameters-input').length).toBe(1);

                //when
                scope.parameters.separator = '|';
                scope.$digest();

                //then
                expect(separatorContainer.find('.dataset-parameters-input').length).toBe(2);
                expect(separatorContainer.find('.dataset-parameters-input').eq(1).find('input').length).toBe(1);
            });
        });

        describe('button', function() {
           it('should enable button when processing is falsy', function() {
               //given
               scope.processing = false;

               //when
               createElement();

               //then
               expect(element.find('button').attr('disabled')).toBeFalsy();
           });

           it('should disable button when processing is truthy', function() {
               //given
               scope.processing = true;

               //when
               createElement();

               //then
               expect(element.find('button').attr('disabled')).toBeTruthy();
           });
        });
    });

    describe('validation', function() {
        it('should call validation callback on form submit', function() {
            //given
            scope.dataset = {id: '54a146cf854b54', type: 'text/csv'};
            scope.parameters.separator = '|';
            scope.parameters.encoding = 'UTF-16';
            createElement();

            expect(scope.validate).not.toHaveBeenCalled();

            //when
            element.find('button').click();

            //then
            expect(scope.validate).toHaveBeenCalledWith(
                {id: '54a146cf854b54', type: 'text/csv'},
                {separator: '|', encoding: 'UTF-16'}
            );
        });
    });
});