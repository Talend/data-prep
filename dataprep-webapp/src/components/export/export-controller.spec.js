describe('Export controller', function() {
    'use strict';

    var scope, createController, form;

    var exportTypes = [
        {
            'mimeType': 'text/csv',
            'extension': '.csv',
            'id': 'CSV',
            'needParameters': 'true',
            'defaultExport': 'false',
            'enabled': 'true',
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
            },{
                'name': 'fileName',
                'labelKey': 'EXPORT_FILENAME"',
                'type': 'text',
                'defaultValue': {'value': ';', 'labelKey': 'EXPORT_FILENAME_DEFAULT'}
            }]
        },
        {
            'mimeType': 'application/tde',
            'extension': '.tde',
            'id': 'TABLEAU',
            'needParameters': 'false',
            'defaultExport': 'false',
            'enabled': 'true'
        },
        {
            'mimeType': 'application/vnd.ms-excel',
            'extension': '.xlsx',
            'id': 'XLSX',
            'needParameters': 'false',
            'defaultExport': 'true',
            'enabled': 'true'
        }
    ];
    var currentParameters = {exportType: 'XLSX'};

    var stateMock;

    beforeEach(module('data-prep.export', function($provide) {
        stateMock = {
            playground: {
                preparation : {name: 'prepname'},
                exportParameters : { exportType: 'CSV', 'exportParameters.csvSeparator': ';' , 'exportParameters.fileName': 'prepname' }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller, $q, ExportService) {
        form = {submit : function() {}};
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('ExportCtrl', {
                $scope: scope
            });
            ctrl.form = form; //simulate init by directive
            return ctrl;
        };

        spyOn(ExportService, 'refreshTypes').and.callFake(function() {
            ExportService.exportTypes = exportTypes;
            return $q.when(exportTypes);
        });
        spyOn(ExportService, 'getParameters').and.returnValue(currentParameters);
        spyOn(ExportService, 'setParameters').and.returnValue();
        spyOn(form, 'submit').and.returnValue();
    }));

    describe('property binding', function() {
        it('should bind stepId getter to RecipeService', inject(function(RecipeService) {
            //given
            var ctrl = createController();
            expect(ctrl.stepId).toBeFalsy();

            //when
            RecipeService.getRecipe().push({
                transformation : {
                    stepId: '48da64513c43a548e678bc99'
                }
            });

            //then
            expect(ctrl.stepId).toBe('48da64513c43a548e678bc99');
        }));

        it('should bind datasetId getter to PlaygroundService', function() {
            //given
            var ctrl = createController();
            expect(ctrl.datasetId).toBeFalsy();

            //when
            stateMock.playground.dataset = {
                id: '48da64513c43a548e678bc99'
            };

            //then
            expect(ctrl.datasetId).toBe('48da64513c43a548e678bc99');
        });

        it('should bind exportTypes getter to ExportService', inject(function(ExportService) {
            //given
            var ctrl = createController();
            expect(ctrl.exportTypes).toBe(exportTypes);

            var newTypes = [];

            //when
            ExportService.exportTypes = newTypes;

            //then
            expect(ctrl.exportTypes).toBe(newTypes);
        }));
    });

    describe('on creation', function() {
        it('should init export types', inject(function(ExportService) {
            //when
            createController();

            //then
            expect(ExportService.refreshTypes).toHaveBeenCalled();
        }));

        it('should init current export type and parameter', function() {
            //when
            var ctrl = createController();
            scope.$digest();

            //then
            expect(ctrl.exportService.currentExportType).toBeFalsy();
            expect(ctrl.exportService.currentExportParameters).toBeFalsy();
        });
    });

    it('should reset params with saved one and reset current export type accordingly', function() {
        //given
        var ctrl = createController();
        ctrl.exportService.currentExportType = {exportType: 'XLSX'} ;
        ctrl.exportService.currentExportParameters = {filename: 'test'};

        //when
        ctrl.exportService.reset();

        //then
        expect(ctrl.exportService.currentExportType).toBeFalsy();
        expect(ctrl.exportService.currentExportParameters).toBeFalsy();
    });

    describe('on new type selection', function() {
        it('should update parameters (type)', function() {
            //given
            var ctrl = createController();
            scope.$apply();

            var csvType = exportTypes[0];
            var csvParams = { exportType: 'CSV', 'exportParameters.csvSeparator': ';' , 'exportParameters.fileName': 'prepname' }; //with separator default value

            expect(ctrl.exportService.currentExportType).not.toBe(csvType);
            expect(ctrl.exportService.currentExportParameters).not.toEqual(csvParams);

            //when
            ctrl.changeTypeAndExport(csvType);

            //then
            expect(ctrl.exportService.currentExportType).toBe(csvType);
            expect(ctrl.exportService.currentExportParameters).toEqual(csvParams);
        });

        it('should update parameters (type + fileName)', function() {
            //given
            var ctrl = createController();
            scope.$apply();

            stateMock.playground.exportParameters = null;
            stateMock.playground.preparation = null;
            stateMock.playground.dataset = {
                name: '48da64513c43a548e678bc99'
            };

            var csvType = exportTypes[0];
            var csvParams = { exportType: 'CSV', 'exportParameters.csvSeparator': ';' , 'exportParameters.fileName': '48da64513c43a548e678bc99' }; //with separator default value

            expect(ctrl.exportService.currentExportType).not.toBe(csvType);
            expect(ctrl.exportService.currentExportParameters).not.toEqual(csvParams);

            //when
            ctrl.changeTypeAndExport(csvType);

            //then
            expect(ctrl.exportService.currentExportType).toBe(csvType);
            expect(ctrl.exportService.currentExportParameters).toEqual(csvParams);
        });

        it('should show modal on type with parameters', function() {
            //given
            var ctrl = createController();
            scope.$apply();
            expect(ctrl.showModal).toBeFalsy();

            var csvType = exportTypes[0];

            //when
            ctrl.changeTypeAndExport(csvType);

            //then
            expect(ctrl.showModal).toBe(true);
            expect(form.submit).not.toHaveBeenCalled();
        });
    });

    describe('on parameters validation', function() {
        it('should save current export parameters', inject(function(ExportService) {
            //given
            var ctrl = createController();
            ctrl.exportService.currentExportType = exportTypes[0];

            //when
            ctrl.saveEditionAndExport();

            //then
            expect(ExportService.setParameters).toHaveBeenCalledWith(exportTypes[0]);
        }));

        it('should launch export after a $timeout', inject(function($timeout) {
            //given
            var ctrl = createController();
            var parameters = {exportType: 'CSV', 'exportParameters.csvSeparator': ','};
            ctrl.exportService.currentExportParameters = parameters;

            //when
            ctrl.saveEditionAndExport();
            $timeout.flush();

            //then
            expect(form.submit).toHaveBeenCalled();
        }));
    });

    describe('on export', function() {
        it('should set action in form', inject(function(RestURLs, $timeout) {
            //given
            var ctrl = createController();
            ctrl.exportService.currentExportType = exportTypes[0];

            //when
            ctrl.export();
            $timeout.flush();

            //then
            expect(form.action).toBe(RestURLs.exportUrl);
        }));

        it('should submit form', inject(function($timeout) {
            //given
            var ctrl = createController();
            ctrl.exportService.currentExportType = exportTypes[0];

            //when
            ctrl.export();
            $timeout.flush();

            //then
            expect(form.submit).toHaveBeenCalled();
        }));

        it('should cancel parameters', inject(function() {
            //given
            var ctrl = createController();
            spyOn(ctrl.exportService, 'getType').and.returnValue(exportTypes[0]);

            //when
            ctrl.cancelCurrentParameters ();

            //then
            expect(ctrl.exportService.currentExportType).toBe(exportTypes[0]);
            expect(ctrl.exportService.currentExportParameters).toEqual({ exportType: 'CSV', 'exportParameters.csvSeparator': ';' , 'exportParameters.fileName': 'prepname' });
        }));
    });
});