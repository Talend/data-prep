/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

function injectBrush(ctrl) {
    const brush = { extent: jasmine.createSpy('brush.extent') };
    const brushgTransition = { call: jasmine.createSpy('brushg.transition().call()') };
    const brushg = { transition: jasmine.createSpy('brushg.transition()').and.returnValue(brushgTransition) };

    ctrl.brush = brush;
    ctrl.brushg = brushg;
}

describe('Range slider controller', () => {
    let scope;
    let createController;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new(true);

        createController = () => $controller('RangeSliderCtrl', { $scope: scope });
    }));

    describe('init', () => {
        it('should set date format', () => {
            // when
            const ctrl = createController();

            // then
            expect(ctrl.dateFormat).toBe('YYYY-MM-DD');
        });
    });

    describe('utils', () => {
        describe('#isDateType', () => {
            it('should return true on date limits', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'date' };

                // when
                const isDate = ctrl.isDateType();

                // then
                expect(isDate).toBe(true);
            });

            it('should return true on date limits', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };

                // when
                const isDate = ctrl.isDateType();

                // then
                expect(isDate).toBe(false);
            });
        });

        describe('#adaptToInputValue', () => {
            it('should convert number to string', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };

                // when
                const adaptedValues = ctrl.adaptToInputValue({
                    min: 25,
                    max: 45,
                });

                // then
                expect(adaptedValues).toEqual({
                    min: '25',
                    max: '45',
                });
            });

            it('should convert timestamp to date string', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'date' };

                // when
                const adaptedValues = ctrl.adaptToInputValue({
                    min: +new Date('2016/06/27'),
                    max: +new Date('2018/06/27'),
                });

                // then
                expect(adaptedValues).toEqual({
                    min: '2016-06-27',
                    max: '2018-06-27',
                });
            });
        });

        describe('#adaptFromInputValue', () => {
            it('should convert string to number', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };

                // when
                const adaptedValues = ctrl.adaptFromInputValue({
                    min: '25',
                    max: '45',
                });

                // then
                expect(adaptedValues).toEqual({
                    min: 25,
                    max: 45,
                });
            });

            it('should convert string to timestamp', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'date' };

                // when
                const adaptedValues = ctrl.adaptFromInputValue({
                    min: '2016-06-27',
                    max: '2018-06-27',
                });

                // then
                expect(adaptedValues).toEqual({
                    min: +new Date('2016/06/27'),
                    max: +new Date('2018/06/27'),
                });
            });
        });

        describe('#getLimitsText', () => {
            it('should create limits text on number', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 25.25,
                    max: 45.636,
                };

                // when
                const limitsTexts = ctrl.getLimitsText();

                // then
                expect(limitsTexts).toEqual({
                    minText: '25.25',
                    maxText: '45.636',
                });
            });

            it('should create limits text on date', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'date',
                    min: +new Date('2016/06/27'),
                    max: +new Date('2018/06/27'),
                };

                // when
                const limitsTexts = ctrl.getLimitsText();

                // then
                expect(limitsTexts).toEqual({
                    minText: '2016-06-27',
                    maxText: '2018-06-27',
                });
            });
        });

        describe('#adaptToInboundValues', () => {
            it('should set min/max to the limits if the value is out of limits', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10,
                    max: 45,
                };

                // when
                const adaptedValues = ctrl.adaptToInboundValues({
                    min: 2,
                    max: 50,
                });

                // then
                expect(adaptedValues).toEqual({
                    min: 10,
                    max: 45,
                });
            });
        });
    });

    describe('model', () => {
        describe('#initModel', () => {
            it('should set number of decimal configuration to the bigger decimal number', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10.2,
                    max: 45.6896,
                };
                expect(ctrl.nbDecimals).not.toBe(4);

                // when
                ctrl.initModel();

                // then
                expect(ctrl.nbDecimals).toBe(4);
            });

            it('should set brush last values', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10.2,
                    max: 45.6896,
                    minBrush: 12,
                    maxBrush: 25,
                };
                expect(ctrl.lastValues).not.toBeDefined();

                // when
                ctrl.initModel();

                // then
                expect(ctrl.lastValues.brush).toEqual({
                    min: 12,
                    max: 25,
                });
            });

            it('should set brush last values to min/max if they are not defined', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10.2,
                    max: 45.6896,
                    // minBrush not defined
                    // maxBrush not defined
                };
                expect(ctrl.lastValues).not.toBeDefined();

                // when
                ctrl.initModel();

                // then
                expect(ctrl.lastValues.brush).toEqual({
                    min: 10.2,
                    max: 45.6896,
                });
            });

            it('should set input last values', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10.2,
                    max: 45.6896,
                    minFilterVal: 12,
                    maxFilterVal: 25,
                };
                expect(ctrl.lastValues).not.toBeDefined();

                // when
                ctrl.initModel();

                // then
                expect(ctrl.lastValues.input).toEqual({
                    min: 12,
                    max: 25,
                });
            });

            it('should set brush last values to min/max if they are not defined', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10.2,
                    max: 45.6896,
                    // minFilterVal not defined
                    // maxFilterVal not defined
                };
                expect(ctrl.lastValues).not.toBeDefined();

                // when
                ctrl.initModel();

                // then
                expect(ctrl.lastValues.input).toEqual({
                    min: 10.2,
                    max: 45.6896,
                });
            });
        });

        describe('#setLastBrushValues', () => {
            it('should set last brush values', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10,
                    max: 45,
                };
                ctrl.lastValues = { brush: {} };

                // when
                ctrl.setLastBrushValues({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.lastValues.brush).toEqual({
                    min: 12,
                    max: 40,
                });
            });
        });

        describe('#setLastInputValues', () => {
            it('should set last input values', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = {
                    type: 'number',
                    min: 10,
                    max: 45,
                };
                ctrl.lastValues = { input: {} };

                // when
                ctrl.setLastInputValues({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.lastValues.input).toEqual({
                    min: 12,
                    max: 40,
                });
            });
        });
    });

    describe('brush', () => {
        describe('#updateBrush', () => {
            it('should update brush values', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.nbDecimals = 2;

                // when
                ctrl.updateBrush({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.brush.extent).toHaveBeenCalledWith([12, 40]);
                expect(ctrl.brushg.transition).toHaveBeenCalled();
                expect(ctrl.brushg.transition().call).toHaveBeenCalled();
            });

            it('should introduce a delta when brush values min/max are the same', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.nbDecimals = 2;

                // when
                ctrl.updateBrush({
                    min: 10,
                    max: 10,
                });

                // then
                expect(ctrl.brush.extent).toHaveBeenCalledWith([10, 10.0001]);
                expect(ctrl.brushg.transition).toHaveBeenCalled();
                expect(ctrl.brushg.transition().call).toHaveBeenCalled();
            });
        });
    });

    describe('input', () => {
        describe('#setInputValue', () => {
            it('should convert number to string and update input model', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = {};

                // when
                ctrl.setInputValue({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.minMaxModel).toEqual({
                    min: '12',
                    max: '40',
                });
            });

            it('should convert timestamp to date string and update input model', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'date' };
                ctrl.minMaxModel = {};

                // when
                ctrl.setInputValue({
                    min: +new Date('2016/06/27'),
                    max: +new Date('2018/06/27'),
                });

                // then
                expect(ctrl.minMaxModel).toEqual({
                    min: '2016-06-27',
                    max: '2018-06-27',
                });
            });
        });

        describe('#resetInputValues', () => {
            it('should hide error message', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.lastValues = {
                    input: {
                        min: 12,
                        max: 40,
                    },
                };
                ctrl.minMaxModel = {};
                ctrl.invalidNumber = true;          // error visible
                ctrl.invalidNumberWithComma = true; // error visible

                // when
                ctrl.resetInputValues();

                // then
                expect(ctrl.invalidNumber).toBe(false);
                expect(ctrl.invalidNumberWithComma).toBe(false);
            });

            it('should set input model to lest registered input values', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.lastValues = {
                    input: {
                        min: 12,
                        max: 40,
                    },
                };
                ctrl.minMaxModel = {};

                // when
                ctrl.resetInputValues();

                // then
                expect(ctrl.minMaxModel).toEqual({
                    min: '12',
                    max: '40',
                });
            });
        });

        describe('#showMsgErr', () => {
            it('should show invalid number error message', () => {
                // given
                const ctrl = createController();
                ctrl.minMaxModel = { min: '12', max: '40' };
                ctrl.invalidNumber = false;

                // when
                ctrl.showMsgErr();

                // then
                expect(ctrl.invalidNumber).toBe(true);
            });

            it('should show invalid number decimal error message when comma is used', () => {
                // given
                const ctrl = createController();
                ctrl.minMaxModel = { min: '12,25', max: '40' };
                ctrl.invalidNumberWithComma = false;

                // when
                ctrl.showMsgErr();

                // then
                expect(ctrl.invalidNumberWithComma).toBe(true);
            });

            it('should NOT show invalid number decimal error message when comma is NOT used', () => {
                // given
                const ctrl = createController();
                ctrl.minMaxModel = { min: '12', max: '40' };
                ctrl.invalidNumberWithComma = false;

                // when
                ctrl.showMsgErr();

                // then
                expect(ctrl.invalidNumberWithComma).toBe(false);
            });
        });

        describe('#hideMsgErr', () => {
            it('should hide error messages', () => {
                // given
                const ctrl = createController();
                ctrl.invalidNumber = true;
                ctrl.invalidNumberWithComma = true;

                // when
                ctrl.hideMsgErr();

                // then
                expect(ctrl.invalidNumber).toBe(false);
                expect(ctrl.invalidNumberWithComma).toBe(false);
            });
        });

        describe('#inputsAreValid', () => {
            it('should return true with number input', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = { min: '25', max: '18' };

                // when
                const valid = ctrl.inputsAreValid();

                // then
                expect(valid).toBeTruthy();
            });

            it('should return false with non number input', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = { min: 'aze', max: '18' };

                // when
                const valid = ctrl.inputsAreValid();

                // then
                expect(valid).toBeFalsy();
            });

            it('should return true with valid date format input', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'date' };
                ctrl.minMaxModel = { min: '2015-01-25', max: '2016-03-18' };

                // when
                const valid = ctrl.inputsAreValid();

                // then
                expect(valid).toBeTruthy();
            });

            it('should return false with non valid date format input', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'date' };
                ctrl.minMaxModel = { min: '2015-01-25', max: 'aze' };

                // when
                ctrl.hideMsgErr();

                // when
                const valid = ctrl.inputsAreValid();

                // then
                expect(valid).toBeFalsy();
            });
        });

        describe('#validateInputs', () => {
            it('should hide error messages when inputs are valid', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = { min: '25', max: '18' };
                ctrl.invalidNumber = true;

                // when
                ctrl.validateInputs();

                // then
                expect(ctrl.invalidNumber).toBe(false);
            });

            it('should show error messages when inputs are invalid', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = { min: 'aze', max: '18' };
                ctrl.invalidNumber = false;

                // when
                ctrl.validateInputs();

                // then
                expect(ctrl.invalidNumber).toBe(true);
            });
        });

        describe('#handleKey', () => {
            it('should validate on ENTER', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = { min: '12', max: '25' };
                ctrl.lastValues = {
                    input: { min: '10', max: '45' },
                    brush: { min: 10, max: 45 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.handleKey({ keyCode: 13 });

                // then
                expect(ctrl.onBrushEnd).toHaveBeenCalledWith({
                    interval: { min: 12, max: 25, isMaxReached: false },
                });
            });

            it('should reset inputs on ESC', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number' };
                ctrl.minMaxModel = { min: '12', max: '25' };
                ctrl.lastValues = { input: { min: '10', max: '45' } };

                // when
                ctrl.handleKey({ keyCode: 27 });

                // then
                expect(ctrl.minMaxModel).toEqual({ min: '10', max: '45' });
            });
        });
    });

    describe('propagation', () => {
        describe('#onChange', () => {
            it('should propagate to parent component', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number', max: 30 };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onChange({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.onBrushEnd).toHaveBeenCalledWith({
                    interval: { min: 12, max: 40, isMaxReached: true },
                });
            });
        });

        describe('#onBrushChange', () => {
            it('should set last registered brush values', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number', max: 30 };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onBrushChange({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.lastValues.brush).toEqual({ min: 12, max: 40 });
            });

            it('should propagate to parent component', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number', max: 30 };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onBrushChange({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.lastValues.input).toEqual({ min: 12, max: 40 });
            });

            it('should propagate to parent component', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number', max: 30 };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onBrushChange({
                    min: 12,
                    max: 40,
                });

                // then
                expect(ctrl.onBrushEnd).toHaveBeenCalledWith({
                    interval: { min: 12, max: 40, isMaxReached: true },
                });
            });
        });

        describe('#onInputChange', () => {
            it('should update brush with (within bounds) values', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.rangeLimits = { type: 'number', min: 5, max: 30 };
                ctrl.minMaxModel = { min: '10', max: '115' };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onInputChange();

                // then
                expect(ctrl.brush.extent).toHaveBeenCalledWith([10, 30]);
                expect(ctrl.brushg.transition).toHaveBeenCalled();
                expect(ctrl.brushg.transition().call).toHaveBeenCalled();
            });

            it('should set last registered brush (within bounds) values', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.rangeLimits = { type: 'number', min: 5, max: 30 };
                ctrl.minMaxModel = { min: '10', max: '115' };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onInputChange();

                // then
                expect(ctrl.lastValues.brush).toEqual({ min: 10, max: 30 });
            });

            it('should set last registered input values', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.rangeLimits = { type: 'number', min: 5, max: 30 };
                ctrl.minMaxModel = { min: '10', max: '115' };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onInputChange();

                // then
                expect(ctrl.lastValues.input).toEqual({ min: 10, max: 115 });
            });

            it('should propagate change to parent component', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.rangeLimits = { type: 'number', min: 5, max: 30 };
                ctrl.minMaxModel = { min: '10', max: '115' };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onInputChange();

                // then
                expect(ctrl.onBrushEnd).toHaveBeenCalledWith({
                    interval: { min: 10, max: 115, isMaxReached: true },
                });
            });

            it('should do nothing when inputs have not changed', () => {
                // given
                const ctrl = createController();
                injectBrush(ctrl);
                ctrl.rangeLimits = { type: 'number', min: 10, max: 115 };
                ctrl.minMaxModel = { min: '10', max: '25' };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onInputChange();

                // then
                expect(ctrl.onBrushEnd).not.toHaveBeenCalled();
            });

            it('should reset input values when they are invalid', () => {
                // given
                const ctrl = createController();
                ctrl.rangeLimits = { type: 'number', max: 30 };
                ctrl.minMaxModel = { min: 'aze', max: '115' };
                ctrl.lastValues = {
                    brush: { min: 10, max: 25 },
                    input: { min: 10, max: 25 },
                };
                ctrl.onBrushEnd = jasmine.createSpy('onBrushEnd()');

                // when
                ctrl.onInputChange();

                // then
                expect(ctrl.onBrushEnd).not.toHaveBeenCalled();
                expect(ctrl.minMaxModel).toEqual({ min: '10', max: '25' });
            });
        });
    });
});
