describe('lookup service', function() {
	'use strict';

	var dsLookupUrl = 'http://172.17.0.6:8080/datasets/9e739b88-5ec9-4b58-84b5-2127a7e2eac7/content?metadata=true';
	var dsLookupcontent = {
		data:{
			'metadata': {
				'id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
				'records': 3,
				'certificationStep': 'NONE',
				'location': {
					'type': 'local'
				},
				'name': 'lookup_2',
				'author': 'anonymous',
				'created': 1447689742940,
				'encoding': 'UTF-8'
			}
		}
	};

	beforeEach(module('data-prep.services.lookup'));

	beforeEach(inject(function(LookupRestService, StateService, $q) {
		spyOn(LookupRestService, 'getLookupContent').and.returnValue($q.when(dsLookupcontent));
		spyOn(LookupRestService, 'getLookupActions').and.returnValue();
		spyOn(StateService, 'setCurrentLookupData').and.returnValue();
		spyOn(StateService, 'setLookupDataset').and.returnValue();
	}));

	it('should show toast on error without translate arg', inject(function(LookupRestService, $rootScope, LookupService, StateService) {
		//given

		//when
		LookupService.loadLookupContent(dsLookupUrl);
		$rootScope.$digest();

		//then
		expect(LookupRestService.getLookupContent).toHaveBeenCalledWith(dsLookupUrl);
		expect(StateService.setCurrentLookupData).toHaveBeenCalledWith(dsLookupcontent.data);
		expect(StateService.setLookupDataset).toHaveBeenCalledWith(dsLookupcontent.data.metadata);
	}));

	it('should show toast on error without translate arg', inject(function(LookupRestService, $rootScope, LookupService) {
		//given

		//when
		LookupService.getLookupPossibleActions('9e739b88-5ec9-4b58-84b5-2127a7e2eac7');
		$rootScope.$digest();

		//then
		expect(LookupRestService.getLookupActions).toHaveBeenCalledWith('9e739b88-5ec9-4b58-84b5-2127a7e2eac7');
	}));
});