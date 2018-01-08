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

describe('Export directive', () => {
    'use strict';

    let scope;
    let element;
    let ctrl;
    let stateMock;

    const exportTypes = [
        {
            "mimeType": "text/csv",
            "extension": ".csv",
            "id": "CSV",
            "name": "CSV",
            "needParameters": true,
            "defaultExport": false,
            "enabled": true,
            "disableReason": "",
            "title": "Export to CSV",
            "parameters": [
                {
                    "name": "csvSeparator",
                    "type": "select",
                    "implicit": false,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "configuration": {
                        "values": [
                            {
                                "value": ";",
                                "label": "Semicolon"
                            },
                            {
                                "value": "\t",
                                "label": "Tabulation"
                            },
                            {
                                "value": " ",
                                "label": "Space"
                            },
                            {
                                "value": ",",
                                "label": "Comma"
                            }
                        ],
                        "multiple": false
                    },
                    "radio": true,
                    "description": "Select character to use as a delimiter",
                    "label": "Delimiter",
                    "default": ";"
                },
                {
                    "name": "fileName",
                    "type": "string",
                    "implicit": false,
                    "canBeBlank": false,
                    "placeHolder": "",
                    "description": "Name of the generated export file",
                    "label": "Filename",
                    "default": ""
                }
            ]
        },
        {
            "mimeType": "application/vnd.ms-excel",
            "extension": ".xlsx",
            "id": "XLSX",
            "name": "XLSX",
            "needParameters": true,
            "defaultExport": true,
            "enabled": true,
            "disableReason": "",
            "title": "Export to XLSX",
            "parameters": [
                {
                    "name": "fileName",
                    "type": "string",
                    "implicit": false,
                    "canBeBlank": false,
                    "placeHolder": "",
                    "description": "Name of the generated export file",
                    "label": "Filename",
                    "default": ""
                }
            ]
        }
    ];
    const xlsxType = exportTypes[1];

    beforeEach(angular.mock.module('data-prep.export', ($provide) => {
        stateMock = {
            playground: {
                recipe: {
                    current: {
                        steps: []
                    }
                }
            },
            export: {
                exportTypes: exportTypes,
                defaultExportType: {
                    exportType: 'XLSX'
                }
            }
        };
        $provide.constant('state', stateMock);
    }));


    beforeEach(inject(($rootScope, $compile, $q, ExportService, StateService) => {
        spyOn(ExportService, 'getType').and.returnValue(xlsxType);
        spyOn(StateService, 'setDefaultExportType').and.returnValue();

        scope = $rootScope.$new();
        element = angular.element('<export></export>');
        $compile(element)(scope);
        scope.$digest();

        ctrl = element.controller('export');
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('form', () => {
        it('should inject preparation id input', () => {
            //given
            const input = element.find('#exportForm').eq(0)[0].preparationId;
            expect(input.value).toBeFalsy();

            //when
            stateMock.playground.preparation = {id: '48da64513c43a548e678bc99'};
            scope.$digest();

            //then
            expect(input.value).toBe('48da64513c43a548e678bc99');
        });

        it('should inject step id input', () => {
            //given
            const input = element.find('#exportForm').eq(0)[0].stepId;
            expect(input.value).toBeFalsy();

            //when
            stateMock.playground.recipe.current.steps.push({
                transformation: {
                    stepId: '48da64513c43a548e678bc99',
                },
            });
            scope.$digest();

            //then
            expect(input.value).toBe('48da64513c43a548e678bc99');
        });

        it('should inject dataset id input', inject((state) => {
            //given
            const input = element.find('#exportForm').eq(0)[0].datasetId;
            expect(input.value).toBeFalsy();

            //when
            state.playground.dataset = {id: '48da64513c43a548e678bc99'};
            scope.$digest();

            //then
            expect(input.value).toBe('48da64513c43a548e678bc99');
        }));

        it('should inject export type input', () => {
            //given
            const input = element.find('#exportForm').eq(0)[0].exportType;

            //then
            expect(input.value).toBe('XLSX');
        });

        it('should set form in controller', inject(() => {
            //then
            expect(ctrl.form).toBeDefined();
        }));
    });

    describe('launch export', () => {
        it('should launch a csv export', () => {
            //given
            stateMock.playground.preparation = {
                id: '48da64513c43a548e678bc99',
                name: 'cars_prep'
            };

            //when
            element.find('.dropdown-menu').find('a').eq(0).click();
            scope.$digest();

            //then
            expect(stateMock.export.exportTypes[0].parameters[1].value).toBe('cars_prep');
            expect(ctrl.selectedType).toBe(exportTypes[0]);
            expect(ctrl.showModal).toBe(true);
        });
    });

    describe('disabled export', () => {
        it('should have disabled style', () => {
            //given
            stateMock.export.exportTypes[1].enabled = false;

            //when
            scope.$digest();

            //then
            expect(element.find('.dropdown-menu').find('a').eq(1).hasClass('disabled')).toBe(true);
        });

        it('should have disabled message', () => {
            //given
            stateMock.export.exportTypes[1].enabled = false;

            //when
            scope.$digest();

            //then
            expect(element.find('.dropdown-menu').find('a').eq(1).text().trim()).toBe('XLSX -');//disabledReason is empty
        });
    });
});
