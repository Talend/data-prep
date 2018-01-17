/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datagrid Message Component', () => {
    'use strict';

    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('data-prep.datagrid'));

    beforeEach(inject(($q, $rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<datagrid-message nb-lines="nbLines"' +
                                                        'filters="gridFilters"' +
                                                        'remove-filters="removeAllFilters()">' +
                                        '</datagrid-message>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render insertion-datagrid-message', () => {
        //given
        scope.nbLines = 0;
        scope.gridFilters = [{}];

        //when
        createElement();

        //then
        expect(element.find('.datagrid-message')[0].hasAttribute('insertion-datagrid-message')).toBe(true);
    });

    it('should render content', () => {
        //given
        scope.nbLines = 0;
        scope.gridFilters = [{}];

        //when
        createElement();

        //then
        expect(element.find('span').length).toBe(3);
    });

    it('should call removeAllFilters', () => {
        //given
        scope.nbLines = 0;
        scope.gridFilters = [{}];
        scope.removeAllFilters = () => {};
        spyOn(scope, 'removeAllFilters');

        //when
        createElement();
        element.find('span')[1].click();

        //then
        expect(scope.removeAllFilters).toHaveBeenCalled();
    });

    it('should NOT render datagrid-message', () => {
        //when
        createElement();

        //then
        expect($(element).find('.datagrid-message').length).toBe(0);
    });
});
