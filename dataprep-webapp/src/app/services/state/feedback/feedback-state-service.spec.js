/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Feedback state', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.state'));

    it('should init visibility to false', inject(function (feedbackState) {
        //then
        expect(feedbackState.visible).toBe(false);
    }));

    it('should show feedback', inject(function (feedbackState, FeedbackStateService) {
        //given
        feedbackState.visible = false;

        //when
        FeedbackStateService.show();

        //then
        expect(feedbackState.visible).toBe(true);
    }));

    it('should hide feedback', inject(function (feedbackState, FeedbackStateService) {
        //given
        feedbackState.visible = true;

        //when
        FeedbackStateService.hide();

        //then
        expect(feedbackState.visible).toBe(false);
    }));
});
