describe('Dataset list directive', function() {
    'use strict';

    function strEndsWith(str, suffix) {
        return str.match(suffix + '$')[0] === suffix;
    }

    var scope, createElement, element;
    var datasets = [
        {
            'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
            'name': 'US States',
            'author': 'anonymousUser',
            'created': '1437020219741',
            'type': 'text/csv',
            'certificationStep': 'NONE'
        },
        {
            'id': 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
            'name': 'Customers',
            'author': 'anonymousUser',
            'created': '143702021974',
            'type': 'application/vnd.ms-excel',
            'certificationStep': 'PENDING'
        },
        {
            'id': 'e93b9c92-e054-4f6a-a38f-ca52f22ead3a',
            'name': 'Customers 2',
            'author': 'anonymousUser',
            'created': '14370202197',
            'certificationStep': 'CERTIFIED'
        }
    ];

    beforeEach(module('data-prep.dataset-list'));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'INVENTORY_DETAILS': 'owned by {{author}}, created {{created | moment_from_now}}, contains {{records}} lines'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile, $q, DatasetService) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<dataset-list></dataset-list>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };

        spyOn(DatasetService, 'datasetsList').and.returnValue(datasets);
        spyOn(DatasetService, 'getDatasets').and.callFake(function() {
            return $q.when(datasets);
        });
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render dataset list', function($moment) {
        //when
        createElement();

        //then
        var icon = element.find('.inventory-icon').eq(0);
        var iconSrc = icon.find('> img')[0].src;
        var certificationIcon = icon.find('.pin');
        expect(strEndsWith(iconSrc, '/assets/images/inventory/csv_file.png')).toBe(true);
        expect(certificationIcon.length).toBe(0);
        expect(element.find('.inventory-title').eq(0).text()).toBe('US States');
        expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created '+$moment('1437020219741','x').fromNow()+', contains  lines');

        icon = element.find('.inventory-icon').eq(1);
        iconSrc = icon.find('> img')[0].src;
        certificationIcon = icon.find('.pin')[0].src;
        expect(strEndsWith(iconSrc, '/assets/images/inventory/xls_file.png')).toBe(true);
        expect(strEndsWith(certificationIcon, '/assets/images/certification-pending.png')).toBe(true);
        expect(element.find('.inventory-title').eq(1).text()).toBe('Customers');
        expect(element.find('.inventory-description').eq(1).text()).toBe('owned by anonymousUser, created '+$moment('143702021974','x').fromNow()+', contains  lines');

        icon = element.find('.inventory-icon').eq(2);
        iconSrc = icon.find('> img')[0].src;
        certificationIcon = icon.find('.pin')[0].src;
        expect(strEndsWith(iconSrc, '/assets/images/inventory/generic_file.png')).toBe(true);
        expect(strEndsWith(certificationIcon, '/assets/images/certification-certified.png')).toBe(true);
        expect(element.find('.inventory-title').eq(2).text()).toBe('Customers 2');
        expect(element.find('.inventory-description').eq(2).text()).toBe('owned by anonymousUser, created '+$moment('14370202197','x').fromNow()+', contains  lines');
    });
});
