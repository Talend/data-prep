/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const PLACEHOLDER_DELIMITER = '%%';

const delimiterMatcher = new RegExp(PLACEHOLDER_DELIMITER, 'g');

const placeholderMatcher = new RegExp(`${PLACEHOLDER_DELIMITER}\\w+${PLACEHOLDER_DELIMITER}`, 'g');

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:HelpService
 * @description Help configuration from settings
 */
export default function HelpService() {
	/**
	 * @ngdoc method
	 * @name register
	 * @propertyOf data-prep.services.utils.service:HelpService
	 * @description Register help configuration from app settings
	 * @param {Object} helpSettings All help settings to be consumed
	 */
	this.register = function register(helpSettings) {
		const { languageFacet, versionFacet, searchUrl, exactUrl, fuzzyUrl } = helpSettings;

		this.languageFacet = languageFacet;
		this.versionFacet = versionFacet;
		this.searchUrl = searchUrl;
		this.exactUrl = exactUrl;
		this.fuzzyUrl = fuzzyUrl;
	};

	/**
	 * Identify placeholders in specified content
	 * @param text content with/without placeholders
	 */
	this.hasPlaceholders = function (text) {
		return text.includes(PLACEHOLDER_DELIMITER);
	};

	/**
	 * Replace some placeholders in order to use registered values
	 * @param text content with placeholders
	 */
	this.replacePlaceholders = function (text) {
		return text.replace(placeholderMatcher, (placeholder) => {
			const property = placeholder.replace(delimiterMatcher, '');
			return this[property] || property;
		});
	};
}
