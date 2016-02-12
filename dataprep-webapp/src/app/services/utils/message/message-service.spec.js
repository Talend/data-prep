/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Message service', function() {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en_US', {
            'TITLE': 'TITLE_VALUE',
            'CONTENT_WITHOUT_ARG': 'CONTENT_WITHOUT_ARG_VALUE',
            'CONTENT_WITH_ARG': 'CONTENT_WITH_ARG_VALUE : {{argValue}}'
        });
        $translateProvider.preferredLanguage('en_US');
    }));

    beforeEach(inject(function(toaster) {
        spyOn(toaster, 'pop').and.returnValue();
    }));

    it('should show toast on error without translate arg', inject(function($rootScope, MessageService, toaster) {
        //given
        var titleId = 'TITLE';
        var contentId = 'CONTENT_WITHOUT_ARG';

        //when
        MessageService.error(titleId, contentId);
        $rootScope.$digest();

        //then
        expect(toaster.pop).toHaveBeenCalledWith('error', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE', 0);
    }));

    it('should show toast on error with translate arg', inject(function($rootScope, MessageService, toaster) {
        //given
        var titleId = 'TITLE';
        var contentId = 'CONTENT_WITH_ARG';
        var args = {argValue: 'my value'};

        //when
        MessageService.error(titleId, contentId, args);
        $rootScope.$digest();

        //then
        expect(toaster.pop).toHaveBeenCalledWith('error', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value', 0);
    }));

    it('should show toast on warning without translate arg', inject(function($rootScope, MessageService, toaster) {
        //given
        var titleId = 'TITLE';
        var contentId = 'CONTENT_WITHOUT_ARG';

        //when
        MessageService.warning(titleId, contentId);
        $rootScope.$digest();

        //then
        expect(toaster.pop).toHaveBeenCalledWith('warning', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE', 0);
    }));

    it('should show toast on warning with translate arg', inject(function($rootScope, MessageService, toaster) {
        //given
        var titleId = 'TITLE';
        var contentId = 'CONTENT_WITH_ARG';
        var args = {argValue: 'my value'};

        //when
        MessageService.warning(titleId, contentId, args);
        $rootScope.$digest();

        //then
        expect(toaster.pop).toHaveBeenCalledWith('warning', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value', 0);
    }));

    it('should show toast on success without translate arg', inject(function($rootScope, MessageService, toaster) {
        //given
        var titleId = 'TITLE';
        var contentId = 'CONTENT_WITHOUT_ARG';

        //when
        MessageService.success(titleId, contentId);
        $rootScope.$digest();

        //then
        expect(toaster.pop).toHaveBeenCalledWith('success', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE', 5000);
    }));

    it('should show toast on success with translate arg', inject(function($rootScope, MessageService, toaster) {
        //given
        var titleId = 'TITLE';
        var contentId = 'CONTENT_WITH_ARG';
        var args = {argValue: 'my value'};

        //when
        MessageService.success(titleId, contentId, args);
        $rootScope.$digest();

        //then
        expect(toaster.pop).toHaveBeenCalledWith('success', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value', 5000);
    }));
});