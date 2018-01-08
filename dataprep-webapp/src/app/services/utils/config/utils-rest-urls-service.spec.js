/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

describe('REST urls service', () => {
	beforeEach(angular.mock.module('data-prep.services.utils'));

	it('should init api urls with empty server url (same url by default)', inject((RestURLs) => {
		// when
		RestURLs.register({ serverUrl: '' }, settings.uris);

		// then
		expect(RestURLs.datasetUrl).toBe('/api/datasets');
		expect(RestURLs.uploadDatasetUrl).toBe(RestURLs.datasetUrl);
		expect(RestURLs.transformUrl).toBe('/api/transform');
		expect(RestURLs.preparationUrl).toBe('/api/preparations');
		expect(RestURLs.previewUrl).toBe('/api/preparations/preview');
		expect(RestURLs.exportUrl).toBe('/api/export');
		expect(RestURLs.aggregationUrl).toBe('/api/aggregate');
		expect(RestURLs.typesUrl).toBe('/api/types');
		expect(RestURLs.folderUrl).toBe('/api/folders');
		expect(RestURLs.mailUrl).toBe('/api/mail');
		expect(RestURLs.searchUrl).toBe('/api/search');
		expect(RestURLs.upgradeVersion).toBe('/api/upgrade/check');
		expect(RestURLs.tcompUrl).toBe('/api/tcomp');
		expect(RestURLs.versionUrl).toBe('/api/version');
	}));

	it('should change api url with provided server url', inject((RestURLs) => {
		// when
		RestURLs.register({ serverUrl: 'http://10.10.10.10:8888' }, settings.uris);

		// then
		expect(RestURLs.datasetUrl).toBe('http://10.10.10.10:8888/api/datasets');
		expect(RestURLs.uploadDatasetUrl).toBe(RestURLs.datasetUrl);
		expect(RestURLs.transformUrl).toBe('http://10.10.10.10:8888/api/transform');
		expect(RestURLs.preparationUrl).toBe('http://10.10.10.10:8888/api/preparations');
		expect(RestURLs.previewUrl).toBe('http://10.10.10.10:8888/api/preparations/preview');
		expect(RestURLs.exportUrl).toBe('http://10.10.10.10:8888/api/export');
		expect(RestURLs.aggregationUrl).toBe('http://10.10.10.10:8888/api/aggregate');
		expect(RestURLs.typesUrl).toBe('http://10.10.10.10:8888/api/types');
		expect(RestURLs.folderUrl).toBe('http://10.10.10.10:8888/api/folders');
		expect(RestURLs.mailUrl).toBe('http://10.10.10.10:8888/api/mail');
		expect(RestURLs.searchUrl).toBe('http://10.10.10.10:8888/api/search');
		expect(RestURLs.upgradeVersion).toBe('http://10.10.10.10:8888/api/upgrade/check');
		expect(RestURLs.tcompUrl).toBe('http://10.10.10.10:8888/api/tcomp');
		expect(RestURLs.versionUrl).toBe('http://10.10.10.10:8888/api/version');
	}));
});
