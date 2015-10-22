describe('Statistics REST service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.statistics'));

    beforeEach(inject(function ($injector, RestURLs) {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should get aggregation column data', inject(function ($rootScope, RestURLs, StatisticsRestService) {
        //given
        var data = null;
        var params = {};
        var response = {
            data: [
                {'data': 'Lansing', 'occurrences': 15},
                {'data': 'Helena', 'occurrences': 5},
                {'data': 'Baton Rouge', 'occurrences': 64},
                {'data': 'Annapolis', 'occurrences': 4},
                {'data': 'Pierre', 'occurrences': 104}
            ]
        };

        $httpBackend
            .expectPOST(RestURLs.aggregationUrl, params)
            .respond(200, response);

        //when
        StatisticsRestService.getAggregations(params)
            .then(function(response) {
                data = response.data;
            });
        $httpBackend.flush();

        //then
        expect(data).toEqual(response.data);
    }));

    it('should get aggregation column data from cache', inject(function ($rootScope, RestURLs, StatisticsRestService) {
        //given
        var data = null;
        var params = {};
        var response = {
            data: [
                {'data': 'Lansing', 'occurrences': 15},
                {'data': 'Helena', 'occurrences': 5},
                {'data': 'Baton Rouge', 'occurrences': 64},
                {'data': 'Annapolis', 'occurrences': 4},
                {'data': 'Pierre', 'occurrences': 104}
            ]
        };

        //given : mock rest service and call it to set result in cache
        $httpBackend
            .expectPOST(RestURLs.aggregationUrl, params)
            .respond(200, response);
        StatisticsRestService.getAggregations(params);
        $httpBackend.flush();

        //when : no mock for rest call here
        StatisticsRestService.getAggregations(params)
            .then(function(response) {
                data = response.data;
            });
        $rootScope.$digest();

        //then
        expect(data).toEqual(response.data);
    }));

    it('should reset cache', inject(function ($rootScope, RestURLs, StatisticsRestService) {
        //given
        var params = {};
        var response = {
            data: [
                {'data': 'Lansing', 'occurrences': 15},
                {'data': 'Helena', 'occurrences': 5},
                {'data': 'Baton Rouge', 'occurrences': 64},
                {'data': 'Annapolis', 'occurrences': 4},
                {'data': 'Pierre', 'occurrences': 104}
            ]
        };

        //given : mock rest service and call it to set result in cache
        $httpBackend
            .expectPOST(RestURLs.aggregationUrl, params)
            .respond(200, response);
        StatisticsRestService.getAggregations(params);
        $httpBackend.flush();

        //when
        StatisticsRestService.resetCache();

        //then : mock rest call again, it will throw an error if no rest call is performed
        $httpBackend
            .expectPOST(RestURLs.aggregationUrl, params)
            .respond(200, response);
        StatisticsRestService.getAggregations(params);
        $rootScope.$digest();
    }));

});
