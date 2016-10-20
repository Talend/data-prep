/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Navbar directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;
    var stateMock;
    let $httpBackend;

    beforeEach(angular.mock.module('data-prep.navbar', ($provide) => {
        stateMock = {
            ee: false,
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.whenGET('assets/images/header/board.svg').respond(200, '');
        $httpBackend.whenGET('assets/images/header/info.svg').respond(200, '');
        $httpBackend.whenGET('assets/images/header/search.svg').respond(200, '');
        $httpBackend.whenGET('assets/images/header/bubble.svg').respond(200, '');
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<navbar></navbar>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    describe('rendering elements', () => {
        it('should render navigation bar', function () {
            //when
            createElement();

            //then
            expect(element.find('talend-navbar').length).toBe(1);
        });

        it('should render footer bar', function () {
            //when
            createElement();

            //then
            expect(element.find('footer').length).toBe(1);
        });

        it('should render content insertion point', function () {
            //when
            createElement();

            //then
            expect(element.find('ui-view.content').length).toBe(1);
        });

        it('should render navigation items insertion point', function () {
            //when
            createElement();

            //then
            expect(element.find('.navigation-items[insertion-home-right-header]').length).toBe(1);
        });

        it('should render feedback form', function () {
            //when
            stateMock.ee = undefined;
            createElement();

            //then
            expect(element.find('#message-icon').length).toBe(1);
        });

        it('should render onboarding icon', function () {
            //when
            createElement();

            //then
            expect(element.find('#onboarding-icon').length).toBe(1);
        });

        it('should not render feedback form', function () {
            //when
            stateMock.ee = true;
            createElement();

            //then
            expect(element.find('#message-icon').length).toBe(0);
        });
    });

    describe('events', () => {
        it('should launch onboarding storytelling on icon click', () => {
            //given
            createElement();
            var ctrl = element.controller('navbar');
            spyOn(ctrl, 'startTour').and.returnValue();

            //when
            element.find('#onboarding-icon').eq(0).click();
            scope.$digest();

            //then
            expect(ctrl.startTour).toHaveBeenCalledWith('preparation');
        });
    });
});
