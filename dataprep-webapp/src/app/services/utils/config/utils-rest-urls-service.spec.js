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

	it('should init api urls', inject((RestURLs) => {
		// when
		RestURLs.register(settings.uris);

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

	it('should init api urls with context path', inject((RestURLs) => {
		// when
		const urisWithContext = {
			...settings.uris,
			context: '/context',
		};
		RestURLs.register(urisWithContext);

		// then
		expect(RestURLs.datasetUrl).toBe('/context/api/datasets');
		expect(RestURLs.uploadDatasetUrl).toBe(RestURLs.datasetUrl);
		expect(RestURLs.transformUrl).toBe('/context/api/transform');
		expect(RestURLs.preparationUrl).toBe('/context/api/preparations');
		expect(RestURLs.previewUrl).toBe('/context/api/preparations/preview');
		expect(RestURLs.exportUrl).toBe('/context/api/export');
		expect(RestURLs.aggregationUrl).toBe('/context/api/aggregate');
		expect(RestURLs.typesUrl).toBe('/context/api/types');
		expect(RestURLs.folderUrl).toBe('/context/api/folders');
		expect(RestURLs.mailUrl).toBe('/context/api/mail');
		expect(RestURLs.searchUrl).toBe('/context/api/search');
		expect(RestURLs.upgradeVersion).toBe('/context/api/upgrade/check');
		expect(RestURLs.tcompUrl).toBe('/context/api/tcomp');
		expect(RestURLs.versionUrl).toBe('/context/api/version');
	}));
});
