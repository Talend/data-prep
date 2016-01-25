describe('Feedback Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.feedback'));

    beforeEach(inject(function ($rootScope, $injector, RestURLs) {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should send feed back', inject(function ($rootScope, FeedbackRestService, RestURLs) {
        //given
        var feedback = { title: ''};

        $httpBackend
            .expectPUT(RestURLs.mailUrl, feedback)
            .respond(200);

        //when
        FeedbackRestService.sendFeedback(feedback);
        $httpBackend.flush();

        //then
        //Expect http call to be performed. If not an error is thrown by $httpBackend
    }));

});