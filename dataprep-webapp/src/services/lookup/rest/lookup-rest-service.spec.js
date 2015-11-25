describe('Lookup Rest Service', function () {
	'use strict';

	var $httpBackend;
	var datasetContent = {
			'metadata': {
				'id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
			},
			'records':[],
			'columns':[]
	};
	var datasetActions = [
		{
			'category': 'data_blending',
			'name': 'lookup',
			'parameters': [
				{
					'name': 'column_id',
					'type': 'string',
					'default': ''
				},
				{
					'name': 'filter',
					'type': 'filter',
					'default': ''
				},
				{
					'name': 'lookup_ds_name',
					'type': 'string',
					'default': 'lookup_2'
				},
				{
					'name': 'lookup_ds_id',
					'type': 'string',
					'default': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7'
				},
				{
					'name': 'lookup_ds_url',
					'type': 'string',
					'default': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true'
				},
				{
					'name': 'lookup_join_on',
					'type': 'string',
					'default': ''
				},
				{
					'name': 'lookup_join_on_name',
					'type': 'string',
					'default': ''
				},
				{
					'name': 'lookup_selected_cols',
					'type': 'list',
					'default': ''
				}
			]
		}
	];

	beforeEach(module('data-prep.services.lookup'));

	beforeEach(inject(function ($rootScope, $injector, RestURLs) {
		RestURLs.setServerUrl('');
		$httpBackend = $injector.get('$httpBackend');
	}));

	var datasetLookupId = '9e739b88-5ec9-4b58-84b5-2127a7e2eac7';

	it('should call lookup actions list rest service ', inject(function ($rootScope, RestURLs, LookupRestService) {
		//given
		var result = null;
		$httpBackend
			.expectGET(RestURLs.datasetActionsUrl + '/9e739b88-5ec9-4b58-84b5-2127a7e2eac7/actions')
			.respond(200, datasetActions);

		//when
		LookupRestService.getLookupActions(datasetLookupId).then(function (response) {
			result = response.data;
		});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(result).toEqual(datasetActions);
	}));

	it('should call lookup content rest service ', inject(function ($rootScope, RestURLs, LookupRestService) {
		//given
		var result = null;
		$httpBackend
			.expectGET('http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true')
			.respond(200, datasetContent);

		//when
		LookupRestService.getLookupContent('http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true').then(function (response) {
			result = response.data;
		});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(result).toEqual(datasetContent);
	}));
});