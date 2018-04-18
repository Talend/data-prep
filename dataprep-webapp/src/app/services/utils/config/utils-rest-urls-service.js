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
		this.context = uris.context || '';
		this.aggregationUrl = `${this.context}${uris.apiAggregate}`;
		this.datasetUrl = `${this.context}${uris.apiDatasets}`;
		this.uploadDatasetUrl = `${this.context}${uris.apiUploadDatasets}`;
		this.exportUrl = `${this.context}${uris.apiExport}`;
		this.folderUrl = `${this.context}${uris.apiFolders}`;
		this.mailUrl = `${this.context}${uris.apiMail}`;
		this.preparationUrl = `${this.context}${uris.apiPreparations}`;
		this.previewUrl = `${this.context}${uris.apiPreparationsPreview}`;
		this.searchUrl = `${this.context}${uris.apiSearch}`;
		this.settingsUrl = `${this.context}${uris.apiSettings}`;
		this.tcompUrl = `${this.context}${uris.apiTcomp}`;
		this.transformUrl = `${this.context}${uris.apiTransform}`;
		this.typesUrl = `${this.context}${uris.apiTypes}`;
		this.upgradeVersion = `${this.context}${uris.apiUpgradeCheck}`;
		this.versionUrl = `${this.context}${uris.apiVersion}`;
	};
}
