/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

import { PLACEHOLDER_DELIMITER } from './utils-help-service';

const { help } = settings;

describe('Documentation search service', () => {

	beforeEach(angular.mock.module('data-prep.services.utils'));

	beforeEach(inject((HelpService) => {
		HelpService.register(help);
	}));

	it('should register', inject((HelpService) => {
		//then
		expect(HelpService.versionFacet).toBe(help.versionFacet);
		expect(HelpService.languageFacet).toBe(help.languageFacet);
		expect(HelpService.searchUrl).toBe(help.searchUrl);
		expect(HelpService.fuzzyUrl).toBe(help.fuzzyUrl);
		expect(HelpService.exactUrl).toBe(help.exactUrl);
	}));

	it('should identify placeholders', inject((HelpService) => {
		// given
		const noMatchingText = 'lorem ipsum dolor asit';
		const matchingText = `lorem ipsum ${PLACEHOLDER_DELIMITER}dolor${PLACEHOLDER_DELIMITER} asit`;

		// when
		const noMatchingTextHasPlaceholders = HelpService.hasPlaceholders(noMatchingText);
		const matchingTextHasPlaceholders = HelpService.hasPlaceholders(matchingText);

		// then
		expect(noMatchingTextHasPlaceholders).toBeFalsy();
		expect(matchingTextHasPlaceholders).toBeTruthy();
	}));

	it('should replace placeholders', inject((HelpService) => {
		// given
		function addDelimiters(text) {
			return PLACEHOLDER_DELIMITER + text + PLACEHOLDER_DELIMITER;
		}

		const unknownValue = 'UNKNOWN';
		const toBeReplaced1 = addDelimiters('versionFacet');
		const toBeReplaced2 = addDelimiters('languageFacet');
		const toBeReplaced3 = addDelimiters(unknownValue);
		const matchingText = `lorem ipsum ${toBeReplaced1} asit ${toBeReplaced2} dolor met ${toBeReplaced3}`;

		// when
		const matchingTextHasPlaceholders = HelpService.hasPlaceholders(matchingText);
		const matchingTextReplaced = HelpService.replacePlaceholders(matchingText);

		// then
		expect(matchingTextHasPlaceholders).toBeTruthy();
		expect(matchingTextReplaced).not.toContain(PLACEHOLDER_DELIMITER);
		expect(matchingTextReplaced).toContain(help.versionFacet);
		expect(matchingTextReplaced).toContain(help.languageFacet);
		expect(matchingTextReplaced).toContain(unknownValue);
	}));
});
