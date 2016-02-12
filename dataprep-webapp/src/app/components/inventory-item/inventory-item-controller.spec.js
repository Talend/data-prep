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

    var createController, scope, ctrl;

    beforeEach(angular.mock.module('data-prep.inventory-item'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = function () {
            return $controller('InventoryItemCtrl', {
                $scope: scope
            });
        };
    }));

    describe('call action when actions are enabled', function() {
        beforeEach(inject(function() {
            ctrl = createController();
            ctrl.open = jasmine.createSpy('open');
            ctrl.openRelatedInv = jasmine.createSpy('openRelatedInv');
        }));

        it('should call open related inventory callback', inject(function () {
            //given
            var prep = {};

            //when
            ctrl.openRelatedInventoryItem(prep);

            //then
            expect(ctrl.openRelatedInv).toHaveBeenCalledWith(prep);
        }));

        //it('should NOT call open related inventory callback', inject(function () {
        //    //given
        //    var prep = {};
        //    ctrl.openRelatedInv = null;
        //
        //    //when
        //    ctrl.openRelatedInventoryItem(prep);
        //
        //    //then
        //    expect(ctrl.openRelatedInv).not.toHaveBeenCalledWith();
        //}));

        it('should process the tooltip data to compile it when related inventories do NOT exist', inject(function () {
            //given
            ctrl.relatedInventories = [];
            ctrl.type = 'dataset';
            ctrl.item = {
                name: 'my dataset name'
            };

            //when
            var tooltipData = ctrl.getTooltipContent();

            //then
            expect(tooltipData).toEqual({
                type: 'dataset',
                name: 'my dataset name'
            });
        }));

        it('should process the tooltip data to compile it when related inventories exist', inject(function () {
            //given
            ctrl.relatedInventories = [{name:'prep1'}, {name:'prep2'}];
            ctrl.relatedInventoriesType = 'preparation';

            //when
            var tooltipData = ctrl.getTooltipContent();

            //then
            expect(tooltipData).toEqual({
                type: 'preparation',
                name: 'prep1'
            });
        }));

        it('should open an inventory Item: the case of related inventory', inject(function () {
            //given
            ctrl.relatedInventories = [{name:'prep1'}, {name:'prep2'}];
            ctrl.actionsEnabled = true;

            //when
            ctrl.openInventoryItem();

            //then
            expect(ctrl.openRelatedInv).toHaveBeenCalledWith(ctrl.relatedInventories[0]);
        }));

        it('should open an inventory Item: the case of the item itself', inject(function () {
            //given
            ctrl.relatedInventories = [];
            ctrl.actionsEnabled = true;
            ctrl.item = {};

            //when
            ctrl.openInventoryItem();

            //then
            expect(ctrl.open).toHaveBeenCalledWith(ctrl.item);
        }));

    });
});
