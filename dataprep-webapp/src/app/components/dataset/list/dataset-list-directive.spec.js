/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('DatasetList directive', function () {
    'use strict';

    function strEndsWith(str, suffix) {
        return str.match(suffix + '$')[0] === suffix;
    }

    var scope,  createElement, element, stateMock;
    var datasets = [
        {
            'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
            'name': 'US States',
            'author': 'anonymousUser',
            'created': '1437020219741',
            'type': 'text/csv',
            'certificationStep': 'NONE',
            'preparations': [{name:'US States prepa'}, {name:'US States prepa 2'}]
        },
        {
            'id': 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
            'name': 'Customers',
            'author': 'anonymousUser',
            'created': '143702021974',
            'type': 'application/vnd.ms-excel',
            'certificationStep': 'PENDING',
            'preparations': [{name:'Customers prepa'}]
        },
        {
            'id': 'e93b9c92-e054-4f6a-a38f-ca52f22ead3a',
            'name': 'Customers 2',
            'author': 'anonymousUser',
            'created': '14370202197',
            'certificationStep': 'CERTIFIED',
            'preparations': []
        }
    ];

    var sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    var orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('data-prep.dataset-list', function ($provide) {
        stateMock = {
            folder: {
                currentFolderContent: {
                    datasets: datasets
                }
            },
            inventory: {
                datasets: [],
                sortList: sortList,
                orderList: orderList
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('data-prep.services.onboarding'));
    beforeEach(angular.mock.module('data-prep.datagrid'));

    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'INVENTORY_DETAILS': 'owned by {{author}}, created {{created | TDPMoment}}, contains {{records}} lines'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile, $q, DatasetService) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<dataset-list></dataset-list>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };

        //datasetListCtrl.datasets is bound to DatasetListService.datasets
        //DatasetListService.datasets = datasets;
        spyOn(DatasetService, 'getDatasets').and.callFake(function () {
            return $q.when(datasets);
        });
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render dataset list', inject(function ($filter) {
        //given
        var momentize = $filter('TDPMoment');

        //when
        createElement();

        //then
        expect(element.find('.inventory-item').length).toBe(3);

        var icon = element.find('.inventory-icon').eq(0);
        var iconSrc = icon.find('> img')[0].src;
        var certificationIcon = icon.find('.pin');
        expect(strEndsWith(iconSrc, '/assets/images/inventory/csv_file.png')).toBe(true);
        expect(certificationIcon.length).toBe(0);
        expect(element.find('.inventory-title').eq(0).text().indexOf('US States')).toBe(0);
        expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains  lines');

        icon = element.find('.inventory-icon').eq(1);
        iconSrc = icon.find('> img')[0].src;
        certificationIcon = icon.find('.pin')[0].src;
        expect(strEndsWith(iconSrc, '/assets/images/inventory/xls_file.png')).toBe(true);
        expect(strEndsWith(certificationIcon, '/assets/images/certification-pending.png')).toBe(true);
        expect(element.find('.inventory-title').eq(1).text().indexOf('Customers')).toBe(0);
        expect(element.find('.inventory-description').eq(1).text()).toBe('owned by anonymousUser, created ' + momentize('143702021974') + ', contains  lines');

        icon = element.find('.inventory-icon').eq(2);
        iconSrc = icon.find('> img')[0].src;
        certificationIcon = icon.find('.pin')[0].src;
        expect(strEndsWith(iconSrc, '/assets/images/inventory/generic_file.png')).toBe(true);
        expect(strEndsWith(certificationIcon, '/assets/images/certification-certified.png')).toBe(true);
        expect(element.find('.inventory-title').eq(2).text().indexOf('Customers 2')).toBe(0);
        expect(element.find('.inventory-description').eq(2).text()).toBe('owned by anonymousUser, created ' + momentize('14370202197') + ', contains  lines');
    }));

    it('should focus on the name input field', inject(function($rootScope){
        //given
        createElement();
        var ctrl = element.controller('datasetList');
        ctrl.folderDestinationModal = true;
        $rootScope.$digest();

        //when
        ctrl.focusOnNameInput();

        //then
        var activeEl = document.activeElement; //eslint-disable-line angular/document-service
        expect(angular.element(activeEl).attr('id')).toBe('new-name-input-id');
    }));

    it('should create related preparations list', inject(function(){
        expect(element.find('.inventory-item').eq(0).find('.inventory-actions-related-item-menu > li').length).toBe(4);
        expect(element.find('.inventory-item').eq(1).find('.inventory-actions-related-item-menu > li').length).toBe(3);
        expect(element.find('.inventory-item').eq(2).find('.inventory-actions-related-item-menu > li').length).toBe(0);
    }));
});
