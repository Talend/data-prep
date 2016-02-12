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


    beforeEach(angular.mock.module('data-prep.inventory-item'));

    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'INVENTORY_DETAILS': 'owned by {{author}}, created {{created | TDPMoment}}, contains {{records}} lines'
        });
        $translateProvider.preferredLanguage('en');
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('display actions', function() {
        beforeEach(inject(function ($rootScope, $compile) {
            scope = $rootScope.$new();

            scope.dataset = dataset;
            scope.openDataset = function(){};
            scope.openRelatedInv = function(){};
            scope.copy = function(){};
            scope.remove = function(){};
            scope.toggleFavorite = function(){};
            scope.preparations = [];
            createElement = function () {
                element = angular.element('<inventory-item ' +
                    'item="dataset" ' +
                    'details="INVENTORY_DETAILS" ' +
                    'type="dataset" ' +
                    'related-inventories="preparations" ' +
                    'related-inventories-type="preparation" ' +
                    'open-related-inv="openPreparation" ' +
                    'open="open" ' +
                    'copy="copy" ' +
                    'rename="rename" ' +
                    'remove="remove"' +
                    'toggle-favorite="toggleFavorite"' +
                    '></inventory-item>');
                $compile(element)(scope);
                scope.$digest();
                ctrl = element.controller('inventoryItem');
                return element;
            };
        }));

        it('should render dataset directive', inject(function ($filter) {
            //given
            var momentize = $filter('TDPMoment');

            //when
            createElement();

            //then
            var icon = element.find('.inventory-icon').eq(0);
            var iconSrc = icon.find('> img')[0].src;
            var certificationIcon = icon.find('.pin');
            expect(strEndsWith(iconSrc, '/assets/images/inventory/csv_file.png')).toBe(true);
            expect(certificationIcon.length).toBe(0);
            expect(element.find('.inventory-title').eq(0).text().indexOf('US States')).toBe(0);
            expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains  lines');
            expect(element.find('a').length).toBe(3); //copy, remove and favorite actions
        }));

        it('should render the related inventory icon and items list', inject(function () {
            //given
            scope.item = dataset;
            scope.preparations = dataset.preparations;

            //when
            createElement();

            //then
            expect(element.find('.inventory-actions-related-item').length).toBe(1);
            var menuItems = element.find('.inventory-actions-related-item-menu > li');
            expect(menuItems.length).toBe(4);
            var relatedPrepsList = menuItems.eq(3).text().trim();
            expect(relatedPrepsList.indexOf(dataset.preparations[1].name) > -1).toBeTruthy();
        }));

        it('should NOT render the related inventory icon and items list when the list is empty', inject(function () {
            //given
            scope.item = dataset;
            scope.preparations = [];

            //when
            createElement();

            //then
            expect(element.find('.inventory-actions-related-item').length).toBe(0);
        }));

        describe('current Inventory Item openings:', function(){
            beforeEach(inject(function() {
                scope.item = dataset;
                scope.preparations = [];
                scope.open = jasmine.createSpy('open');
                scope.rename = jasmine.createSpy('rename');
                createElement();
            }));

            it('should open inventory item on element dblclick', inject(function () {
                //when
                element.find('.inventory-item').dblclick();

                //then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            }));

            it('should open inventory item on file icon click', inject(function () {
                //given
                var icon = element.find('.inventory-icon');

                //when
                icon.click();

                //then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            }));

            it('should open inventory item on inventory title click', inject(function () {
                //given
                var title = element.find('.inventory-title');

                //when
                title.click();

                //then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            }));
        });

        describe('related inventory item openings:', function(){
            beforeEach(inject(function() {
                scope.item = dataset;
                scope.preparations = [{}, {}];
                scope.open = jasmine.createSpy('open');
                scope.rename = jasmine.createSpy('rename');
                createElement();
                ctrl.openRelatedInv = jasmine.createSpy('openRelatedInv');
            }));

            it('should open related inventory item on dblclick', inject(function () {
                //when
                element.find('.inventory-item').dblclick();

                //then
                expect(ctrl.openRelatedInv).toHaveBeenCalledWith(scope.preparations[0]);
            }));

            it('should open related inventory item on bottle click', inject(function () {
                //given
                var bottle = element.find('.inventory-actions-related-item .button-dropdown-main').eq(0);

                //when
                bottle.click();

                //then
                expect(ctrl.openRelatedInv).toHaveBeenCalledWith(scope.preparations[0]);
            }));

            it('should open related inventory item on bottle dblclick', inject(function () {
                //given
                var bottle = element.find('.inventory-actions').eq(0).find('a').eq(0);

                //when
                bottle.dblclick();

                //then
                expect(ctrl.openRelatedInv).toHaveBeenCalledWith(scope.preparations[0]);
            }));

            it('should open new inventory item and not the related inventory', inject(function () {
                //given
                var newPreparation = element.find('.inventory-actions-related-item-menu > li').eq(0);

                //when
                newPreparation.click();

                //then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            }));

            it('should open 2nd related inventory item', inject(function () {
                //given
                var secRelatedInv = element.find('.inventory-actions-related-item-menu > li').eq(3);

                //when
                secRelatedInv.dblclick();

                //then
                expect(ctrl.openRelatedInv).toHaveBeenCalledWith(scope.preparations[1]);
            }));
        });
    });
});
