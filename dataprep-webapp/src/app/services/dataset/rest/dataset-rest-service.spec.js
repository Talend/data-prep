/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

describe('Dataset Rest Service', () => {
	'use strict';

	let $httpBackend;

	beforeEach(angular.mock.module('data-prep.services.dataset'));

	beforeEach(inject(($rootScope, $injector, RestURLs) => {
		RestURLs.register({ serverUrl: '' }, settings.uris);
		$httpBackend = $injector.get('$httpBackend');

		spyOn($rootScope, '$emit').and.returnValue();
	}));

	describe('list', () => {
		it('should call dataset list rest service WITHOUT sort parameters', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasets = [
				{ name: 'Customers (50 lines)' },
				{ name: 'Us states' },
				{ name: 'Customers (1K lines)' },
			];
			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/summary')
				.respond(200, datasets);

			//when
			DatasetRestService.getDatasets(undefined, undefined, $q.defer()).then((response) => {
				result = response.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(datasets);
		}));

		it('should call dataset list rest service WITH sort parameters', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasets = [
				{ name: 'Customers (50 lines)' },
				{ name: 'Us states' },
				{ name: 'Customers (1K lines)' },
			];
			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/summary' + '?sort=name&order=asc')
				.respond(200, datasets);

			//when
			DatasetRestService.getDatasets('name', 'asc', $q.defer()).then((response) => {
				result = response.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(datasets);
		}));

		it('should call dataset list rest service WITH sort parameters (only sortType)', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasets = [
				{ name: 'Customers (50 lines)' },
				{ name: 'Us states' },
				{ name: 'Customers (1K lines)' },
			];
			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/summary' + '?sort=name')
				.respond(200, datasets);

			//when
			DatasetRestService.getDatasets('name', undefined, $q.defer()).then((response) => {
				result = response.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(datasets);
		}));

		it('should call dataset list rest service WITH sort parameters (only sortOrder)', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasets = [
				{ name: 'Customers (50 lines)' },
				{ name: 'Us states' },
				{ name: 'Customers (1K lines)' },
			];
			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/summary' + '?order=asc')
				.respond(200, datasets);

			//when
			DatasetRestService.getDatasets(undefined, 'asc', $q.defer()).then((response) => {
				result = response.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(datasets);
		}));

		it('should call dataset list by name and return the first dataset', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let dataset = null;
			const searchResult = { datasets: [{ name: 'Customers' }] };
			$httpBackend
				.expectGET(`${RestURLs.searchUrl}?name=toto&strict=true&filter=datasets`)
				.respond(200, searchResult);

			//when
			DatasetRestService.getDatasetByName('toto')
				.then((response) => {
					dataset = response;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(dataset).toEqual(searchResult.datasets[0]);
		}));

		it('should call dataset list by name and return undefined', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let dataset = null;
			const searchResult = {};
			$httpBackend
				.expectGET(`${RestURLs.searchUrl}?name=toto&strict=true&filter=datasets`)
				.respond(200, searchResult);

			//when
			DatasetRestService.getDatasetByName('toto')
				.then((response) => {
					dataset = response;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(dataset).toEqual(undefined);
		}));

		it('should call dataset list with a filter on the name', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasets = [
				{ name: 'Customers (50 lines)' },
				{ name: 'Customers (1K lines)' },
			];
			$httpBackend
				.expectGET(RestURLs.datasetUrl + '?name=Cust')
				.respond(200, datasets);

			//when
			DatasetRestService.getFilteredDatasets({name: 'Cust'})
				.then((response) => {
					result = response;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(datasets);
		}));

		it('should call dataset list with a filter on the name in Japanese', inject(($rootScope, $q, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasets = [
				{ name: '顧客 (50 lines)' },
				{ name: '顧客 (1K lines)' },
			];
			$httpBackend
				.expectGET(RestURLs.datasetUrl + '?name=%E9%A1%A7%E5%AE%A2')
				.respond(200, datasets);

			//when
			DatasetRestService.getFilteredDatasets({name: '顧客'})
				.then((response) => {
					result = response;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(datasets);
		}));
	});

	describe('creation', () => {
		it('should call dataset creation rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let datasetId = null;
			const dataset = { name: 'my dataset', file: { path: '/path/to/file' }, error: false, size: 666 };

			$httpBackend
				.expectPOST(RestURLs.uploadDatasetUrl + '?name=my%20dataset&size=666')
				.respond(200, 'e85afAa78556d5425bc2');

			//when
			DatasetRestService.create(dataset).then((res) => {
				datasetId = res.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(datasetId).toBe('e85afAa78556d5425bc2');
		}));

		it('should call dataset creation rest service with file', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			var datasetId = null;
			var contentType = 'text/plain';
			var importParameters = {
				type: 'http',
				name: 'greatremotedataset',
				url: 'moc.dnelat//:ptth',
				size: 666
			};
			var file = { id: '0001' };
			var headers = {
				'Content-Type': 'text/plain',
				Accept: 'application/json, text/plain, */*'
			};

			$httpBackend
				.expectPOST(RestURLs.uploadDatasetUrl + '?name=greatremotedataset&size=666', file, headers)

				.respond(200, 'e85afAa78556d5425bc2');

			//when
			DatasetRestService.create(importParameters, contentType, file).then((res) => {
				datasetId = res.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(datasetId).toBe('e85afAa78556d5425bc2');
		}));

		it('should call dataset creation rest service with import parameters for remote http', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			var datasetId = null;
			var dataset = { name: 'my dataset', file: { path: '/path/to/file' }, error: false, size: 666 };

			$httpBackend
				.expectPOST(RestURLs.uploadDatasetUrl + '?name=my%20dataset&size=666')

				.respond(200, 'e85afAa78556d5425bc2');

			//when
			DatasetRestService.create(dataset).then((res) => {
				datasetId = res.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(datasetId).toBe('e85afAa78556d5425bc2');
		}));

		it('should call dataset creation rest service with parameters', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			var datasetId = null;
			var contentType = 'application/vnd.remote-ds.http';
			var importParameters = {
				type: 'http',
				name: 'greatremotedataset',
				url: 'moc.dnelat//:ptth',
			};
			var file = null;
			var headers = {
				'Content-Type': 'application/vnd.remote-ds.http',
				Accept: 'application/json, text/plain, */*',
			};

			$httpBackend
				.expectPOST(RestURLs.uploadDatasetUrl + '?name=greatremotedataset', importParameters, headers)

				.respond(200, 'e85afAa78556d5425bc2');

			//when
			DatasetRestService.create(importParameters, contentType, file).then((res) => {
				datasetId = res.data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(datasetId).toBe('e85afAa78556d5425bc2');
		}));
	});

	describe('update', () => {
		it('should call dataset update rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			var dataset = {
				name: 'my dataset',
				file: { path: '/path/to/file' },
				error: false,
				id: 'e85afAa78556d5425bc2',
			};

			$httpBackend
				.expectPUT(RestURLs.uploadDatasetUrl + '/e85afAa78556d5425bc2?name=my%20dataset')
				.respond(200);

			//when
			DatasetRestService.update(dataset);
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			//expect PUT not to throw any exception
		}));

		it('should call dataset update rest service with size', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			var dataset = {
				name: 'my dataset',
				file: { path: '/path/to/file' },
				error: false,
				id: 'e85afAa78556d5425bc2',
			};

			$httpBackend
				.expectPUT(RestURLs.uploadDatasetUrl + '/e85afAa78556d5425bc2?name=my%20dataset&size=1000')
				.respond(200);

			//when
			DatasetRestService.update(dataset, {size: 1000});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			//expect PUT not to throw any exception
		}));

		it('should call dataset metadata update rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const metadata = { id: 'e85afAa78556d5425bc2', name: 'my dataset' };

			$httpBackend
				.expectPUT(RestURLs.datasetUrl + '/e85afAa78556d5425bc2/metadata', metadata)
				.respond(200);

			//when
			DatasetRestService.updateMetadata(metadata);
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			//expect PUT not to throw any exception
		}));

		it('should call update column service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const datasetId = '75b1547dc4145e218';
			const columnId = '24a5416584cf63b26';
			const params = {
				domain: 'CITY',
			};

			$httpBackend
				.expectPOST(RestURLs.datasetUrl + '/' + datasetId + '/column/' + columnId, params)
				.respond(200);

			//when
			DatasetRestService.updateColumn(datasetId, columnId, params);
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			//expect POST not to throw any exception;
		}));
	});

	describe('delete', () => {
		it('should call dataset delete rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const dataset = {
				name: 'my dataset',
				file: { path: '/path/to/file' },
				error: false,
				id: 'e85afAa78556d5425bc2',
			};

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
	});

	describe('content', () => {
		it('should call dataset get metadata rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasetId = 'e85afAa78556d5425bc2';
			const data = [{ column: [] }];

			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/e85afAa78556d5425bc2/metadata')
				.respond(200, data);

			//when
			DatasetRestService.getMetadata(datasetId).then((data) => {
				result = data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(data);
		}));

		it('should call dataset get content rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasetId = 'e85afAa78556d5425bc2';
			const data = [{ column: [], records: [] }];

			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/e85afAa78556d5425bc2?metadata=false&includeTechnicalProperties=true')
				.respond(200, data);

			//when
			DatasetRestService.getContent(datasetId, false).then((data) => {
				result = data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(data);
		}));

		it('should call dataset get content rest service with TQL', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasetId = 'e85afAa78556d5425bc2';
			const data = [{ column: [], records: [] }];
			const tql = "(0001='charles')";
			$httpBackend
				.expectGET(RestURLs.datasetUrl + "/e85afAa78556d5425bc2?filter=(0001%253D'charles')&includeTechnicalProperties=true&metadata=false")
				.respond(200, data);

			//when
			DatasetRestService.getContent(datasetId, false, tql).then((data) => {
				result = data;
			});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(data);
		}));
	});

	describe('sheet preview', () => {
		it('should call dataset sheet preview service with default sheet', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasetId = 'e85afAa78556d5425bc2';
			const data = {
				columns: [{ id: 'col1' }],
				records: [{ col1: 'toto' }, { col1: 'tata' }]
			};

			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true')
				.respond(200, { data: data });

			//when
			DatasetRestService.getSheetPreview(datasetId)
				.then((response) => {
					result = response.data;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(data);
		}));

		it('should call dataset sheet preview service with provided sheet', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasetId = 'e85afAa78556d5425bc2';
			const sheetName = 'my sheet';
			const data = {
				columns: [{ id: 'col1' }],
				records: [{ col1: 'toto' }, { col1: 'tata' }]
			};

			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true&sheetName=my%20sheet')
				.respond(200, { data: data });

			//when
			DatasetRestService.getSheetPreview(datasetId, sheetName)
				.then((response) => {
					result = response.data;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(data);
		}));

		it('should show loading on call dataset sheet preview service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const datasetId = 'e85afAa78556d5425bc2';
			const sheetName = 'my sheet';
			const data = {
				columns: [{ id: 'col1' }],
				records: [{ col1: 'toto' }, { col1: 'tata' }]
			};

			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true&sheetName=my%20sheet')
				.respond(200, { data: data });

			//when
			DatasetRestService.getSheetPreview(datasetId, sheetName);
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
		}));
	});

	describe('favorite', () => {
		it('should call set favorite', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const dataset = {
				name: 'my dataset',
				file: { path: '/path/to/file' },
				error: false,
				id: 'e85afAa78556d5425bc2',
				favorite: true,
			};

			$httpBackend
				.expectPOST(RestURLs.datasetUrl + '/favorite/e85afAa78556d5425bc2?unset=true')
				.respond(200);

			//when
			DatasetRestService.toggleFavorite(dataset);
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			//expect POST not to throw any exception;
		}));
	});

	describe('clone', () => {
		it('should call clone rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const dataset = { id: 'foobar' };

			$httpBackend
				.expectPOST(RestURLs.datasetUrl + '/foobar/copy')
				.respond(200);

			//when
			DatasetRestService.clone(dataset);
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			//expect GET not to throw any exception;
		}));
	});

	describe('encodings', () => {
		it('should call encodings GET', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			const encodings = ['UTF-8', 'UTF-16'];
			let result = null;

			$httpBackend
				.expectGET(RestURLs.datasetUrl + '/encodings')
				.respond(200, encodings);

			//when
			DatasetRestService.getEncodings()
				.then((encodingsFromCall) => {
					result = encodingsFromCall;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(encodings);
		}));
	});

	describe('compatible preparations', () => {
		it('should call rest service', inject(($rootScope, DatasetRestService, RestURLs) => {
			//given
			let result = null;
			const datasetId = 'e85afAa78556d5425bc2';
			const preparations = [{ id: '36c2692ef5' }];

			$httpBackend
				.expectGET(`${RestURLs.datasetUrl}/${datasetId}/compatiblepreparations`)
				.respond(200, preparations);

			//when
			DatasetRestService.getCompatiblePreparations(datasetId)
				.then((preps) => {
					result = preps;
				});
			$httpBackend.flush();
			$rootScope.$digest();

			//then
			expect(result).toEqual(preparations);
		}));
	});
});
