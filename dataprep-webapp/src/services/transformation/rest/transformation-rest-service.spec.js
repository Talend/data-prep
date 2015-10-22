describe('Transformation Rest Service', function() {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.transformation'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should call POST transform rest service to get all transformations', inject(function ($rootScope, TransformationRestService, RestURLs) {
        //given
        var column = {'id': 'firstname', 'quality': { 'empty': 0, 'invalid': 0, 'valid': 2 }, 'type': 'string', 'total': 2};
        var response = null;

        var result = [{'category':'case','items':[],'name':'uppercase','value':'','type':'OPERATION','parameters':[{'name':'column_name','type':'string','default':''}]},{'category':'case','items':[],'name':'lowercase','value':'','type':'OPERATION','parameters':[{'name':'column_name','type':'string','default':''}]}];
        $httpBackend
            .expectPOST(RestURLs.transformUrl + '/actions/column', JSON.stringify(column))
            .respond(200, result);

        //when
        TransformationRestService.getTransformations(JSON.stringify(column), true).then(function (resp) {
            response = resp.data;
        });
        $httpBackend.flush();

        //then
        expect(response).toEqual(result);
    }));


    it('should call POST transform rest service to get some suggested transformations', inject(function ($rootScope, TransformationRestService, RestURLs) {
        //given
        var column = {'id': 'firstname', 'quality': { 'empty': 0, 'invalid': 0, 'valid': 2 }, 'type': 'string', 'total': 2};
        var response = null;

        var result = [{'category':'case','items':[],'name':'uppercase','value':'','type':'OPERATION','parameters':[{'name':'column_name','type':'string','default':''}]},{'category':'case','items':[],'name':'lowercase','value':'','type':'OPERATION','parameters':[{'name':'column_name','type':'string','default':''}]}];
        $httpBackend
            .expectPOST(RestURLs.transformUrl + '/suggest/column', JSON.stringify(column))
            .respond(200, result);

        //when
        TransformationRestService.getTransformations(JSON.stringify(column), false).then(function (resp) {
            response = resp.data;
        });
        $httpBackend.flush();

        //then
        expect(response).toEqual(result);
    }));

    it('should call GET transformation dynamic params rest service with preparationId', inject(function ($rootScope, TransformationRestService, RestURLs) {
        //given
        var response = null;
        var action = 'textclustering';
        var columnId = 'firstname';
        var preparationId = '7b89ef45f6';

        var result = {type: 'cluster', details: {titles: ['', '']}};
        $httpBackend
            .expectGET(RestURLs.transformUrl + '/suggest/textclustering/params?preparationId=7b89ef45f6&columnId=firstname')
            .respond(200, result);

        //when
        TransformationRestService.getDynamicParameters(action, columnId, null, preparationId)
            .then(function (resp) {
                response = resp.data;
            });
        $httpBackend.flush();

        //then
        expect(response).toEqual(result);
    }));

    it('should call GET transformation dynamic params rest service with preparationId and stepId', inject(function ($rootScope, TransformationRestService, RestURLs) {
        //given
        var response = null;
        var action = 'textclustering';
        var columnId = 'firstname';
        var preparationId = '7b89ef45f6';
        var stepId = '126578a98bf';

        var result = {type: 'cluster', details: {titles: ['', '']}};
        $httpBackend
            .expectGET(RestURLs.transformUrl + '/suggest/textclustering/params?preparationId=7b89ef45f6&stepId=126578a98bf&columnId=firstname')
            .respond(200, result);

        //when
        TransformationRestService.getDynamicParameters(action, columnId, null, preparationId, stepId)
            .then(function (resp) {
                response = resp.data;
            });
        $httpBackend.flush();

        //then
        expect(response).toEqual(result);
    }));

    it('should call GET transformation dynamic params rest service with datasetId', inject(function ($rootScope, TransformationRestService, RestURLs) {
        //given
        var response = null;
        var action = 'textclustering';
        var columnId = 'firstname';
        var datasetId = '7b89ef45f6';

        var result = {type: 'cluster', details: {titles: ['', '']}};
        $httpBackend
            .expectGET(RestURLs.transformUrl + '/suggest/textclustering/params?datasetId=7b89ef45f6&columnId=firstname')
            .respond(200, result);

        //when
        TransformationRestService.getDynamicParameters(action, columnId, datasetId, null)
            .then(function (resp) {
                response = resp.data;
            });
        $httpBackend.flush();

        //then
        expect(response).toEqual(result);
    }));
});