describe('Suggestions stats directive', function() {
    'use strict';

    var scope, createElement, element, stateMock;

    beforeEach(module('data-prep.suggestions-stats', function ($provide) {
        stateMock = {playground: {
            suggestions: {
                isLoading: false
            }
        }};
        $provide.constant('state', stateMock);
    }));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<suggestions-stats></suggestions-stats>');
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render loading element', inject(function() {
        //given
        stateMock.playground.suggestions.isLoading = true;

        //when
        createElement();

        //then
        expect(element.find('.suggestions-loading-div').length).toBe(1);
        expect(element.find('bg-splitter').length).toBe(1);
        expect(element.find('bg-splitter.ng-hide').length).toBe(1);
    }));

    it('should render suggestions/stats splitter', inject(function() {
        //given
        stateMock.playground.suggestions.isLoading = false;

        //when
        createElement();

        //then
        expect(element.find('.suggestions-loading-div').length).toBe(0);
        expect(element.find('bg-splitter').length).toBe(1);
        expect(element.find('bg-splitter.ng-hide').length).toBe(0);
    }));
});