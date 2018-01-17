/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Quality bar controller', () => {

    let createController;
    let scope;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = () => {
            return $controller('QualityBarCtrl', {
                $scope: scope,
            });
        };
    }));

    it('should calculate simple hash from quality values', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 10,
            empty: 5,
            valid: 72,
        };

        // when
        const hash = ctrl.hashQuality();

        // then
        expect(hash).toBe('51072');
    });

    it('should compute rounded percentages', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 10,
            empty: 5,
            valid: 72,
        };

        // when
        ctrl.computePercent();

        // then
        expect(ctrl.percent.empty).toBe(6);
        expect(ctrl.percent.invalid).toBe(11);
        expect(ctrl.percent.valid).toBe(83);
    });

    it('should compute width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 33,
            valid: 68,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(26);
        expect(ctrl.width.invalid).toBe(20);
        expect(ctrl.width.valid).toBe(54);
    });

    it('should set min width to empty width, and reduce the other width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 1,
            valid: 68,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(10);
        expect(ctrl.width.invalid).toBe(24);
        expect(ctrl.width.valid).toBe(66);
    });

    it('should set min width to invalid width, and reduce the other width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 1,
            empty: 25,
            valid: 68,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(24);
        expect(ctrl.width.invalid).toBe(10);
        expect(ctrl.width.valid).toBe(66);
    });

    it('should set min width to valid width, and reduce the other width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 68,
            valid: 1,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(66);
        expect(ctrl.width.invalid).toBe(24);
        expect(ctrl.width.valid).toBe(10);
    });

    it('should set 0 to empty width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 0,
            valid: 68,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(0);
        expect(ctrl.width.invalid).toBe(27);
        expect(ctrl.width.valid).toBe(73);
    });

    it('should set 0 to invalid width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 0,
            empty: 25,
            valid: 68,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(27);
        expect(ctrl.width.invalid).toBe(0);
        expect(ctrl.width.valid).toBe(73);
    });

    it('should set 0 to valid width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 68,
            valid: 0,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(73);
        expect(ctrl.width.invalid).toBe(27);
        expect(ctrl.width.valid).toBe(0);
    });

    it('should reduce width to the bigger only when the others are at minimal width', () => {
        // given
        const ctrl = createController();
        ctrl.quality = {
            invalid: 100000,
            empty: 1,
            valid: 1,
        };

        // when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        // then
        expect(ctrl.width.empty).toBe(10);
        expect(ctrl.width.invalid).toBe(80);
        expect(ctrl.width.valid).toBe(10);
    });
});
