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
    var service = {
        setServerUrl: setServerUrl
    };

    return service;

    /**
     * @ngdoc method
     * @name setServerUrl
     * @propertyOf data-prep.services.utils.service:RestURLs
     * @description Init the api urls with a provided server url
     * @param {string} serverUrl The server url
     */
    function setServerUrl(serverUrl) {
        service.datasetUrl = serverUrl + '/api/datasets';
        service.transformUrl = serverUrl + '/api/transform';
        service.preparationUrl = serverUrl + '/api/preparations';
        service.previewUrl = serverUrl + '/api/preparations/preview';
        service.exportUrl = serverUrl + '/api/export';
        service.aggregationUrl = serverUrl + '/api/aggregate';
        service.typesUrl = serverUrl + '/api/types';
        service.folderUrl = serverUrl + '/api/folders';
        service.mailUrl = serverUrl + '/api/mail';
        service.inventoryUrl = serverUrl + '/api/inventory';
        service.upgradeVersion = serverUrl + '/api/upgrade/check';
    }
}