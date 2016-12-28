/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datagrid header directive', () => {
    let scope;
    let createElement;
    let element;
    let ctrl;
    let stateMock;
    const types = [
        { id: 'ANY', name: 'any', labelKey: 'ANY' },
        { id: 'STRING', name: 'string', labelKey: 'STRING' },
        { id: 'NUMERIC', name: 'numeric', labelKey: 'NUMERIC' },
        { id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
        { id: 'DOUBLE', name: 'double', labelKey: 'DOUBLE' },
        { id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
        { id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
        { id: 'DATE', name: 'date', labelKey: 'DATE' },
    ];

    const semanticDomains = [
        {
            "id": "AIRPORT",
            "label": "Airport",
            "frequency": 3.03,
        },
        {
            "id": "CITY",
            "label": "City",
            "frequency": 99.24,
        },
    ];

    const body = angular.element('body');
    const column = {
        id: '0001',
        name: 'MostPopulousCity',
        quality: {
            empty: 5,
            invalid: 10,
            valid: 72,
        },
        type: 'string',
    };

    beforeEach(angular.mock.module('data-prep.datagrid-header', ($provide) => {
        stateMock = {
            playground: {
                preparation: {
                    id: 'prepId',
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, StateService, ColumnTypesService) => {
        spyOn(ColumnTypesService, 'refreshSemanticDomains').and.returnValue($q.when(semanticDomains));
        spyOn(ColumnTypesService, 'refreshTypes').and.returnValue($q.when(types));
    }));

    beforeEach(inject(($rootScope, $compile, $timeout) => {
        scope = $rootScope.$new(true);
        scope.column = column;

        createElement = () => {
            element = angular.element('<datagrid-header column="column"></datagrid-header>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();

            ctrl = element.controller('datagridHeader');
            spyOn(ctrl, 'updateColumnName').and.returnValue();
            ctrl.columnNameEdition = { $commitViewValue: jasmine.createSpy('$commitViewValue') };
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should display column title and domain', () => {
        //given
        scope.column = {
            id: '0001',
            name: 'MostPopulousCity',
            quality: {
                empty: 5,
                invalid: 10,
                valid: 72,
            },
            type: 'string',
            domain: 'city',
        };

        //when
        createElement();

        //then
        expect(element.find('.grid-header-title').text()).toBe('MostPopulousCity');
        expect(element.find('.grid-header-type').text()).toBe('city');
    });

    it('should display column title and type when there is no domain', () => {
        //when
        createElement();

        //then
        expect(element.find('.grid-header-title').text()).toBe('MostPopulousCity');
        expect(element.find('.grid-header-type').text()).toBe('text');
    });

    it('should close dropdown on get transform list error', inject(($timeout, $q, TransformationService) => {
        //given
        createElement();
        spyOn(TransformationService, 'getTransformations').and.returnValue($q.when({ allTransformations : [] }));
        element.find('.grid-header-caret').click();
        const dropdown = element.find('sc-dropdown').eq(0);
        expect(dropdown.hasClass('show')).toBe(true);

        //when
        ctrl.transformationsRetrieveError = true;
        scope.$digest();
        $timeout.flush();

        //then
        expect(dropdown.hasClass('show')).toBe(false);
    }));

    it('should show input to rename column name when double click', () => {
        //given
        createElement();

        const headerTitle = element.find('.grid-header-title').eq(0);
        expect(ctrl.isEditMode).toBeFalsy();

        //when
        headerTitle.dblclick();

        //then
        expect(ctrl.isEditMode).toBeTruthy();
    });

    it('should select input text when edition mode is tuned on', inject(($window, $timeout) => {
        //given
        createElement();

        const headerTitle = element.find('.grid-header-title').eq(0);

        //when
        headerTitle.dblclick();
        $timeout.flush(100);

        //then
        expect(document.activeElement).toBe(element.find('.grid-header-title-input').eq(0)[0]); //eslint-disable-line angular/document-service
        expect($window.getSelection().toString()).toBe('MostPopulousCity');
    }));

    it('should switch from input to text on ESC keydown', () => {
        //given
        createElement();

        ctrl.setEditMode(true);

        const event = angular.element.Event('keydown');
        event.keyCode = 27;

        //when
        element.find('.grid-header-title-input').eq(0).trigger(event);

        //then
        expect(ctrl.isEditMode).toBe(false);
    });

    it('should reset column name on ESC keydown', () => {
        //given
        createElement();

        ctrl.setEditMode(true);
        ctrl.newName = 'toto';

        const event = angular.element.Event('keydown');
        event.keyCode = 27;

        //when
        element.find('.grid-header-title-input').eq(0).trigger(event);

        //then
        expect(ctrl.newName).toBe('MostPopulousCity');
    });

    it('should switch from input to text on ENTER event without changes', () => {
        //given
        createElement();

        ctrl.setEditMode(true);

        const event = angular.element.Event('keydown');
        event.keyCode = 13;

        //when
        element.find('.grid-header-title-input').eq(0).trigger(event);

        //then
        expect(ctrl.isEditMode).toBe(false);
    });

    it('should submit update on ENTER with changes', () => {
        //given
        createElement();

        ctrl.setEditMode(true);
        ctrl.newName = 'MostPopulousCityInTheWorld';

        const event = angular.element.Event('keydown');
        event.keyCode = 13;

        //when
        element.find('.grid-header-title-input').eq(0).trigger(event);

        //then
        expect(ctrl.updateColumnName).toHaveBeenCalled();
        expect(ctrl.columnNameEdition.$commitViewValue).toHaveBeenCalled();
    });

    it('should switch from input to text on BLUR event without changes', () => {
        //given
        createElement();

        ctrl.setEditMode(true);

        const event = angular.element.Event('blur');

        //when
        element.find('.grid-header-title-input').eq(0).trigger(event);

        //then
        expect(ctrl.isEditMode).toBe(false);
    });

    it('should submit update on BLUR event with changes', () => {
        //given
        createElement();

        ctrl.setEditMode(true);
        ctrl.newName = 'MostPopulousCityInTheWorld';

        const event = angular.element.Event('blur');

        //when
        element.find('.grid-header-title-input').eq(0).trigger(event);

        //then
        expect(ctrl.updateColumnName).toHaveBeenCalled();
    });

    it('should stop click propagation in input', () => {
        //given
        createElement();
        const event = angular.element.Event('click');

        //when
        element.find('input').eq(0).trigger(event);

        //then
        expect(event.isPropagationStopped()).toBe(true);
        expect(event.isDefaultPrevented()).toBe(true);
    });

    it('should hide menu on left click on grid-header', () => {
        //given
        createElement();
        element.find('.dropdown-menu').addClass('show-menu');

        //when
        const event = angular.element.Event('mousedown');
        event.which = 1;
        element.find('.grid-header').eq(0).trigger(event);

        //then
        expect(element.find('.dropdown-menu').hasClass('show-menu')).toBeFalsy();
    });

    it('should show menu on right click on grid-header if menu is hidden', inject(($q, TransformationService) => {
        //given
        createElement();
        spyOn(TransformationService, 'getTransformations').and.returnValue($q.when({ allTransformations : []}));
        expect(element.find('sc-dropdown').hasClass('show')).toBeFalsy();

        //when
        const event = angular.element.Event('mouseup');
        event.which = 3;
        element.find('.grid-header').eq(0).trigger(event);

        //then
        expect(element.find('sc-dropdown').hasClass('show')).toBeTruthy();
    }));

    it('should hide menu on right click if menu is visible', () => {
        //given
        createElement();
        element.find('.dropdown-menu').addClass('show-menu');

        //when
        const event = angular.element.Event('mousedown');
        event.which = 3;
        element.find('.grid-header').eq(0).trigger(event);

        //then
        expect(element.find('.dropdown-menu').hasClass('show-menu')).toBeFalsy();
    });
});
