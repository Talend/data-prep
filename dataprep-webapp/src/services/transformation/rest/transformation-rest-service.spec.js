describe('Transformation Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.transformation'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
    }));

    describe('transformations/suggestions', function() {
        var column = {
            'id': 'firstname',
            'quality': {'empty': 0, 'invalid': 0, 'valid': 2},
            'type': 'string',
            'total': 2
        };

        var result = [
            {
                'category': 'case',
                'items': [],
                'name': 'uppercase',
                'value': '',
                'type': 'OPERATION',
                'parameters': [{'name': 'column_name', 'type': 'string', 'default': ''}]
            },
            {
                'category': 'case',
                'items': [],
                'name': 'lowercase',
                'value': '',
                'type': 'OPERATION',
                'parameters': [{'name': 'column_name', 'type': 'string', 'default': ''}]
            }
        ];

        it('should call POST transform rest service to get all transformations', inject(function ($rootScope, TransformationRestService, RestURLs) {
            //given
            var response = null;
            $httpBackend
                .expectPOST(RestURLs.transformUrl + '/actions/column', column)
                .respond(200, result);

            //when
            TransformationRestService.getTransformations(column)
                .then(function (resp) {
                    response = resp.data;
                });
            $httpBackend.flush();

            //then
            expect(response).toEqual(result);
        }));

        it('should call POST transform rest service to get column suggestions', inject(function ($rootScope, TransformationRestService, RestURLs) {
            //given
            var response = null;
            $httpBackend
                .expectPOST(RestURLs.transformUrl + '/suggest/column', column)
                .respond(200, result);

            //when
            TransformationRestService.getSuggestions(column)
                .then(function (resp) {
                    response = resp.data;
                });
            $httpBackend.flush();

            //then
            expect(response).toEqual(result);
        }));
    });

    describe('dynamic parameters', function() {
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
});