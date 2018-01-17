/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Loading directive', function () {
    'use strict';

    var scope;
    var element;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        var html = '<talend-loading></talend-dropdown>';
        element = $compile(html)(scope);
        scope.$digest();
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should immediatly add "is-loading" class when "talend.loading.start" is emitted', inject(function ($rootScope) {
        //given
        expect(element.hasClass('is-loading')).toBe(false);

        //when
        $rootScope.$emit('talend.loading.start');
        $rootScope.$digest();

        //then
        expect(element.hasClass('is-loading')).toBe(true);
    }));

    it('should add "show-loading" class after 200ms when "talend.loading.start" is emitted', inject(function ($timeout, $rootScope) {
        //given
        expect(element.hasClass('show-loading')).toBe(false);

        //when
        $rootScope.$emit('talend.loading.start');
        $rootScope.$digest();
        expect(element.hasClass('show-loading')).toBe(false);
        $timeout.flush(200);

        //then
        expect(element.hasClass('show-loading')).toBe(true);
    }));

    it('should remove "show-loading" class when "talend.loading.start" is emitted', inject(function ($rootScope) {
        //given
        element.addClass('is-loading show-loading');

        //when
        $rootScope.$emit('talend.loading.stop');
        $rootScope.$digest();

        //then
        expect(element.hasClass('show-loading')).toBe(false);
        expect(element.hasClass('is-loading')).toBe(false);
    }));
});
