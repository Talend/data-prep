describe('Feedback state', function () {
    'use strict';

    beforeEach(module('data-prep.services.state'));

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
