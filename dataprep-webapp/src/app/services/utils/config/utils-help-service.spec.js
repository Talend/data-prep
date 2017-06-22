/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

const { help } = settings;

describe('Documentation search service', () => {

	beforeEach(angular.mock.module('data-prep.services.utils'));

	it('should register', inject((HelpService) => {
		// when
		HelpService.register(help);

		//then
		expect(HelpService.versionFacet).toBe(help.versionFacet);
		expect(HelpService.languageFacet).toBe(help.languageFacet);
		expect(HelpService.searchUrl).toBe(help.searchUrl);
		expect(HelpService.fuzzyUrl).toBe(help.fuzzyUrl);
		expect(HelpService.exactUrl).toBe(help.exactUrl);
	}));
});
