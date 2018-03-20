/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation Rest Service', () => {
    let $httpBackend;

    beforeEach(angular.mock.module('data-prep.services.transformation'));

    beforeEach(inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
    }));

    describe('transformations', () => {
        const result = [
            {
                category: 'case',
                items: [],
                name: 'uppercase',
                value: '',
                type: 'OPERATION',
                parameters: [{ name: 'column_name', type: 'string', default: '' }],
            },
            {
                category: 'case',
                items: [],
                name: 'lowercase',
                value: '',
                type: 'OPERATION',
                parameters: [{ name: 'column_name', type: 'string', default: '' }],
            },
        ];
        
        it('should get transformations on column scope', inject((RestURLs, TransformationRestService) => {
            // given
            const column = {
                id: 'firstname',
                quality: { empty: 0, invalid: 0, valid: 2 },
                type: 'string',
                total: 2,
            };
            let response = null;
            $httpBackend
                .expectPOST(RestURLs.transformUrl + '/actions/column', column)
                .respond(200, result);

            // when
            TransformationRestService.getTransformations('column', column)
                .then((resp) => { response = resp; });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));
        
        it('should get transformations on line scope', inject((RestURLs, TransformationRestService) => {
            // given
            let response = null;
            $httpBackend
                .expectGET(RestURLs.transformUrl + '/actions/line')
                .respond(200, result);

            // when
            TransformationRestService.getTransformations('line')
                .then((resp) => { response = resp; });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));
        
        it('should get transformations on dataset scope', inject((RestURLs, TransformationRestService) => {
            // given
            let response = null;
            $httpBackend
                .expectGET(RestURLs.transformUrl + '/actions/dataset')
                .respond(200, result);

            // when
            TransformationRestService.getTransformations('dataset')
                .then((resp) => { response = resp; });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));
    });
    
    describe('suggestions', () => {
        const result = [
            {
                category: 'case',
                items: [],
                name: 'uppercase',
                value: '',
                type: 'OPERATION',
                parameters: [{ name: 'column_name', type: 'string', default: '' }],
            },
            {
                category: 'case',
                items: [],
                name: 'lowercase',
                value: '',
                type: 'OPERATION',
                parameters: [{ name: 'column_name', type: 'string', default: '' }],
            },
        ];

        it('should get suggestions on provided scope and entity', inject((RestURLs, TransformationRestService) => {
            // given
            const entity = {
                id: 'firstname',
                quality: { empty: 0, invalid: 0, valid: 2 },
                type: 'string',
                total: 2,
            };
            const scope = 'column';
            let response = null;
            $httpBackend
                .expectPOST(`${RestURLs.transformUrl}/suggest/${scope}`, entity)
                .respond(200, result);

            // when
            TransformationRestService.getSuggestions(scope, entity)
                .then((resp) => { response = resp; });
            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));
    });

    describe('dynamic parameters', () => {
        it('should call GET transformation dynamic params rest service with preparationId', inject(($rootScope, TransformationRestService, RestURLs) => {
            // given
            let response = null;
            const action = 'textclustering';
            const columnId = 'firstname';
            const preparationId = '7b89ef45f6';

            const result = { type: 'cluster', details: { titles: ['', ''] } };
            $httpBackend
                .expectGET(RestURLs.transformUrl + '/suggest/textclustering/params?preparationId=7b89ef45f6&columnId=firstname')
                .respond(200, result);

            // when
            TransformationRestService.getDynamicParameters(action, columnId, null, preparationId)
                .then((resp) => {
                    response = resp;
                });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));

        it('should call GET transformation dynamic params rest service with preparationId and stepId', inject(($rootScope, TransformationRestService, RestURLs) => {
            // given
            let response = null;
            const action = 'textclustering';
            const columnId = 'firstname';
            const preparationId = '7b89ef45f6';
            const stepId = '126578a98bf';

            const result = { type: 'cluster', details: { titles: ['', ''] } };
            $httpBackend
                .expectGET(RestURLs.transformUrl + '/suggest/textclustering/params?preparationId=7b89ef45f6&stepId=126578a98bf&columnId=firstname')
                .respond(200, result);

            // when
            TransformationRestService.getDynamicParameters(action, columnId, null, preparationId, stepId)
                .then((resp) => {
                    response = resp;
                });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));

        it('should call GET transformation dynamic params rest service with datasetId', inject(($rootScope, TransformationRestService, RestURLs) => {
            // given
            let response = null;
            const action = 'textclustering';
            const columnId = 'firstname';
            const datasetId = '7b89ef45f6';

            const result = { type: 'cluster', details: { titles: ['', ''] } };
            $httpBackend
                .expectGET(RestURLs.transformUrl + '/suggest/textclustering/params?datasetId=7b89ef45f6&columnId=firstname')
                .respond(200, result);

            // when
            TransformationRestService.getDynamicParameters(action, columnId, datasetId, null)
                .then((resp) => {
                    response = resp;
                });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));
    });

    describe('datasets transformations', () => {
        const result = [{}];

        it('should fetch the dataset transformations', inject(($rootScope, TransformationRestService, RestURLs) => {
            // given
            let response = null;
            const datasetId = '4354bf2543a514c25';
            $httpBackend
                .expectGET(RestURLs.datasetUrl + '/' + datasetId + '/actions')
                .respond(200, result);

            // when
            TransformationRestService.getDatasetTransformations(datasetId)
                .then((resp) => {
                    response = resp.data;
                });

            $httpBackend.flush();

            // then
            expect(response).toEqual(result);
        }));
    });
});
