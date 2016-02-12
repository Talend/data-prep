/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Confirm widget service', function() {
    'use strict';

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en_US', {
            'TEXT_1': 'TEXT_1_VALUE',
            'TEXT_2': 'TEXT_2_VALUE',
            'TEXT_3': 'TEXT_3_VALUE : {{argValue}}'
        });
        $translateProvider.preferredLanguage('en_US');
    }));

    afterEach(inject(function($timeout, TalendConfirmService) {
        if(TalendConfirmService.element) {
            TalendConfirmService.resolve();
            $timeout.flush();
        }
    }));

    it('should create scope and confirm element with options', inject(function($rootScope, TalendConfirmService) {
        //given
        var text1 = 'TEXT_1';
        var text2 = 'TEXT_2';
        var body = angular.element('body');
        expect(TalendConfirmService.modalScope).toBeFalsy();
        expect(TalendConfirmService.element).toBeFalsy();
        expect(body.has('talend-confirm').length).toBe(0);

        //when
        TalendConfirmService.confirm({disableEnter: true}, [text1, text2]);
        $rootScope.$digest();

        //then
        expect(TalendConfirmService.modalScope).toBeTruthy();
        expect(TalendConfirmService.modalScope.texts).toEqual(['TEXT_1_VALUE', 'TEXT_2_VALUE']);
        expect(TalendConfirmService.modalScope.disableEnter).toBe(true);
        expect(TalendConfirmService.element).toBeTruthy();
        expect(TalendConfirmService.element.scope()).toBe(TalendConfirmService.modalScope);

        expect(body.has('talend-confirm').length).toBe(1);
    }));

    it('should create scope and confirm element without options', inject(function($rootScope, TalendConfirmService) {
        //given
        var text1 = 'TEXT_1';
        var text2 = 'TEXT_2';
        var body = angular.element('body');
        expect(TalendConfirmService.modalScope).toBeFalsy();
        expect(TalendConfirmService.element).toBeFalsy();
        expect(body.has('talend-confirm').length).toBe(0);

        //when
        TalendConfirmService.confirm(null, [text1, text2]);
        $rootScope.$digest();

        //then
        expect(TalendConfirmService.modalScope).toBeTruthy();
        expect(TalendConfirmService.modalScope.texts).toEqual(['TEXT_1_VALUE', 'TEXT_2_VALUE']);
        expect(TalendConfirmService.modalScope.disableEnter).toBeFalsy();
        expect(TalendConfirmService.element).toBeTruthy();
        expect(TalendConfirmService.element.scope()).toBe(TalendConfirmService.modalScope);

        expect(body.has('talend-confirm').length).toBe(1);
    }));

    it('should create scope and confirm element with translate arguments', inject(function($rootScope, TalendConfirmService) {
        //given
        var text1 = 'TEXT_1';
        var text3 = 'TEXT_3';
        var body = angular.element('body');
        expect(TalendConfirmService.modalScope).toBeFalsy();
        expect(TalendConfirmService.element).toBeFalsy();
        expect(body.has('talend-confirm').length).toBe(0);

        //when
        TalendConfirmService.confirm(null, [text1, text3], {argValue: 'my value'});
        $rootScope.$digest();

        //then
        expect(TalendConfirmService.modalScope).toBeTruthy();
        expect(TalendConfirmService.modalScope.texts).toEqual(['TEXT_1_VALUE', 'TEXT_3_VALUE : my value']);
        expect(TalendConfirmService.element).toBeTruthy();
        expect(TalendConfirmService.element.scope()).toBe(TalendConfirmService.modalScope);

        expect(body.has('talend-confirm').length).toBe(1);
    }));

    describe('with existing confirm', function() {
        var promise, element, scope;

        beforeEach(inject(function($rootScope, TalendConfirmService) {
            promise = TalendConfirmService.confirm();
            $rootScope.$digest();

            scope = TalendConfirmService.modalScope;
            element = TalendConfirmService.element;

            spyOn(element, 'remove').and.returnValue();
        }));

        it('should throw error on confirm create but another confirm modal is already created', inject(function($timeout, TalendConfirmService) {
            //when
            try {
                TalendConfirmService.confirm();
            }

            //then
            catch(error) {
                expect(error.message).toBe('A confirm popup is already created');
                TalendConfirmService.resolve();
                $timeout.flush();
                return;
            }
            throw Error('should have thrown error on second confirm() call');
        }));

        it('should resolve promise and remove/destroy scope and element', inject(function($timeout, TalendConfirmService) {
            //given
            var resolved = false;
            promise.then(function() {
                resolved = true;
            });

            var scopeDestroyed = false;
            scope.$on('$destroy', function() {
                scopeDestroyed = true;
            });

            //when
            TalendConfirmService.resolve();
            $timeout.flush();

            //then
            expect(resolved).toBe(true);
            expect(scopeDestroyed).toBe(true);
            expect(element.remove).toHaveBeenCalled();
            expect(TalendConfirmService.modalScope).toBeFalsy();
            expect(TalendConfirmService.element).toBeFalsy();
        }));

        it('should reject promise and remove/destroy scope and element', inject(function($timeout, TalendConfirmService) {
            //given
            var cause = false;
            promise.catch(function(error) {
                cause = error;
            });

            var scopeDestroyed = false;
            scope.$on('$destroy', function() {
                scopeDestroyed = true;
            });

            //when
            TalendConfirmService.reject('dismiss');
            $timeout.flush();

            //then
            expect(cause).toBe('dismiss');
            expect(scopeDestroyed).toBe(true);
            expect(element.remove).toHaveBeenCalled();
            expect(TalendConfirmService.modalScope).toBeFalsy();
            expect(TalendConfirmService.element).toBeFalsy();
        }));
    });
});