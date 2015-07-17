describe('Dataset Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.dataset'));

    beforeEach(inject(function ($rootScope, $injector) {
        $httpBackend = $injector.get('$httpBackend');

        spyOn($rootScope, '$emit').and.callThrough();
    }));

    it('should call dataset list rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var result = null;
        var datasets = [
            {name: 'Customers (50 lines)'},
            {name: 'Us states'},
            {name: 'Customers (1K lines)'}
        ];
        $httpBackend
            .expectGET(RestURLs.datasetUrl)
            .respond(200, datasets);

        //when
        DatasetRestService.getDatasets().then(function (response) {
            result = response.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(datasets);
    }));

    it('should call dataset creation rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var datasetId = null;
        var dataset = {name: 'my dataset', file: {path: '/path/to/file'}, error: false};

        $httpBackend
            .expectPOST(RestURLs.datasetUrl + '?name=my%20dataset')

            .respond(200, 'e85afAa78556d5425bc2');

        //when
        DatasetRestService.create(dataset).then(function (res) {
            datasetId = res.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(datasetId).toBe('e85afAa78556d5425bc2');
    }));

    it('should call dataset creation rest service with import parameters for remote http', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var datasetId = null;
        var importParameters = {
            type: 'http',
            name: 'greatremotedataset',
            url:  'moc.dnelat//:ptth'
        };
        var headers = {'Content-Type': 'application/vnd.remote-ds.http', 'Accept':'application/json, text/plain, */*'};

        $httpBackend
            .expectPOST(RestURLs.datasetUrl + '?name=greatremotedataset', importParameters, headers)
            .respond(200, '54g5g4d3fg4d3f5q5g4');

        //when
        DatasetRestService.import(importParameters).then(function (res) {
            datasetId = res.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(datasetId).toBe('54g5g4d3fg4d3f5q5g4');
    }));

    it('should call dataset update rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var dataset = {name: 'my dataset', file: {path: '/path/to/file'}, error: false, id: 'e85afAa78556d5425bc2'};

        $httpBackend
            .expectPUT(RestURLs.datasetUrl + '/e85afAa78556d5425bc2?name=my%20dataset')
            .respond(200);

        //when
        DatasetRestService.update(dataset);
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        //expect PUT not to throw any exception
    }));

    it('should call dataset delete rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var dataset = {name: 'my dataset', file: {path: '/path/to/file'}, error: false, id: 'e85afAa78556d5425bc2'};

        $httpBackend
            .expectDELETE(RestURLs.datasetUrl + '/e85afAa78556d5425bc2')
            .respond(200);

        //when
        DatasetRestService.delete(dataset);
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        //expect DELETE not to throw any exception
    }));

    it('should call dataset get content rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var result = null;
        var datasetId = 'e85afAa78556d5425bc2';
        var data = [{column: [], records: []}];

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/e85afAa78556d5425bc2?metadata=false')
            .respond(200, data);

        //when
        DatasetRestService.getContent(datasetId, false).then(function (data) {
            result = data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(data);
    }));

    it('should call dataset certification rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var datasetId = 'e85afAa78556d5425bc2';

        $httpBackend
            .expectPUT(RestURLs.datasetUrl + '/e85afAa78556d5425bc2/processcertification')
            .respond(200);

        //when
        DatasetRestService.processCertification(datasetId);
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        //expect PUT not to throw any exception
    }));

    it('should call dataset metadata update rest service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var metadata = {id: 'e85afAa78556d5425bc2', name: 'my dataset'};

        $httpBackend
            .expectPOST(RestURLs.datasetUrl + '/e85afAa78556d5425bc2', metadata)
            .respond(200);

        //when
        DatasetRestService.updateMetadata(metadata);
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        //expect POST not to throw any exception
    }));

    it('should call dataset sheet preview service with default sheet', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var result = null;
        var datasetId = 'e85afAa78556d5425bc2';
        var data = {columns: [{id: 'col1'}], records: [{col1: 'toto'}, {col1: 'tata'}]};

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true')
            .respond(200, {data: data});

        //when
        DatasetRestService.getSheetPreview(datasetId)
            .then(function(response) {
                result = response.data;
            });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(data);
    }));

    it('should call dataset sheet preview service with provided sheet', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var result = null;
        var datasetId = 'e85afAa78556d5425bc2';
        var sheetName = 'my sheet';
        var data = {columns: [{id: 'col1'}], records: [{col1: 'toto'}, {col1: 'tata'}]};

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true&sheetName=my%20sheet')
            .respond(200, {data: data});

        //when
        DatasetRestService.getSheetPreview(datasetId, sheetName)
            .then(function(response) {
                result = response.data;
            });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(result).toEqual(data);
    }));

    it('should show loading on call dataset sheet preview service', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var datasetId = 'e85afAa78556d5425bc2';
        var sheetName = 'my sheet';
        var data = {columns: [{id: 'col1'}], records: [{col1: 'toto'}, {col1: 'tata'}]};

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true&sheetName=my%20sheet')
            .respond(200, {data: data});

        //when
        DatasetRestService.getSheetPreview(datasetId, sheetName);
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
    }));

    it('should call set favorite', inject(function ($rootScope, DatasetRestService, RestURLs) {
        //given
        var dataset = {name: 'my dataset', file: {path: '/path/to/file'}, error: false, id: 'e85afAa78556d5425bc2',
        favorite:true};

        $httpBackend
            .expectPOST(RestURLs.datasetUrl + '/favorite/e85afAa78556d5425bc2?unset=true')
            .respond(200);

        //when
        DatasetRestService.toggleFavorite(dataset);
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        //expect POST no to throw any exception;
    }));

});