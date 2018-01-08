/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Lookup datagrid header controller', function () {
    'use strict';

    var createController;
    var scope;
    var stateMock;

    beforeEach(angular.mock.module('data-prep.lookup', function ($provide) {
        stateMock = {
            playground: {
                lookup: {
                    selectedColumn: {
                        id: '0000',
                        name: 'lookupGridColName',
                    },
                    columnsToAdd: ['0002', '0003'],
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('LookupDatagridHeaderCtrl', {
                $scope: scope,
            });
        };
    }));

    it('should show the checkboxes', inject(function () {
        //given
        var ctrl = createController();
        ctrl.column = {
            id: '0001',
        };
        //when
        var showCheckbox = ctrl.showCheckbox();

        //then
        expect(showCheckbox).toBeTruthy();
    }));

    it('should hide the checkboxes', inject(function () {
        //given
        var ctrl = createController();
        ctrl.column = {
            id: '0000',
        };
        //when
        var showCheckbox = ctrl.showCheckbox();

        //then
        expect(showCheckbox).toBeFalsy();
    }));

    it('should update the array of the selected columns to be added to the lookup', inject(function (StateService) {
        //given
        var ctrl = createController();
        spyOn(StateService, 'updateLookupColumnsToAdd').and.returnValue();
        var event = angular.element.Event('click');

        //when
        ctrl.updateColsToAdd(event);

        //then
        expect(StateService.updateLookupColumnsToAdd).toHaveBeenCalled();
    }));
});
