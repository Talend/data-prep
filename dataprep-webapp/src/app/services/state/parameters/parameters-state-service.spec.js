/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Playground state service', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.state'));

    describe('init', function () {
        it('should init visible to false', inject(function (parametersState) {
            //then
            expect(parametersState.visible).toBe(false);
        }));

        it('should init isSending to false', inject(function (parametersState) {
            //then
            expect(parametersState.isSending).toBe(false);
        }));

        it('should init separators list', inject(function (parametersState) {
            //then
            expect(parametersState.configuration.separators).toEqual([
                { label: ';', value: ';' },
                { label: ',', value: ',' },
                { label: '<space>', value: ' ' },
                { label: '<tab>', value: '\t' },
                { label: '|', value: '|' },
            ]);
        }));
    });

    describe('visibility', function () {
        it('should hide', inject(function (parametersState, ParametersStateService) {
            //given
            parametersState.visible = true;

            //when
            ParametersStateService.hide();

            //then
            expect(parametersState.visible).toBe(false);
        }));

        it('should show', inject(function (parametersState, ParametersStateService) {
            //given
            parametersState.visible = false;

            //when
            ParametersStateService.show();

            //then
            expect(parametersState.visible).toBe(true);
        }));
    });

    describe('sending flag', function () {
        it('should set sending flag', inject(function (parametersState, ParametersStateService) {
            //given
            parametersState.isSending = false;

            //when
            ParametersStateService.setIsSending(true);

            //then
            expect(parametersState.isSending).toBe(true);
        }));
    });

    describe('encoding', function () {
        it('should set encodings list', inject(function (parametersState, ParametersStateService) {
            //given
            var encodings = ['UTF-8', 'UTF-16'];
            parametersState.configuration.encodings = [];

            //when
            ParametersStateService.setEncodings(encodings);

            //then
            expect(parametersState.configuration.encodings).toBe(encodings);
        }));
    });

    describe('update', function () {
        it('should update dataset separator', inject(function (parametersState, ParametersStateService) {
            //given
            var dataset = { parameters: { SEPARATOR: ',' } };
            parametersState.values.separator = '\t';

            //when
            ParametersStateService.update(dataset);

            //then
            expect(parametersState.values.separator).toBe(',');
        }));

        it('should update dataset text enclosure character', inject(function (parametersState, ParametersStateService) {
            //given
            var dataset = { parameters: { TEXT_ENCLOSURE_CHAR: '#' } };
            parametersState.values.separator = '\t';

            //when
            ParametersStateService.update(dataset);

            //then
            expect(parametersState.values.textEnclosureCharacter).toBe('#');
        }));

        it('should update dataset escape character', inject(function (parametersState, ParametersStateService) {
            //given
            var dataset = { parameters: { ESCAPE_CHAR: '@' } };
            parametersState.values.separator = '\t';

            //when
            ParametersStateService.update(dataset);

            //then
            expect(parametersState.values.escapeCharacter).toBe('@');
        }));

        it('should update dataset encoding', inject(function (parametersState, ParametersStateService) {
            //given
            var dataset = { encoding: 'UTF-8', parameters: {} };
            parametersState.values.encoding = 'UTF-16';

            //when
            ParametersStateService.update(dataset);

            //then
            expect(parametersState.values.encoding).toBe('UTF-8');
        }));
    });

    describe('reset', function () {
        it('should reset parameters', inject(function (parametersState, ParametersStateService) {
            //given
            parametersState.visible = true;
            parametersState.isSending = true;
            parametersState.values.separator = ',';
            parametersState.values.encoding = 'UTF-8';
            parametersState.values.escapeCharacter = '#';
            parametersState.values.textEnclosureCharacter = '@';

            //when
            ParametersStateService.reset();

            //then
            expect(parametersState.visible).toBe(false);
            expect(parametersState.isSending).toBe(false);
            expect(parametersState.values.separator).toBe(null);
            expect(parametersState.values.encoding).toBe(null);
            expect(parametersState.values.textEnclosureCharacter).toBe(null);
            expect(parametersState.values.escapeCharacter).toBe(null);
        }));
    });
});
