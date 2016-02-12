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
 * @name data-prep.services.export.service:ExportService
 * @description Export service. This service provide the entry point to the backend export REST api.
 * @requires data-prep.services.export.service:ExportRestService
 */
export default function ExportService($window, ExportRestService) {
    'ngInject';

    var EXPORT_PARAMS_KEY = 'org.talend.dataprep.export.params';
    var self = this;

    this.exportTypes = [];

    /**
     * @ngdoc property
     * @name currentExportType
     * @propertyOf data-prep.services.export.service:ExportService
     * @description Current export type
     * @type {Object}
     */
    this.currentExportType = null;

    /**
     * @ngdoc property
     * @name currentExportParameters
     * @propertyOf data-prep.services.export.service:ExportService
     * @description Current export parameters model, bound to form inputs
     * @type {Object}
     */
    this.currentExportParameters = null;

    /**
     * @ngdoc method
     * @name reset
     * @methodOf data-prep.export.controller:ExportCtrl
     * @description Reset current export parameters with the saved one from localStorage
     */
    this.reset = function reset() {
        self.currentExportType = self.getType(self.getParameters().id);
        self.currentExportParameters = null;
    };


    /**
     * @ngdoc method
     * @name getParameters
     * @methodOf data-prep.services.export.service:ExportService
     * @description Get the saved export type from localStorage
     */
    this.getParameters = function getParameters() {
        var params = $window.localStorage.getItem(EXPORT_PARAMS_KEY);
        return params ? JSON.parse(params) : null;
    };

    /**
     * @ngdoc method
     * @name setParameters
     * @methodOf data-prep.services.export.service:ExportService
     * @description Save export type in localStorage
     */
    this.setParameters = function setParameters(params) {
        $window.localStorage.setItem(EXPORT_PARAMS_KEY, JSON.stringify(params));
    };

    /**
     * @ngdoc method
     * @name getType
     * @methodOf data-prep.services.export.service:ExportService
     * @description Get the type by id
     */
    this.getType = function getType(id) {
        return _.find(self.exportTypes, function (type) {
            return type.id === id;
        });
    };

    /**
     * @ngdoc method
     * @name saveDefaultExport
     * @methodOf data-prep.services.export.service:ExportService
     * @description [PRIVATE] Save the default export in localStorage
     */
    var saveDefaultExport = function saveDefaultExport() {
        var exportType = _.find(self.exportTypes, function (type) {
            return type.defaultExport === 'true';
        });
        self.setParameters(exportType);
    };

    /**
     * @ngdoc method
     * @name refreshTypes
     * @methodOf data-prep.services.export.service:ExportService
     * @description Refresh the export types list and save default if no parameters has been saved yet
     */
    this.refreshTypes = function refreshTypes() {
        return ExportRestService.exportTypes()
            .then(function (response) {
                self.exportTypes = response.data;

                // save default export if no parameter has been saved yet
                if (!self.getParameters() && self.exportTypes.length) {
                    saveDefaultExport();
                }

                return self.exportTypes;
            });
    };
}