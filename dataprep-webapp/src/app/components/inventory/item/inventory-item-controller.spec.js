/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Inventory Item controller', function () {
    'use strict';

    var createController;
    var scope;
    var ctrl;

    beforeEach(angular.mock.module('data-prep.inventory-item'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = function () {
            return $controller('InventoryItemCtrl', {
                $scope: scope,
            });
        };
    }));

    describe('call action when actions are enabled', function () {
        beforeEach(inject(function () {
            ctrl = createController();
            ctrl.open = jasmine.createSpy('open');
            ctrl.openRelatedInventory = jasmine.createSpy('openRelatedInventory');
        }));

        it('should call open related inventory callback', inject(function () {
            //given
            var prep = {};

            //when
            ctrl.openRelatedInventoryItem(prep);

            //then
            expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(prep);
        }));

        it('should process the tooltip data to compile it', inject(function () {
            //given
            ctrl.relatedInventories = [];
            ctrl.type = 'dataset';
            ctrl.item = {
                name: 'my dataset name',
            };

            //when
            var tooltipData = ctrl.getTooltipContent(false);

            //then
            expect(tooltipData).toEqual({
                type: 'dataset',
                name: 'my dataset name',
            });
        }));

        it('should process the tooltip data to compile it by using tooltipname', inject(function () {
            //given
            ctrl.relatedInventories = [];
            ctrl.type = 'dataset';
            ctrl.item = {
                name: 'my dataset name',
                tooltipName: 'my dataset tooltip',

            };

            //when
            var tooltipData = ctrl.getTooltipContent(false);

            //then
            expect(tooltipData).toEqual({
                type: 'dataset',
                name: 'my dataset tooltip',
            });
        }));

        it('should process the tooltip data to compile it for related inventories', inject(function () {
            //given
            ctrl.relatedInventories = [{ name: 'prep1' }, { name: 'prep2' }];
            ctrl.relatedInventoriesType = 'preparation';

            //when
            var tooltipData = ctrl.getTooltipContent(true);

            //then
            expect(tooltipData).toEqual({
                type: 'preparation',
                name: 'prep1',
            });
        }));
    });

    describe('Href Link', function() {
        beforeEach(inject(function() {
            ctrl = createController();
        }));

        it('should return dataset Href link', inject(function () {
            //given
            ctrl.type = 'dataset';
            ctrl.item = {
                id: '0001'
            };

            //then
            expect(ctrl.getItemHrefLink()).toEqual('#/playground/dataset?datasetid=0001');
        }));

        it('should return folder Href link', inject(function () {
            //given
            ctrl.type = 'folder';
            ctrl.item = {
                id: '0001'
            };

            //then
            expect(ctrl.getItemHrefLink()).toEqual('#/index/preparations/0001');
        }));

        it('should return preparation Href link', inject(function () {
            //given
            ctrl.type = 'preparation';
            ctrl.item = {
                id: '0001'
            };

            //then
            expect(ctrl.getItemHrefLink()).toEqual('#/playground/preparation?prepid=0001');
        }));
    });
});
