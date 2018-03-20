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

describe('Export REST Service', () => {
    let $httpBackend;
    const exportTypes = [
        {
            mimeType: 'text/csv',
            extension: '.csv',
            id: 'CSV',
            needParameters: true,
            defaultExport: false,
            parameters: [{
                name: 'csvSeparator',
                labelKey: 'CHOOSE_SEPARATOR',
                type: 'radio',
                defaultValue: { value: ';', labelKey: 'SEPARATOR_SEMI_COLON' },
                values: [
                    { value: '&#09;', labelKey: 'SEPARATOR_TAB' },
                    { value: ' ', labelKey: 'SEPARATOR_SPACE' },
                    { value: ',', labelKey: 'SEPARATOR_COMMA' },
                ],
            },],
        },
        {
            mimeType: 'application/tde',
            extension: '.tde',
            id: 'TABLEAU',
            needParameters: false,
            defaultExport: false,
        },
        {
            mimeType: 'application/vnd.ms-excel',
            extension: '.xls',
            id: 'XLS',
            needParameters: false,
            defaultExport: true,
        },
    ];

    beforeEach(angular.mock.module('data-prep.services.export'));

    beforeEach(inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
    }));

    it('should get export types', inject(($rootScope, RestURLs, ExportRestService) => {
        //given
        let types = null;
        $httpBackend
            .expectGET(`${RestURLs.exportUrl}/formats/datasets/myDatasetId`)
            .respond(200, exportTypes);

        //when
        ExportRestService.exportTypes('datasets', 'myDatasetId')
            .then((response) => {
                types = response;
            });
        $httpBackend.flush();
        $rootScope.$digest();

        //then
        expect(types).toEqual(exportTypes);
    }));
});
