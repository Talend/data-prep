describe('Navbar controller', function () {
    'use strict';

    var createController, scope, $stateMock;

    beforeEach(module('data-prep.navbar'));

    beforeEach(inject(function ($rootScope, $controller, $q, DatasetService, OnboardingService) {
        jasmine.clock().install();
        scope = $rootScope.$new();
        $stateMock = {};

        createController = function () {
            return $controller('NavbarCtrl', {
                $scope: scope,
                $state: $stateMock
            });
        };

        spyOn(DatasetService, 'getDatasets').and.returnValue($q.when());
        spyOn(OnboardingService, 'startTour').and.returnValue();
    }));

    afterEach(function() {
        jasmine.clock().uninstall();
    });

    describe('onboarding not completed yet', function() {
        beforeEach(inject(function(OnboardingService) {
            spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);
        }));

        it('should start tour on dataset page', inject(function ($timeout, DatasetService, OnboardingService) {
            //given
            $stateMock.params = {};
            $stateMock.current = {name: 'nav.home.datasets'};

            //when
            createController();
            scope.$digest();

            //then
            expect(DatasetService.getDatasets).toHaveBeenCalled();
            expect(OnboardingService.startTour).not.toHaveBeenCalled();

            //when
            jasmine.clock().tick(100);

            //then
            expect(OnboardingService.startTour).toHaveBeenCalledWith('dataset');
        }));

        it('should not start tour on dataset playground page', inject(function (DatasetService, OnboardingService) {
            //given
            $stateMock.params = {datasetid: '154645'};
            $stateMock.current = {name: 'nav.home.datasets'};

            //when
            createController();
            scope.$digest();
            jasmine.clock().tick(100);

            //then
            expect(DatasetService.getDatasets).not.toHaveBeenCalled();
            expect(OnboardingService.startTour).not.toHaveBeenCalled();
        }));

        it('should not start tour on other than dataset page', inject(function (DatasetService, OnboardingService) {
            //given
            $stateMock.params = {};
            $stateMock.current = {name: 'nav.home.other'};

            //when
            createController();
            scope.$digest();
            jasmine.clock().tick(100);

            //then
            expect(DatasetService.getDatasets).not.toHaveBeenCalled();
            expect(OnboardingService.startTour).not.toHaveBeenCalled();
        }));
    });

    describe('onboarding completed yet', function() {
        beforeEach(inject(function(OnboardingService) {
            spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false);
        }));

        it('should not start tour on dataset page', inject(function ($timeout, DatasetService, OnboardingService) {
            //given
            $stateMock.params = {};
            $stateMock.current = {name: 'nav.home.datasets'};

            //when
            createController();
            scope.$digest();
            jasmine.clock().tick(100);

            //then
            expect(DatasetService.getDatasets).not.toHaveBeenCalled();
            expect(OnboardingService.startTour).not.toHaveBeenCalled();
        }));
    });

    describe('feedback ', function() {
        beforeEach(inject(function (StateService) {
            spyOn(StateService, 'showFeedback').and.returnValue();
        }));

        it('should open feedback modal', inject(function (StateService) {
            //given
            $stateMock.params = {};
            $stateMock.current = {name: 'nav.home.datasets'};
            var ctrl = createController();

            //given
            ctrl.openFeedbackForm();

            //then
            expect(StateService.showFeedback).toHaveBeenCalled();
        }));

    });


    describe('search ', function() {
        beforeEach(inject(function (EasterEggsService) {
            spyOn(EasterEggsService, 'enableEasterEgg').and.returnValue();
        }));

        it('should call the easter eggs service', inject(function (EasterEggsService) {
            //given
            $stateMock.params = {};
            $stateMock.current = {name: 'nav.home.datasets'};
            var ctrl = createController();

            //when
            ctrl.searchInput = 'barcelona';
            ctrl.search();

            //then
            expect(EasterEggsService.enableEasterEgg).toHaveBeenCalledWith('barcelona');
        }));

    });
});
