/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation menu directive', function () {
    'use strict';
    var scope, createElement, element;

    var types = [
        {'id': 'ANY', 'name': 'any', 'labelKey': 'ANY'},
        {'id': 'STRING', 'name': 'string', 'labelKey': 'STRING'},
        {'id': 'NUMERIC', 'name': 'numeric', 'labelKey': 'NUMERIC'},
        {'id': 'INTEGER', 'name': 'integer', 'labelKey': 'INTEGER'},
        {'id': 'DOUBLE', 'name': 'double', 'labelKey': 'DOUBLE'},
        {'id': 'FLOAT', 'name': 'float', 'labelKey': 'FLOAT'},
        {'id': 'BOOLEAN', 'name': 'boolean', 'labelKey': 'BOOLEAN'},
        {'id': 'DATE', 'name': 'date', 'labelKey': 'DATE'}
    ];

    beforeEach(angular.mock.module('data-prep.transformation-menu'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'COLUMN_TYPE_IS': 'This column is a ',
            'COLUMN_TYPE_SET': 'Set as',
            'FLOAT': 'DECIMAL'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($q, $rootScope, $compile, ColumnTypesService) {
        spyOn(ColumnTypesService, 'getTypes').and.returnValue($q.when(types));

        scope = $rootScope.$new();
        scope.column = {
            id: '0001',
            domain: 'CITY',
            domainLabel: 'CITY',
            domainFrequency: 18,
            type: 'string',
            semanticDomains: [
                {id: '', label: '', frequency: 15},
                {id: 'CITY', label: 'CITY', frequency: 18},
                {id: 'REGION', label: 'REGION', frequency: 6},
                {id: 'COUNTRY', label: 'COUNTRY', frequency: 17}
            ]
        };

        createElement = function () {
            element = angular.element('<type-transform-menu column="column"></type-transform-menu>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should display domain', function () {
        //when
        createElement();

        //then (beware of the space char between "is a " and "CITY" which is not exactly a space)
        expect(element.find('>li >span').eq(0).text()).toBe('This column is a ');
        expect(element.find('>li >a').text().trim()).toBe('CITY');
    });

    it('should display simplified type when there is no domain', function () {
        //given
        scope.column.domain = '';
        scope.column.domainLabel = '';
        scope.column.domainCount = 0;

        //when
        createElement();

        //then (beware of the space char between "is a " and "text which is not exactly a space)
        expect(element.find('>li >span').eq(0).text()).toBe('This column is a ');
        expect(element.find('>li >a').text().trim()).toBe('text');
    });

    it('should render domain items with percentages', function () {
        //when
        createElement();

        //then
        var items = element.find('ul.submenu >li');
        expect(items.length).toBe(11);

        expect(items.eq(0).text().trim()).toBe('CITY 18 %');
        expect(items.eq(1).text().trim()).toBe('COUNTRY 17 %');
        expect(items.eq(2).text().trim()).toBe('REGION 6 %');

        expect(items.eq(3).hasClass('divider')).toBe(true);
    });

    it('should render primitive types', function () {
        //when
        createElement();

        //then
        var items = element.find('ul.submenu >li');
        expect(items.length).toBe(11);

        expect(items.eq(3).hasClass('divider')).toBe(true);

        expect(items.eq(4).text().trim()).toBe('Set as STRING');
        expect(items.eq(5).text().trim()).toBe('Set as INTEGER');
        expect(items.eq(6).text().trim()).toBe('Set as DECIMAL');
        expect(items.eq(7).text().trim()).toBe('Set as BOOLEAN');
        expect(items.eq(8).text().trim()).toBe('Set as DATE');
    });
});
