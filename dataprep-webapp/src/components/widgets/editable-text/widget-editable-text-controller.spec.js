describe('Editable Text Widget', function() {
    'use strict';

    var createController, scope;
    var onValidateFn, onCancelFn;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        onValidateFn = jasmine.createSpy('onValidateFn');
        onCancelFn = jasmine.createSpy('onCancelFn');

        createController = function () {
            var ctrlFn = $controller('TalendEditableTextCtrl', {
                $scope: scope
            }, true);

            ctrlFn.instance.onValidate = onValidateFn;
            ctrlFn.instance.onCancel = onCancelFn;
            return ctrlFn();
        };
    }));

    it('should set edition text to the original text', function() {
        //given
        var ctrl = createController();
        ctrl.text = 'Jimmy';

        expect(ctrl.editionText).toBeFalsy();

        //when
        ctrl.reset();

        //then
        expect(ctrl.editionText).toBe('Jimmy');
    });

    describe('edit mode', function() {
        it('should set edition text to the original text', function() {
            //given
            var ctrl = createController();
            ctrl.text = 'Jimmy';

            expect(ctrl.editionText).toBeFalsy();

            //when
            ctrl.edit();

            //then
            expect(ctrl.editionText).toBe('Jimmy');
        });

        it('should sswitch to edition mode', function() {
            //given
            var ctrl = createController();
            ctrl.editionMode = false;

            //when
            ctrl.edit();

            //then
            expect(ctrl.editionMode).toBe(true);
        });
    });

    describe('validation', function() {
        it('should execute validation callback when text has changed', function() {
            //given
            var ctrl = createController();
            ctrl.text = 'Jimmy';
            ctrl.editionText = 'new Jimmy';

            //when
            ctrl.validate();

            //then
            expect(onValidateFn).toHaveBeenCalled();
        });

        it('should NOT execute validation callback when text has NOT changed', function() {
            //given
            var ctrl = createController();
            ctrl.text = 'Jimmy';
            ctrl.editionText = 'Jimmy';

            //when
            ctrl.validate();

            //then
            expect(onValidateFn).not.toHaveBeenCalled();
        });

        it('should switch to non edition mode', function() {
            //given
            var ctrl = createController();
            ctrl.editionMode = true;

            //when
            ctrl.validate();

            //then
            expect(ctrl.editionMode).toBe(false);
        });
    });

    describe('cancel', function() {
        it('should execute cancel callback', function() {
            //given
            var ctrl = createController();

            //when
            ctrl.cancel();

            //then
            expect(onCancelFn).toHaveBeenCalled();
        });

        it('should switch to non edition mode', function() {
            //given
            var ctrl = createController();
            ctrl.editionMode = true;

            //when
            ctrl.cancel();

            //then
            expect(ctrl.editionMode).toBe(false);
        });
    });
});