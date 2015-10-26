describe('App directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(module('data-prep.app'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<dataprep-app></dataprep-app>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    beforeEach(inject(function($injector, RestURLs) {
        RestURLs.setServerUrl('');

        var $httpBackend = $injector.get('$httpBackend');
        $httpBackend
            .expectGET(RestURLs.exportUrl + '/formats')
            .respond(200, {});
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should hold toaster container', function() {
        //when
        createElement();

        //then
        expect(element.find('#toast-container').length).toBe(1);
    });

    it('should hold loader element', function() {
        //when
        createElement();

        //then
        expect(element.find('talend-loading').length).toBe(1);
    });

    it('should hold playground element', function() {
        //when
        createElement();

        //then
        expect(element.find('playground').length).toBe(1);
    });

    it('should render router insertion point', function() {
        //when
        createElement();

        //then
        expect(element.find('ui-view.main-layout').length).toBe(1);
    });
});