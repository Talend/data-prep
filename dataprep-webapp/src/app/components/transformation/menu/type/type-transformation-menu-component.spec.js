/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation menu component', () => {
    'use strict';
    let scope;
    let createElement;
    let element;
    let controller;
    let stateMock;

    const types = [
        { id: 'ANY', name: 'any', labelKey: 'ANY' },
        { id: 'STRING', name: 'string', labelKey: 'STRING' },
        { id: 'NUMERIC', name: 'numeric', labelKey: 'NUMERIC' },
        { id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
        { id: 'DOUBLE', name: 'double', labelKey: 'DOUBLE' },
        { id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
        { id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
        { id: 'DATE', name: 'date', labelKey: 'DATE' },
    ];

    const semanticDomains = [
        {
            "id": "AIRPORT",
            "label": "Airport",
            "frequency": 3.03,
        },
        {
            "id": "CITY",
            "label": "City",
            "frequency": 99.24,
        },
    ];

    beforeEach(angular.mock.module('data-prep.type-transformation-menu', ($provide) => {
        stateMock = {
            playground: {
                preparation: {
                    id: 'prepId'
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            COLUMN_TYPE_IS: 'This column is a ',
            COLUMN_TYPE_SET: 'Set as',
            FLOAT: 'DECIMAL',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($q, $rootScope, $compile, ColumnTypesService) => {
        spyOn(ColumnTypesService, 'getTypes').and.returnValue($q.when(types));
        spyOn(ColumnTypesService, 'getColSemanticDomains').and.returnValue($q.when(semanticDomains));

        scope = $rootScope.$new();
        scope.column = {
            id: '0001',
            domain: 'CITY',
            domainLabel: 'CITY',
            domainFrequency: 18,
            type: 'string',
        };

        createElement = function () {
            element = angular.element('<type-transform-menu column="column"></type-transform-menu>');
            $compile(element)(scope);
            scope.$digest();
            controller = element.controller('typeTransformMenu');
            return element;
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should display domain', () => {
        //given
        createElement();

        //when
        controller.currentSimplifiedDomain = 'CITY';
        scope.$digest();

        //then (beware of the space char between "is a " and "CITY" which is not exactly a space)
        expect(element.find('>li >span').eq(0).text()).toBe('This column is a ');
        expect(element.find('>li >.info').text().trim()).toBe('CITY');
    });

    it('should display simplified type when there is no domain', () => {
        //given
        scope.column.domain = '';
        scope.column.domainLabel = '';
        scope.column.domainCount = 0;

        //when
        createElement();

        //then (beware of the space char between "is a " and "text which is not exactly a space)
        expect(element.find('>li >span').eq(0).text()).toBe('This column is a ');
        expect(element.find('>li >.info').text().trim()).toBe('text');
    });

    it('should render domain items with percentages', () => {
        //when
        createElement();

        //then
        const items = element.find('ul.submenu >li');
        expect(items.length).toBe(8);

        expect(items.eq(0).text().trim()).toBe('City 99.24 %');
        expect(items.eq(1).text().trim()).toBe('Airport 3.03 %');

        expect(items.eq(2).hasClass('divider')).toBe(true);
    });

    it('should render primitive types', () => {
        //when
        createElement();

        //then
        const items = element.find('ul.submenu >li');
        expect(items.length).toBe(8);

        expect(items.eq(2).hasClass('divider')).toBe(true);

        expect(items.eq(3).text().trim()).toBe('Set as STRING');
        expect(items.eq(4).text().trim()).toBe('Set as INTEGER');
        expect(items.eq(5).text().trim()).toBe('Set as DECIMAL');
        expect(items.eq(6).text().trim()).toBe('Set as BOOLEAN');
        expect(items.eq(7).text().trim()).toBe('Set as DATE');
    });

    describe('clicks', () => {
        it('should trigger change domain process', () => {
            // given
            createElement();
            spyOn(controller, 'changeDomain').and.returnValue();
            const items = element.find('ul.submenu >li');

            // when
            items.eq(1).click();
            scope.$digest();

            // then
            expect(controller.changeDomain).toHaveBeenCalledWith(semanticDomains[0]); // because of inverse
        });

        it('should trigger change type process', () => {
            // given
            createElement();
            spyOn(controller, 'changeType').and.returnValue();
            const items = element.find('ul.submenu >li');

            // when
            items.eq(3).click();
            scope.$digest();

            // then
            expect(controller.changeType).toHaveBeenCalledWith(types[1]);
        });
    });
});
