describe('Upgrade version component', () => {
    let scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.upgrade-version'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<upgrade-version></upgrade-version>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should not render when no new version available', inject(($q, UpgradeVersionService) => {
        //given
        spyOn(UpgradeVersionService, "retrieveNewVersions").and.returnValue($q.when([]));
        createElement();

        //then
        expect(element.find('.upgrade-version').length).toBe(0);
    }));

    it('should render when new version available', inject(($q, UpgradeVersionService) => {
        //given
        spyOn(UpgradeVersionService, "retrieveNewVersions").and.returnValue($q.when([{"version": "2.0.0"}]));
        createElement();

        //then
        expect(element.find('.upgrade-version').length).toBe(1);
    }));
});