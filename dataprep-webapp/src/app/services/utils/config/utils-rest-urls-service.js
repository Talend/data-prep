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
	this.register = function register(uris) {
		this.aggregationUrl = uris.apiAggregate;
		this.datasetUrl = uris.apiDatasets;
		this.uploadDatasetUrl = uris.apiUploadDatasets;
		this.exportUrl = uris.apiExport;
		this.folderUrl = uris.apiFolders;
		this.mailUrl = uris.apiMail;
		this.preparationUrl = uris.apiPreparations;
		this.previewUrl = uris.apiPreparationsPreview;
		this.searchUrl = uris.apiSearch;
		this.settingsUrl = uris.apiSettings;
		this.tcompUrl = uris.apiTcomp;
		this.transformUrl = uris.apiTransform;
		this.typesUrl = uris.apiTypes;
		this.upgradeVersion = uris.apiUpgradeCheck;
		this.versionUrl = uris.apiVersion;
	};
}
