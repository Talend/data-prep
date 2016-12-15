/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Datagrid index header controller', () => {
    'use strict';

    let createController;
    let scope;

    beforeEach(angular.mock.module('data-prep.datagrid-index-header'));
    beforeEach(inject(($rootScope, $componentController, FilterManagerService) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController('datagridIndexHeader', {
                $scope: scope,
            });
        };

        spyOn(FilterManagerService, 'addFilterAndDigest');
    }));

    it('should create invalid_records filter', inject((FilterManagerService) => {
        //given
        const ctrl = createController();

        //when
        ctrl.createFilter('invalid_records');

        //then
        expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('invalid_records');
    }));

    it('should create empty_records filter', inject((FilterManagerService) => {
        //given
        const ctrl = createController();

        //when
        ctrl.createFilter('empty_records');

        //then
        expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('empty_records');
    }));

    it('should create invalid_empty_records filter', inject((FilterManagerService) => {
        //given
        const ctrl = createController();

        //when
        ctrl.createFilter('invalid_empty_records');

        //then
        expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('quality', undefined, undefined, { invalid: true, empty: true });
    }));
});
