describe('Actions suggestions-stats directive', function() {
    'use strict';

    var scope, element, createElement;
    var body = angular.element('body');
    beforeEach(module('data-prep.actions-suggestions'));
    beforeEach(module('htmlTemplates'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));
    
    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<actions-suggestions></actions-suggestions>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should set column name in title', inject(function(SuggestionService) {
        //given
        SuggestionService.currentColumn = {name: 'Col 1'};

        //when
        createElement();

        //then
        expect(element.find('.title').text().trim()).toBe('Col 1');
    }));
});
