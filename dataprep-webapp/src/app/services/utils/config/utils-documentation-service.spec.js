/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

const { url, version, language } = settings.documentation;

describe('Documentation search service', () => {

	beforeEach(angular.mock.module('data-prep.services.utils'));

	it('should set url', inject((DocumentationService) => {
		// when
		DocumentationService.setUrl(url);

		//then
		expect(DocumentationService.url).toBe(url);
	}));


	it('should set version', inject((DocumentationService) => {
		// when
		DocumentationService.setVersion(version);

		//then
		expect(DocumentationService.version).toBe(version);
	}));


	it('should set language', inject((DocumentationService) => {
		// when
		DocumentationService.setLanguage(language);

		//then
		expect(DocumentationService.language).toBe(language);
	}));
});
