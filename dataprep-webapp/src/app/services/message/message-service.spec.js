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
        it('should show toast on error without translate arg', inject((MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.error(titleId, contentId);

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('error', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE');
        }));

        it('should show toast on error with translate arg', inject((MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITH_ARG';
            var args = { argValue: 'my value' };

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.error(titleId, contentId, args);

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('error', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value');
        }));
    });

    describe('warning', () => {
        it('should show toast on warning without translate arg', inject((MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.warning(titleId, contentId);

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('warning', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE');
        }));

        it('should show toast on warning with translate arg', inject((MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITH_ARG';
            var args = { argValue: 'my value' };

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.warning(titleId, contentId, args);

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('warning', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value');
        }));
    });

    describe('success', () => {
        it('should show toast on success without translate arg', inject((MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.success(titleId, contentId);

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('success', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE');
        }));

        it('should show toast on success with translate arg', inject((MessageService, StateService) => {
            //given
            var titleId = 'TITLE';
            var contentId = 'CONTENT_WITH_ARG';
            var args = { argValue: 'my value' };

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.success(titleId, contentId, args);

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('success', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value');
        }));
    });

    describe('buffer', () => {
        it('should buffer different messages and display them after 300ms delay', inject((MessageService, StateService) => {
            //given
            var firstMessageTitleId = 'TITLE';
            var firstMessageContentId = 'CONTENT_WITH_ARG';
            var firstMessageArgs = { argValue: 'my value' };

            var secondMessageTitleId = 'TITLE';
            var secondMessageContentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.success(firstMessageTitleId, firstMessageContentId, firstMessageArgs);
            MessageService.error(secondMessageTitleId, secondMessageContentId);
            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //then
            expect(StateService.pushMessage).toHaveBeenCalledWith('success', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value');
            expect(StateService.pushMessage).toHaveBeenCalledWith('error', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE');
        }));

        it('should only display distinct message once', inject((MessageService, StateService) => {
            //given
            var firstMessageTitleId = 'TITLE';
            var firstMessageContentId = 'CONTENT_WITH_ARG';
            var firstMessageArgs = { argValue: 'my value' };

            var secondMessageTitleId = 'TITLE';
            var secondMessageContentId = 'CONTENT_WITHOUT_ARG';

            expect(StateService.pushMessage).not.toHaveBeenCalled();

            //when
            MessageService.success(firstMessageTitleId, firstMessageContentId, firstMessageArgs);
            MessageService.success(firstMessageTitleId, firstMessageContentId, firstMessageArgs);
            MessageService.success(firstMessageTitleId, firstMessageContentId, firstMessageArgs);
            MessageService.error(secondMessageTitleId, secondMessageContentId);

            //then
            expect(StateService.pushMessage.calls.count()).toBe(2);
            expect(StateService.pushMessage).toHaveBeenCalledWith('success', 'TITLE_VALUE', 'CONTENT_WITH_ARG_VALUE : my value');
            expect(StateService.pushMessage).toHaveBeenCalledWith('error', 'TITLE_VALUE', 'CONTENT_WITHOUT_ARG_VALUE');
        }));
    });
});
