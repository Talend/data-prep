/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset parameters controller', function () {
    'use strict';

    var scope;
    var createController;

    beforeEach(angular.mock.module('data-prep.dataset-parameters'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new(true);

        createController = function () {
            var ctrl = $controller('DatasetParametersCtrl', {
                $scope: scope,
            });
            ctrl.configuration = {
                separators: [
                    { label: ';', value: ';' },
                    { label: ',', value: ',' },
                    { label: '<space>', value: ' ' },
                    { label: '<tab>', value: '\t' },
                ],
            };
            return ctrl;
        };
    }));

    describe('separator in list', function () {
        it('should return true when current separator is an item of the separators list', function () {
            //given
            var ctrl = createController();
            ctrl.parameters = {
                separator: ';',
            };

            //when
            var result = ctrl.separatorIsInList();

            //then
            expect(result).toBeTruthy();
        });

        it('should return false when current separator is NOT an item of the separators list', function () {
            //given
            var ctrl = createController();
            ctrl.parameters = {
                separator: '|',
            };

            //when
            var result = ctrl.separatorIsInList();

            //then
            expect(result).toBeFalsy();
        });

        it('should return false when current separator is falsy', function () {
            //given
            var ctrl = createController();
            ctrl.parameters = {
                separator: '',
            };

            //when
            var result = ctrl.separatorIsInList();

            //then
            expect(result).toBeFalsy();
        });
    });

    describe('validate', function () {
        it('should return true when current separator is an item of the separators list', function () {
            //given
            var ctrl = createController();
            ctrl.dataset = { id: '1348b684f2e548' };
            ctrl.onParametersChange = jasmine.createSpy('on parameter change');
            ctrl.parameters = {
                separator: ';',
                textEnclosureCharacter: '"',
                escapeCharacter: '"',
                encoding: 'UTF-8',
            };
            expect(ctrl.onParametersChange).not.toHaveBeenCalledWith();

            //when
            ctrl.validate();

            //then
            expect(ctrl.onParametersChange).toHaveBeenCalledWith({
                dataset: { id: '1348b684f2e548' },
                parameters: {
                    separator: ';',
                    textEnclosureCharacter: '"',
                    escapeCharacter: '"',
                    encoding: 'UTF-8',
                },
            });
        });

        it('should allow empty escape character and text enclosure character', function () {
            //given
            var ctrl = createController();
            ctrl.dataset = { id: '1348b684f2e548' };
            ctrl.onParametersChange = jasmine.createSpy('on parameter change');
            ctrl.parameters = {
                separator: ';',
                textEnclosureCharacter: '',
                escapeCharacter: '',
                encoding: 'UTF-8',
            };
            expect(ctrl.onParametersChange).not.toHaveBeenCalledWith();

            //when
            ctrl.validate();

            //then
            expect(ctrl.onParametersChange).toHaveBeenCalledWith({
                dataset: { id: '1348b684f2e548' },
                parameters: {
                    separator: ';',
                    textEnclosureCharacter: '',
                    escapeCharacter: '',
                    encoding: 'UTF-8',
                },
            });
        });
    });
});
