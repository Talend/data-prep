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
 * @name data-prep.services.utils.service:HelpService
 * @description
 */
export default function HelpService() {
	/**
	 * @ngdoc method
	 * @name register
	 * @propertyOf data-prep.services.utils.service:HelpService
	 * @description Register help configuration from app settings
	 * @param {Object} helpSettings All documentation settings to be consumed
	 */
	this.register = function register(helpSettings) {
		this.languageFacet = helpSettings.languageFacet;
		this.versionFacet = helpSettings.versionFacet;
		this.searchUrl = helpSettings.searchUrl;
		this.exactUrl = helpSettings.exactUrl;
		this.fuzzyUrl = helpSettings.fuzzyUrl;
	};
}
