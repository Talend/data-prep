/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Datagrid index header Component', () => {
    let createElement;
    let scope;
    let element;

    beforeEach(angular.mock.module('data-prep.datagrid-index-header'));
    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<datagrid-index-header></datagrid-index-header>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render index filter menu', () => {
        // given
        createElement();

        // then
        expect(element.find('sc-dropdown').size()).toBe(1);
        expect(element.find('sc-dropdown li').size()).toBe(3);

        expect(element.find('sc-dropdown li a[translate-once="DISPLAY_ROWS_INVALID_VALUES"]').length).toBe(1);
        expect(element.find('sc-dropdown li a[translate-once="DISPLAY_ROWS_EMPTY_VALUES"]').length).toBe(1);
        expect(element.find('sc-dropdown li a[translate-once="DISPLAY_ROWS_INVALID_EMPTY_VALUES"]').length).toBe(1);
    });
});
