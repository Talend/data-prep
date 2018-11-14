/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Feedback directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;
    var stateMock;
    var body = angular.element('body');
    beforeEach(angular.mock.module('data-prep.feedback', function ($provide) {
        stateMock = { feedback: {
                visible: false,
            }, };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $compile) {
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

    it('should render a feedback form', function () {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('form').length).toBe(1);
    });

    it('should render form labels', function () {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('.feedback-form-line').length).toBe(5);
    });

    it('should render form buttons', function () {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('a').length).toBe(2);
    });

    it('should render form title', function () {
        //when
        stateMock.feedback.visible = true;
        createElement();
        scope.$digest();

        //then
        expect(body.find('.title').length).toBe(1);
    });
});
