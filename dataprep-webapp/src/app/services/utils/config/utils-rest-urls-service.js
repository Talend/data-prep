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
 * @name data-prep.services.utils.service:RestURLs
 * @description The REST api services url
 */
export default function RestURLs() {
	/**
	 * @ngdoc method
	 * @name setConfig
	 * @propertyOf data-prep.services.utils.service:RestURLs
	 * @description Init the api urls with a provided URLs configuration
	 * @param {Object} config The URLs configration
	 */
	this.setConfig = function setConfig({ serverUrl }) {
		this.aggregationUrl = serverUrl + '/api/aggregate';
		this.datasetUrl = serverUrl + '/api/datasets';
		this.exportUrl = serverUrl + '/api/export';
		this.folderUrl = serverUrl + '/api/folders';
		this.mailUrl = serverUrl + '/api/mail';
		this.preparationUrl = serverUrl + '/api/preparations';
		this.previewUrl = serverUrl + '/api/preparations/preview';
		this.searchUrl = serverUrl + '/api/search';
		this.settingsUrl = serverUrl + '/api/settings';
		this.tcompUrl = serverUrl + '/api/tcomp';
		this.transformUrl = serverUrl + '/api/transform';
		this.typesUrl = serverUrl + '/api/types';
		this.upgradeVersion = serverUrl + '/api/upgrade/check';
		this.versionUrl = serverUrl + '/api/version';
	};
}
