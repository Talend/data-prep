/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:RestURLs
 * @description The REST api services url
 */
export default function RestURLs() {
	/**
	 * @ngdoc method
	 * @name register
	 * @propertyOf data-prep.services.utils.service:RestURLs
	 * @description Init the api urls with a provided URLs configuration
	 * @param {Object} config Contains the host and port to define API urls
	 * @param {Object} uris All URIs to be consumed
	 */
	this.register = function register(config, uris) {
		const { serverUrl } = config;
		this.aggregationUrl = serverUrl + uris.apiAggregate;
		this.datasetUrl = serverUrl + uris.apiDatasets;
		this.uploadDatasetUrl = serverUrl + uris.apiUploadDatasets;
		this.exportUrl = serverUrl + uris.apiExport;
		this.folderUrl = serverUrl + uris.apiFolders;
		this.mailUrl = serverUrl + uris.apiMail;
		this.preparationUrl = serverUrl + uris.apiPreparations;
		this.previewUrl = serverUrl + uris.apiPreparationsPreview;
		this.searchUrl = serverUrl + uris.apiSearch;
		this.settingsUrl = serverUrl + uris.apiSettings;
		this.tcompUrl = serverUrl + uris.apiTcomp;
		this.transformUrl = serverUrl + uris.apiTransform;
		this.typesUrl = serverUrl + uris.apiTypes;
		this.upgradeVersion = serverUrl + uris.apiUpgradeCheck;
		this.versionUrl = serverUrl + uris.apiVersion;
	};
}
