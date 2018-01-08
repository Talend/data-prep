/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Inventory Item controller', () => {
    'use strict';

    var createController;
    var scope;
    var ctrl;

    beforeEach(angular.mock.module('data-prep.inventory-item'));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new();
        createController = () => {
            return $controller('InventoryItemCtrl', {
                $scope: scope,
            });
        };
    }));

    it('should return tag', inject(() => {
        //when
        ctrl = createController();

        //then
        expect(ctrl.itemType({tag: 'xls'})).toEqual('xls');
    }));

    it('should return type', inject(() => {
        //when
        ctrl = createController();

        //then
        expect(ctrl.itemType({tag: 'local'})).toEqual('local');
    }));
});
