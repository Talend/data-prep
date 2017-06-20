/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:DocumentationService
 * @description
 */
export default class DocumentationService {

	/**
	 * @ngdoc method
	 * @name setUrl
	 * @propertyOf data-prep.services.utils.service:DocumentationService
	 * @param {String} url The documentation URL
	 */
	setUrl(url) {
		this.url = url;
	}

	/**
	 * @ngdoc method
	 * @name setVersion
	 * @propertyOf data-prep.services.utils.service:DocumentationService
	 * @param {String} version The documentation version
	 */
	setVersion(version) {
		this.version = version;
	}

	/**
	 * @ngdoc method
	 * @name setLanguage
	 * @propertyOf data-prep.services.utils.service:DocumentationService
	 * @param {String} language The documentation language
	 */
	setLanguage(language) {
		this.language = language;
	}
}
