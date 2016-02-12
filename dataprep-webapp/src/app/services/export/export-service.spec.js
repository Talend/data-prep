/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Export service', function() {
    'use strict';

    var EXPORT_PARAMS_KEY = 'org.talend.dataprep.export.params';

    var exportTypes = [
        {
            'mimeType': 'text/csv',
            'extension': '.csv',
            'id': 'CSV',
            'needParameters': 'true',
            'defaultExport': 'false',
            'parameters': [{
                'name': 'csvSeparator',
                'labelKey': 'CHOOSE_SEPARATOR',
                'type': 'radio',
                'defaultValue': {'value': ';', 'labelKey': 'SEPARATOR_SEMI_COLON'},
                'values': [
                    {'value': '&#09;', 'labelKey': 'SEPARATOR_TAB'},
                    {'value': ' ', 'labelKey': 'SEPARATOR_SPACE'},
                    {'value': ',', 'labelKey': 'SEPARATOR_COMMA'}
                ]
            }]
        },
        {
            'mimeType': 'application/tde',
            'extension': '.tde',
            'id': 'TABLEAU',
            'needParameters': 'false',
            'defaultExport': 'false'
        },
        {
            'mimeType': 'application/vnd.ms-excel',
            'extension': '.xlsx',
            'id': 'XLSX',
            'needParameters': 'false',
            'defaultExport': 'true'
        }
    ];

    beforeEach(angular.mock.module('data-prep.services.export'));

    beforeEach(inject(function($q, ExportRestService) {
        spyOn(ExportRestService, 'exportTypes').and.returnValue($q.when({data: exportTypes}));
    }));

    afterEach(inject(function($window) {
        $window.localStorage.removeItem(EXPORT_PARAMS_KEY);
    }));

    it('should get parameters from localStorage', inject(function($window, ExportService) {
        //given
        var expectedParameters = {exportType: 'XLSX'};
        $window.localStorage.setItem(EXPORT_PARAMS_KEY, JSON.stringify(expectedParameters));

        //when
        var parameters = ExportService.getParameters();

        //then
        expect(parameters).toEqual(expectedParameters);
    }));

    it('should return null when no parameters are in localStorage', inject(function($window, ExportService) {
        //given
        $window.localStorage.removeItem(EXPORT_PARAMS_KEY);

        //when
        var parameters = ExportService.getParameters();

        //then
        expect(parameters).toBe(null);
    }));

    it('should save parameters in localStorage', inject(function($window, ExportService) {
        //given
        $window.localStorage.removeItem(EXPORT_PARAMS_KEY);
        var parameters = {exportType: 'XLSX'};
        var parametersAsString = JSON.stringify(parameters);

        //when
        ExportService.setParameters(parameters);

        //then
        expect($window.localStorage.getItem(EXPORT_PARAMS_KEY)).toBe(parametersAsString);
    }));

    it('should return type with provided id', inject(function(ExportService) {
        //given
        ExportService.exportTypes = exportTypes;
        var xlsType = exportTypes[2];

        //when
        var type = ExportService.getType('XLSX');

        //then
        expect(type).toBe(xlsType);
    }));

    it('should refresh export types list from REST call', inject(function($rootScope, ExportService) {
        //given
        ExportService.exportTypes = [];

        //when
        ExportService.refreshTypes();
        $rootScope.$digest();

        //then
        expect(ExportService.exportTypes).toBe(exportTypes);
    }));

    it('should save default type parameters in localStorage when there are no saved params yet', inject(function($rootScope, $window, ExportService) {
        //given
        $window.localStorage.removeItem(EXPORT_PARAMS_KEY);
        var defaultParameters = {'mimeType': 'application/vnd.ms-excel','extension':'.xlsx','id':'XLSX','needParameters':'false','defaultExport':'true'};
        var defaultParametersAsString = JSON.stringify(defaultParameters);

        //when
        ExportService.refreshTypes();
        $rootScope.$digest();

        //then
        expect($window.localStorage.getItem(EXPORT_PARAMS_KEY)).toBe(defaultParametersAsString);
    }));

    it('should NOT save default type parameters in localStorage when there are already saved params', inject(function($rootScope, $window, ExportService) {
        //given
        var parameters = {exportType: 'custom'};
        var parametersAsString = JSON.stringify(parameters);
        $window.localStorage.setItem(EXPORT_PARAMS_KEY, parametersAsString);

        //when
        ExportService.refreshTypes();
        $rootScope.$digest();

        //then
        expect($window.localStorage.getItem(EXPORT_PARAMS_KEY)).toBe(parametersAsString);
    }));

    it('should reset parameters', inject(function($window, ExportService) {
        //given
        var expectedParameters = {id: 'XLSX'};
        $window.localStorage.setItem(EXPORT_PARAMS_KEY, JSON.stringify(expectedParameters));
        ExportService.exportTypes = exportTypes;

        //when
        ExportService.reset();

        //then
        expect(ExportService.currentExportType).toEqual({
            'mimeType': 'application/vnd.ms-excel',
            'extension': '.xlsx',
            'id': 'XLSX',
            'needParameters': 'false',
            'defaultExport': 'true'
        });
        expect(ExportService.currentExportParameters).toBeFalsy();
    }));
});