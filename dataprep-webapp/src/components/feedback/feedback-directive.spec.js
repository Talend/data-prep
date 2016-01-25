describe('Feedback directive', function() {
    'use strict';

    var scope, createElement, element, stateMock;
    var body = angular.element('body');
    beforeEach(module('data-prep.feedback', function ($provide) {
        stateMock = {feedback: {
            visible: false
        }};
        $provide.constant('state', stateMock);
    }));

    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<feedback></feedback>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(inject(function () {
        scope.$destroy();
        element.remove();
    }));

    it('should render a feedback form', function() {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('form').length).toBe(1);
    });

    it('should render form labels', function() {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('.feedback-form-line').length).toBe(5);
    });

    it('should render form buttons', function() {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('button').length).toBe(2);
    });

    it('should render form title', function() {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('.title').length).toBe(1);
    });
});