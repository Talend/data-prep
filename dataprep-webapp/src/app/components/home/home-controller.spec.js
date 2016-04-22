/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Home controller', () => {
    'use strict';

    let ctrl, createController, scope, $httpBackend, StateMock;
    const DATA_INVENTORY_PANEL_KEY = 'org.talend.dataprep.data_inventory_panel_display';
    const dataset = { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false };

    beforeEach(angular.mock.module('data-prep.home', ($provide) => {
        StateMock = {
            dataset: { uploadingDatasets: [] },
            inventory: {}
        };
        $provide.constant('state', StateMock);
    }));

    beforeEach(inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.when('GET', 'i18n/en.json').respond({});
        $httpBackend.when('GET', 'i18n/fr.json').respond({});
    }));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => $componentController('home', { $scope: scope });
    }));

    afterEach(inject(($window) => {
        $window.localStorage.removeItem(DATA_INVENTORY_PANEL_KEY);
    }));

    it('should init upload list to empty array', () => {
        //when
        ctrl = createController();

        //then
        expect(ctrl.uploadingDatasets).toEqual([]);
    });

    it('should init right panel state with value from local storage', inject(($window) => {
        //given
        $window.localStorage.setItem(DATA_INVENTORY_PANEL_KEY, 'true');

        //when
        ctrl = createController();

        //then
        expect(ctrl.showRightPanel).toBe(true);
    }));

    describe('with created controller', () => {

        beforeEach(inject(function () {
            ctrl = createController();
        }));
        
        describe('right panel management', () => {
            it('should toggle right panel flag', inject(() => {
                //given
                expect(ctrl.showRightPanel).toBe(false);

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanel).toBe(true);

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanel).toBe(false);
            }));

            it('should save toggled state in local storage', inject(($window) => {
                //given
                expect(JSON.parse($window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY))).toBeFalsy();

                //when
                ctrl.toggleRightPanel();

                //then
                expect(JSON.parse($window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY))).toBeTruthy();
                //when
                ctrl.toggleRightPanel();

                //then
                expect(JSON.parse($window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY))).toBeFalsy();
            }));

            it('should update right panel icon', inject(() => {
                //given
                expect(ctrl.showRightPanelIcon).toBe('u');

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanelIcon).toBe('t');

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanelIcon).toBe('u');
            }));
        });
    });
});
