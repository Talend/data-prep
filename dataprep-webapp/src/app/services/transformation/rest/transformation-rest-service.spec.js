/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(angular.mock.module('data-prep.services.transformation'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
    }));

    describe('transformations/suggestions', function() {
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

        describe('column', function() {
            var column = {
                'id': 'firstname',
                'quality': {'empty': 0, 'invalid': 0, 'valid': 2},
                'type': 'string',
                'total': 2
            };

            it('should call POST transform rest service to get transformations', inject(function ($rootScope, TransformationRestService, RestURLs) {
                //given
                var response = null;
                $httpBackend
                    .expectPOST(RestURLs.transformUrl + '/actions/column', column)
                    .respond(200, result);

                //when
                TransformationRestService.getColumnTransformations(column)
                    .then(function (resp) {
                        response = resp.data;
                    });
                $httpBackend.flush();

                //then
                expect(response).toEqual(result);
            }));

            it('should call POST transform rest service to get suggestions', inject(function ($rootScope, TransformationRestService, RestURLs) {
                //given
                var response = null;
                $httpBackend
                    .expectPOST(RestURLs.transformUrl + '/suggest/column', column)
                    .respond(200, result);

                //when
                TransformationRestService.getColumnSuggestions(column)
                    .then(function (resp) {
                        response = resp.data;
                    });
                $httpBackend.flush();

                //then
                expect(response).toEqual(result);
            }));
        });

        describe('line', function() {
            it('should call GET transform rest service to get transformations', inject(function ($rootScope, TransformationRestService, RestURLs) {
                //given
                var response = null;
                $httpBackend
                    .expectGET(RestURLs.transformUrl + '/actions/line')
                    .respond(200, result);

                //when
                TransformationRestService.getLineTransformations()
                    .then(function (resp) {
                        response = resp.data;
                    });
                $httpBackend.flush();

                //then
                expect(response).toEqual(result);
            }));
        });
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

    describe('datasets transformations', function() {
        var result = [{}];

        it('should fetch the dataset transformations', inject(function ($rootScope, TransformationRestService, RestURLs) {
            //given
            var response = null;
            var datasetId = '4354bf2543a514c25';
            $httpBackend
                .expectGET(RestURLs.datasetUrl + '/' + datasetId + '/actions')
                .respond(200, result);

            //when
            TransformationRestService.getDatasetTransformations(datasetId)
                .then(function (resp) {
                    response = resp.data;
                });
            $httpBackend.flush();

            //then
            expect(response).toEqual(result);
        }));
    });
});