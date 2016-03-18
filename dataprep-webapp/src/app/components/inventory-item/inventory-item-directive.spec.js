/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('InventoryItem directive', function () {
    'use strict';

    function strEndsWith(str, suffix) {
        return str.match(suffix + '$')[0] === suffix;
    }

    var scope,  createElement, element, ctrl;
    var dataset = {
        'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        'name': 'US States',
        'author': 'anonymousUser',
        'created': '1437020219741',
        'type': 'text/csv',
        'certificationStep': 'NONE',
        'preparations': [{name:'US States prepa'}, {name:'US States prepa 2'}]
    };

    var certifiedDataset = {
        'id': '888888-bf80-41c8-92e5-66d70f22ec1f',
        'name': 'cars',
        'author': 'root',
        'created': '1437020219741',
        'type': 'text/csv',
        'certificationStep': 'certified',
        'preparations': [{name:'US States prepa'}, {name:'US States prepa 2'}]
    };

    beforeEach(angular.mock.module('data-prep.inventory-item'));

    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'OPEN_ACTION':'Open {{type}} \"{{name}}\"',
            'INVENTORY_DETAILS': 'owned by {{author}}, created {{created | TDPMoment}}, contains {{records}} lines',
            'COPY_CLONE_ACTION': 'Copy or Clone {{type}} \"{{name}}\"',
            'DELETE_ACTION': 'Delete {{type}} \"{{name}}\"',
            'CERTIFY_ACTION': 'Certify {{type}} \"{{name}}\"',
            'FAVORITE_ACTION': 'Add {{type}} \"{{name}}\" in your favorites'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        scope.dataset = dataset;
        scope.openDataset = function(){};
        scope.openRelatedInventory = function(){};
        scope.copy = function(){};
        scope.processCertif = function(){};
        scope.rename = function(){};
        scope.open = function(){};
        scope.update = function(){};
        scope.remove = function(){};
        scope.openRelatedInv = function(){};
        scope.toggleFavorite = function(){};
        scope.preparations = [];
        createElement = function () {
            element = angular.element('<inventory-item ' +
                'item="dataset" ' +
                'details="INVENTORY_DETAILS" ' +
                'type="dataset" ' +
                'related-inventories="preparations" ' +
                'related-inventories-type="preparation" ' +
                'open-related-inventory="openRelatedInventory" ' +
                'open="open" ' +
                'process-certification="processCertif" ' +
                'copy="copy" ' +
                'rename="rename" ' +
                'remove="remove" ' +
                'toggle-favorite="toggleFavorite" ' +
                'update="update" ' +
                '></inventory-item>');
            $compile(element)(scope);
            scope.$digest();
            ctrl = element.controller('inventoryItem');
            return element;
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('display inventory components', function() {
        it('should display inventory icon without certification pin', function () {
            //when
            createElement();

            //then
            var icon = element.find('.inventory-icon').eq(0);
            var certificationIcon = icon.find('.pin');
            expect(certificationIcon.length).toBe(0);
        });
        it('should display CSV icon', function () {
            //when
            createElement();

            //then
            var icon = element.find('.inventory-icon').eq(0);
            var iconSrc = icon.find('> img')[0].src;
            expect(strEndsWith(iconSrc, '/assets/images/inventory/csv_file.png')).toBe(true);
        });
        it('should display inventory icon with certification pin', function () {
            //when
            scope.dataset = certifiedDataset;
            createElement();

            //then
            var icon = element.find('.inventory-icon').eq(0);
            var certificationIcon = icon.find('.pin');
            expect(certificationIcon.length).toBe(1);
        });

        it('should display inventory icon tooltip', function () {
            //when
            createElement();

            //then
            var title = element.find('.inventory-icon').eq(0).attr('title').trim();
            expect(title).toBe('Open ' + ctrl.type + ' "' + dataset.name + '"');
        });
        it('should display related inventory icon tooltip', function () {
            //given
            scope.preparations = dataset.preparations;

            //when
            createElement();

            //then
            var title = element.find('.inventory-icon').eq(0).attr('title').trim();
            expect(title).toBe('Open ' + ctrl.relatedInventoriesType + ' "' + dataset.preparations[0].name + '"');
        });

        it('should display inventory details', inject(function ($filter) {
            //given
            var momentize = $filter('TDPMoment');

            //when
            createElement();

            //then
            expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains  lines');
        }));


        it('should display inventory title', function () {
            //when
            createElement();

            //then
            var title = element.find('talend-editable-text').eq(0).text().trim();
            expect(title).toBe(dataset.name);
        });

        it('should NOT display bottle icon: no related inventories', function () {
            //when
            createElement();

            //then
            expect(element.find('.inventory-actions-related-item').length).toBe(0);
        });
        it('should display bottle icon: at least 1 related inventory', function () {
            //given
            scope.preparations = dataset.preparations;

            //when
            createElement();

            //then
            expect(element.find('.inventory-actions-related-item').length).toBe(1);
            var menuItems = element.find('.inventory-actions-related-item-menu > li');
            expect(menuItems.length).toBe(4);
            var relatedPrepsList = menuItems.eq(3).text().trim();
            expect(relatedPrepsList.indexOf(dataset.preparations[1].name) > -1).toBeTruthy();
        });

        it('should display update icon', function () {
            //when
            createElement();

            //then
            var icon = element.find('talend-file-selector').attr('button-data-icon');
            expect(icon).toBe('E');
        });
        it('should display update icon tooltip', function () {
            //when
            createElement();

            //then
            var title = element.find('talend-file-selector').attr('button-title');
            expect(title).toBe('REPLACE_FILE_CONTENT');
        });

        it('should display 2 dividers', function () {
            //when
            createElement();

            //then
            expect(element.find('.divider').length).toBe(2);
        });

        it('should display copy/clone icon', function () {
            //when
            createElement();

            //then
            var icon = element.find('a').eq(0).attr('data-icon');
            expect(icon).toBe('B');
        });
        it('should display copy/clone icon tooltip', function () {
            //when
            createElement();

            //then
            var title = element.find('a').eq(0).attr('title');
            expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
        });

        it('should display remove icon', function () {
            //when
            createElement();

            //then
            var icon = element.find('a').eq(1).attr('data-icon');
            expect(icon).toBe('e');
        });
        it('should display remove icon tooltip', function () {
            //when
            createElement();

            //then
            var title = element.find('a').eq(1).attr('title');
            expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
        });

        it('should display certify icon', function () {
            //when
            createElement();

            //then
            var icon = element.find('a').eq(2).attr('data-icon');
            expect(icon).toBe('n');
        });
        it('should display certify icon tooltip', function () {
            //when
            createElement();

            //then
            var title = element.find('a').eq(2).attr('title');
            expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
        });

        it('should display favorite icon', function () {
            //when
            createElement();

            //then
            var icon = element.find('a').eq(3).attr('data-icon');
            expect(icon).toBe('f');
        });

        it('should display favorite icon tooltip', function () {
            //when
            createElement();

            //then
            var title = element.find('a').eq(3).attr('title');
            expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
        });
    });

    describe('actions on inventory components', function() {
        beforeEach(inject(function() {
            scope.openRelatedInventory = jasmine.createSpy('openRelatedInventory');
            scope.open = jasmine.createSpy('open');
            scope.rename = jasmine.createSpy('rename');
            scope.update = jasmine.createSpy('update');
            scope.copy = jasmine.createSpy('copy');
            scope.remove = jasmine.createSpy('remove');
            scope.processCertif = jasmine.createSpy('processCertif');
            scope.toggleFavorite = jasmine.createSpy('toggleFavorite');
        }));

        it('should open inventory item on file icon click', function () {
            //given
            createElement();
            var icon = element.find('.inventory-icon');

            //when
            icon.click();

            //then
            expect(ctrl.open).toHaveBeenCalledWith(dataset);
        });
        it('should open the related inventory item on file icon click', function () {
            //given
            scope.preparations = [{}, {}];
            createElement();
            var icon = element.find('.inventory-icon');

            //when
            icon.click();

            //then
            expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[0]);
        });

        it('should open inventory item on inventory title click', function () {
            //given
            createElement();
            var title = element.find('.inventory-title');

            //when
            title.click();

            //then
            expect(ctrl.open).toHaveBeenCalledWith(dataset);
        });
        it('should open the related inventory item on inventory title click', function () {
            //given
            scope.preparations = [{}, {}];
            createElement();
            var title = element.find('.inventory-title');

            //when
            title.click();

            //then
            expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[0]);
        });

        it('should open inventory item on element dblclick', function () {
            //given
            createElement();
            var inventory = element.find('.inventory-item');

            //when
            inventory.dblclick();

            //then
            expect(ctrl.open).toHaveBeenCalledWith(dataset);
        });
        it('should open the related inventory item on element dblclick', function () {
            //given
            scope.preparations = [{}, {}];
            createElement();
            var inventory = element.find('.inventory-item');

            //when
            inventory.dblclick();

            //then
            expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[0]);
        });
        it('should open related inventory item on bottle click', function () {
            //given
            scope.preparations = [{}, {}];
            createElement();
            var bottle = element.find('.inventory-actions-related-item .button-dropdown-main').eq(0);

            //when
            bottle.click();

            //then
            expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[0]);
        });
        it('should open new inventory item and not the related inventory', function () {
            //given
            scope.preparations = [{}, {}];
            createElement();
            var newPreparation = element.find('.inventory-actions-related-item-menu > li').eq(0);

            //when
            newPreparation.click();

            //then
            expect(ctrl.open).toHaveBeenCalledWith(dataset);
        });
        it('should open 2nd related inventory item', function () {
            //given
            scope.preparations = [{}, {}];
            createElement();
            var secRelatedInv = element.find('.inventory-actions-related-item-menu > li').eq(3);

            //when
            secRelatedInv.dblclick();

            //then
            expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[1]);
        });

        it('should copy/clone inventory item on clone button click', function () {
            //given
            createElement();
            var cloneBtn = element.find('a').eq(0);

            //when
            cloneBtn.click();

            //then
            expect(ctrl.copy).toHaveBeenCalled();
        });
        it('should remove inventory item on basket button click', function () {
            //given
            createElement();
            var basketBtn = element.find('a').eq(1);

            //when
            basketBtn.click();

            //then
            expect(ctrl.remove).toHaveBeenCalled();
        });
        it('should certify inventory item on certification button click', function () {
            //given
            createElement();
            var certificationBtn = element.find('a').eq(2);

            //when
            certificationBtn.click();

            //then
            expect(ctrl.processCertification).toHaveBeenCalled();
        });
        it('should favorite inventory item on favorite button click', function () {
            //given
            createElement();
            var favoriteBtn = element.find('a').eq(3);

            //when
            favoriteBtn.click();

            //then
            expect(ctrl.toggleFavorite).toHaveBeenCalled();
        });
    });
});
