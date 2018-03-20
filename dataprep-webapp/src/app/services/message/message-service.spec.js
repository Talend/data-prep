/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Message service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.message'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en_US', {
            TITLE: 'TITLE_VALUE',
            CONTENT_WITHOUT_ARG: 'CONTENT_WITHOUT_ARG_VALUE',
            CONTENT_WITH_ARG: 'CONTENT_WITH_ARG_VALUE : {{argValue}}',
        });
        $translateProvider.preferredLanguage('en_US');
    }));

    beforeEach(inject((StateService) => {
        spyOn(StateService, 'pushMessage').and.returnValue();
    }));

    describe('error', () => {
        it('should show toast on error without translate arg', inject(($rootScope, MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.error(titleId, contentId);
            $rootScope.$apply();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith({ type: 'error', title: 'TITLE_VALUE', message: 'CONTENT_WITHOUT_ARG_VALUE' });
        }));

        it('should show toast on error with translate arg', inject(($rootScope, MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITH_ARG';
            var args = { argValue: 'my value' };

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.error(titleId, contentId, args);
            $rootScope.$apply();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith({ type: 'error', title: 'TITLE_VALUE', message: 'CONTENT_WITH_ARG_VALUE : my value' });
        }));
    });

    describe('warning', () => {
        it('should show toast on warning without translate arg', inject(($rootScope, MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.warning(titleId, contentId);
            $rootScope.$apply();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith({ type: 'warning', title: 'TITLE_VALUE', message: 'CONTENT_WITHOUT_ARG_VALUE' });
        }));

        it('should show toast on warning with translate arg', inject(($rootScope, MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITH_ARG';
            var args = { argValue: 'my value' };

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.warning(titleId, contentId, args);
            $rootScope.$apply();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith({ type: 'warning', title: 'TITLE_VALUE', message: 'CONTENT_WITH_ARG_VALUE : my value' });
        }));
    });

    describe('info', () => {
        it('should show toast on success without translate arg', inject(($rootScope, MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.success(titleId, contentId);
            $rootScope.$apply();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith({ type: 'info', title: 'TITLE_VALUE', message: 'CONTENT_WITHOUT_ARG_VALUE' });
        }));

        it('should show toast on success with translate arg', inject(($rootScope, MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITH_ARG';
            var args = { argValue: 'my value' };

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.success(titleId, contentId, args);
            $rootScope.$apply();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith({ type: 'info', title: 'TITLE_VALUE', message: 'CONTENT_WITH_ARG_VALUE : my value' });
        }));
    });
});
