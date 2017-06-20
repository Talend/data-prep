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

describe('Config service', () => {

	beforeEach(angular.mock.module('data-prep.services.utils'));

	it('should set config', inject(($rootScope, ConfigService, DocumentationService) => {
		// given
		spyOn(DocumentationService, 'setUrl');
		spyOn(DocumentationService, 'setVersion');
		spyOn(DocumentationService, 'setLanguage');

		// when
		ConfigService.setConfig({ serverUrl: '' }, settings);
		// $rootScope.$digest();

		// then
		expect(DocumentationService.setUrl).toHaveBeenCalledWith(url);
		expect(DocumentationService.setVersion).toHaveBeenCalledWith(version);
		expect(DocumentationService.setLanguage).toHaveBeenCalledWith(language);
	}));
});
