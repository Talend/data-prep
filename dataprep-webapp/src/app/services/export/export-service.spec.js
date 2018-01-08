/*
 * ============================================================================
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

describe('Export service', () => {
    'use strict';

    let stateMock;

    const exportTypes = [
        {
            mimeType: 'text/csv',
            extension: '.csv',
            id: 'CSV',
            needParameters: true,
            defaultExport: false,
            enabled: true,
            disableReason: '',
            title: 'Export to CSV',
            parameters: [
                {
                    'name': 'csvSeparator',
                    'type': 'select',
                    'implicit': false,
                    'canBeBlank': false,
                    'placeHolder': '',
                    'configuration': {
                        'values': [{ 'value': ';', 'label': 'Semi colon' }, {
                            'value': '\t',
                            'label': 'Tabulation'
                        }, { 'value': ' ', 'label': 'Space' }, { 'value': ',', 'label': 'Comma' }],
                        'multiple': false
                    },
                    description: 'Select character to use as a delimiter',
                    label: 'Select character to use as a delimiter',
                    default: ';',
                },
                {
                    name: 'fileName',
                    type: 'string',
                    implicit: false,
                    canBeBlank: false,
                    placeHolder: '',
                    description: 'Name of the generated export file',
                    label: 'Name of the generated export file',
                    default: '',
                },
            ],
        },
        {
            mimeType: 'application/tde',
            extension: '.tde',
            id: 'TABLEAU',
            needParameters: false,
            defaultExport: false,
            parameters: [
                {
                    name: 'fileName',
                    type: 'string',
                    implicit: false,
                    canBeBlank: false,
                    placeHolder: '',
                    description: 'Name of the generated export file',
                    label: 'Name of the generated export file',
                    default: '',
                },
            ],
        },
        {
            mimeType: 'application/vnd.ms-excel',
            extension: '.xlsx',
            id: 'XLSX',
            needParameters: true,
            defaultExport: true,
            enabled: true,
            disableReason: '',
            title: 'Export to XLSX',
            parameters: [
                {
                    name: 'fileName',
                    type: 'string',
                    implicit: false,
                    canBeBlank: false,
                    placeHolder: '',
                    description: 'Name of the generated export file',
                    label: 'Name of the generated export file',
                    default: '',
                },
            ],
        },
    ];

    beforeEach(angular.mock.module('data-prep.services.export', ($provide) => {
        stateMock = {
            export: { exportTypes: exportTypes }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, ExportRestService, ParametersService, StateService, StorageService) => {
        spyOn(ExportRestService, 'exportTypes').and.returnValue($q.when(exportTypes));
        spyOn(ParametersService, 'resetParamValue').and.returnValue();
        spyOn(StateService, 'setDefaultExportType').and.returnValue();
        spyOn(StateService, 'setExportTypes').and.returnValue();
        spyOn(StorageService, 'saveExportParams').and.returnValue();
    }));

    it('should return type with provided id', inject((ExportService) => {
        // given
        const xlsType = exportTypes[2];

        // when
        const type = ExportService.getType('XLSX');

        // then
        expect(type).toBe(xlsType);
    }));

    it('should set params in app state and storage', inject((ExportService, StateService, StorageService) => {
        // given
        const exportParams = { exportType: 'whatever' };

        // when
        ExportService.setExportParams(exportParams);

        // then
        expect(StateService.setDefaultExportType).toHaveBeenCalledWith(exportParams);
        expect(StorageService.saveExportParams).toHaveBeenCalledWith(exportParams);
    }));

    it('should get export types and set them in app state',
        inject(($rootScope, ExportService, StateService, ExportRestService) => {
            // when
            ExportService.refreshTypes('datasets', 'myDatasetId');
            $rootScope.$digest();

            // then
            expect(ExportRestService.exportTypes).toHaveBeenCalledWith('datasets', 'myDatasetId');
            expect(StateService.setExportTypes).toHaveBeenCalledWith(exportTypes);
        }));

    it('should set default export type in app state and storage',
        inject(($rootScope, ExportService, StorageService, StateService) => {
            // given
            spyOn(StorageService, 'getExportParams').and.returnValue();

            // when
            ExportService.refreshTypes('datasets', 'myDatasetId');
            $rootScope.$digest();

            // then
            const defaultParams = { exportType: 'XLSX' };
            expect(StateService.setDefaultExportType).toHaveBeenCalledWith(defaultParams);
            expect(StorageService.saveExportParams).toHaveBeenCalledWith(defaultParams);
        }));

    it('should set storage saved default export type in app state',
        inject(($rootScope, ExportService, StorageService, StateService) => {
            // given
            const savedExportParams = { exportType: 'whatever' };
            spyOn(StorageService, 'getExportParams').and.returnValue(savedExportParams);

            // when
            ExportService.refreshTypes('datasets', 'myDatasetId');
            $rootScope.$digest();

            // then
            expect(StateService.setDefaultExportType).toHaveBeenCalledWith(savedExportParams);
            expect(StorageService.saveExportParams).not.toHaveBeenCalled();
        }));
});
