/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Feedback controller', function () {
    'use strict';

    var createController;
    var scope;
    var $stateMock;

    beforeEach(angular.mock.module('data-prep.feedback'));

    beforeEach(inject(function ($rootScope, $controller) {
        jasmine.clock().install();
        scope = $rootScope.$new();
        $stateMock = {};

        createController = function () {
            return $controller('FeedbackCtrl', {
                $scope: scope,
                $state: $stateMock,
            });
        };
    }));

    afterEach(function () {
        jasmine.clock().uninstall();
    });

    describe('feedback ', function () {
        beforeEach(inject(function ($q, FeedbackRestService, MessageService, StorageService) {
            spyOn(FeedbackRestService, 'sendFeedback').and.returnValue($q.when(true));
            spyOn(MessageService, 'success').and.returnValue(false);
            spyOn(StorageService, 'saveFeedbackUserMail');
            spyOn(StorageService, 'getFeedbackUserMail').and.returnValue('test mail');
        }));

        it('should initialize feedback', inject(function () {
            //given
            var ctrl = createController();
            //then
            expect(ctrl.feedback.mail).toBe('test mail');
        }));

        it('should send feedback', inject(function (FeedbackRestService) {
            //given
            var feedback = {
                title: 'test',
                mail: 'test',
                severity: 'test',
                type: 'test',
                description: 'test',
            };
            var ctrl = createController();
            ctrl.feedbackForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
            ctrl.feedback = feedback;

            //given
            ctrl.sendFeedback();

            //then
            expect(FeedbackRestService.sendFeedback).toHaveBeenCalledWith(feedback);
        }));

        it('should manage sending flag', function () {
            //given
            var feedback = {
                title: 'test',
                mail: 'test',
                severity: 'test',
                type: 'test',
                description: 'test',
            };
            var ctrl = createController();
            ctrl.feedbackForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
            ctrl.feedback = feedback;
            ctrl.isSendingFeedback = false;

            //given
            ctrl.sendFeedback();
            expect(ctrl.isSendingFeedback).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.isSendingFeedback).toBe(false);
        });

        it('should close feedback modal on send success', function () {
            //given
            var feedback = {
                title: 'test',
                mail: 'test',
                severity: 'test',
                type: 'test',
                description: 'test',
            };
            var ctrl = createController();
            ctrl.feedbackForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
            ctrl.feedback = feedback;
            ctrl.state.feedback.visible = true;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(ctrl.state.feedback.visible).toBe(false);
        });

        it('should show message on send success', inject(function (MessageService) {
            //given
            var feedback = {
                title: 'test',
                mail: 'test',
                severity: 'test',
                type: 'test',
                description: 'test',
            };
            var ctrl = createController();
            ctrl.feedbackForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
            ctrl.feedback = feedback;
            ctrl.state.feedback.visible = true;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('FEEDBACK_SENT_TITLE', 'FEEDBACK_SENT_CONTENT');
        }));

        it('should save user mail', inject(function (StorageService) {
            //given
            var feedback = {
                title: 'test',
                mail: 'test mail',
                severity: 'test',
                type: 'test',
                description: 'test',
            };
            var ctrl = createController();
            ctrl.feedbackForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
            ctrl.feedback = feedback;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(StorageService.saveFeedbackUserMail).toHaveBeenCalledWith('test mail');
        }));

        it('should reset form on send success', function () {
            //given
            var feedback = {
                title: 'test',
                mail: 'test',
                severity: 'test',
                type: 'test',
                description: 'test',
            };
            var ctrl = createController();
            ctrl.feedbackForm = { $commitViewValue: jasmine.createSpy('$commitViewValue').and.returnValue() };
            ctrl.feedback = feedback;
            ctrl.state.feedback.displayFeedback = true;

            //given
            ctrl.sendFeedback();
            scope.$digest();

            //then
            expect(ctrl.feedback).toEqual({
                title: '',
                mail: 'test mail',
                severity: 'MINOR',
                type: 'BUG',
                description: '',
            });
        });
    });
});
