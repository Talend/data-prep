describe('Tabs widget controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('talend.widget'));

    beforeEach(inject(function($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('TalendTabsCtrl', {
                $scope: scope
            });
            ctrl.onTabChange = jasmine.createSpy('onTabChanged');
            return ctrl;
        };
    }));

    it('should save tab in tabs list', function() {
        //given
        var ctrl = createController();
        var tab = {active : false, tabTitle: 'my tab'};

        //when
        ctrl.register(tab);

        //then
        expect(ctrl.tabs).toContain(tab);
    });

    it('should set tab active flag on first element', function() {
        //given
        var ctrl = createController();
        var tab = {active : false, tabTitle: 'my tab'};

        //when
        ctrl.register(tab);

        //then
        expect(tab.active).toBe(true);
    });

    it('should NOT set tab active flag on second element', function() {
        //given
        var ctrl = createController();
        var tab = {active : false, tabTitle: 'my tab'};
        var tab2 = {active : false, tabTitle: 'my tab'};

        //when
        ctrl.register(tab);
        ctrl.register(tab2);

        //then
        expect(tab2.active).toBe(false);
    });

    it('should update active flag on tab selection', function() {
        //given
        var ctrl = createController();
        var tab = {active : false, tabTitle: 'my tab'};
        var tab2 = {active : false, tabTitle: 'my tab'};
        var tab3 = {active : false, tabTitle: 'my tab'};
        var tab4 = {active : false, tabTitle: 'my tab'};

        ctrl.register(tab);
        ctrl.register(tab2);
        ctrl.register(tab3);
        ctrl.register(tab4);

        expect(tab.active).toBe(true);
        expect(tab2.active).toBe(false);
        expect(tab3.active).toBe(false);
        expect(tab4.active).toBe(false);

        //when
        ctrl.select(tab3);

        //then
        expect(tab.active).toBe(false);
        expect(tab2.active).toBe(false);
        expect(tab3.active).toBe(true);
        expect(tab4.active).toBe(false);
    });

    it('should execute tab change callback on tab selection', function() {
        //given
        var ctrl = createController();
        var tab = {active : false, tabTitle: 'my tab'};
        var tab2 = {active : false, tabTitle: 'my tab'};
        var tab3 = {active : false, tabTitle: 'my tab'};
        var tab4 = {active : false, tabTitle: 'my tab'};

        ctrl.register(tab);
        ctrl.register(tab2);
        ctrl.register(tab3);
        ctrl.register(tab4);

        expect(ctrl.onTabChange).not.toHaveBeenCalled();

        //when
        ctrl.select(tab3);

        //then
        expect(ctrl.onTabChange).toHaveBeenCalled();
    });

    it('should unregister tabs', function() {
        //given
        var ctrl = createController();
        var tab = {active : false, tabTitle: 'my tab'};
        var tab2 = {active : false, tabTitle: 'my tab'};
        var tab3 = {active : false, tabTitle: 'my tab'};
        var tab4 = {active : false, tabTitle: 'my tab'};

        ctrl.register(tab);
        ctrl.register(tab2);
        ctrl.register(tab3);
        ctrl.register(tab4);

        //when
        ctrl.unregister(tab2);

        //then
        expect(ctrl.tabs.indexOf(tab2)).toBe(-1);
    });


    it('should update selected tab', function() {
        //given
        var ctrl = createController();
        var tab = {active : true, tabTitle: 'my tab 1'};
        var tab2 = {active : false, tabTitle: 'my tab 2'};
        var tab3 = {active : false, tabTitle: 'my tab 3'};
        var tab4 = {active : false, tabTitle: 'my tab 4'};

        ctrl.register(tab);
        ctrl.register(tab2);
        ctrl.register(tab3);
        ctrl.register(tab4);

        //when
        ctrl.setSelectedTab(2);

        //then
        expect(ctrl.tabs[0].active).toBeFalsy();
        expect(ctrl.tabs[1].active).toBeFalsy();
        expect(ctrl.tabs[2].active).toBeTruthy();
        expect(ctrl.tabs[3].active).toBeFalsy();
    });

    it('should NOT change tab selection when wanted tab does not exist', function() {
        //given
        var ctrl = createController();
        var tab = {active : true, tabTitle: 'my tab 1'};
        var tab2 = {active : false, tabTitle: 'my tab 2'};
        var tab3 = {active : false, tabTitle: 'my tab 3'};
        var tab4 = {active : false, tabTitle: 'my tab 4'};

        ctrl.register(tab);
        ctrl.register(tab2);
        ctrl.register(tab3);
        ctrl.register(tab4);

        //when
        ctrl.setSelectedTab(4);

        //then
        expect(ctrl.tabs[0].active).toBeTruthy();
        expect(ctrl.tabs[1].active).toBeFalsy();
        expect(ctrl.tabs[2].active).toBeFalsy();
        expect(ctrl.tabs[3].active).toBeFalsy();
    });
});
