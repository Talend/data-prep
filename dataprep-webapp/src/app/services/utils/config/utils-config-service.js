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
 * @name data-prep.services.utils.service:ConfigService
 * @description The config service
 */
export default class ConfigService {

	constructor(DocumentationService, RestURLs) {
		'ngInject';

		this.DocumentationService = DocumentationService;
		this.RestURLs = RestURLs;
	}

	/**
	 * @ngdoc method
	 * @name setConfig
	 * @propertyOf data-prep.services.utils.service:ConfigService
	 * @description Init the api urls with a provided URLs configuration
	 * @param {Object} config The URLs configration
	 * @param {Object} settings All app settings
	 */
	setConfig(config, settings) {
		const { documentation, uris } = settings;

		if (documentation) {
			this.DocumentationService.setUrl(documentation.url);
			this.DocumentationService.setVersion(documentation.version);
			this.DocumentationService.setLanguage(documentation.language);
		}

		const { serverUrl } = config;

		this.RestURLs.register(serverUrl, uris);
	}
}
