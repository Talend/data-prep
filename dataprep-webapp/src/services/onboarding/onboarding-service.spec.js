describe('Onboarding service', function() {
    'use strict';

    var TOUR_OPTIONS_KEY = 'org.talend.dataprep.tour_options';

    var introJsMock = {
        setOptions: function() {return this;},
        oncomplete: function() {return this;},
        onexit: function() {return this;},
        start: function() {}
    };

    beforeEach(module('data-prep.services.onboarding'));

    beforeEach(inject(function($window) {
        $window.introJs = function() {
            return introJsMock;
        };

        spyOn(introJsMock, 'setOptions').and.callThrough();
        spyOn(introJsMock, 'oncomplete').and.callThrough();
        spyOn(introJsMock, 'onexit').and.callThrough();
        spyOn(introJsMock, 'start').and.callThrough();
    }));

    afterEach(inject(function($window) {
        $window.localStorage.removeItem(TOUR_OPTIONS_KEY);
    }));

    it('should return true when tour has not been completed yet', inject(function($window, OnboardingService) {
        //given
        $window.localStorage.removeItem(TOUR_OPTIONS_KEY);

        //when
        var result = OnboardingService.shouldStartTour('dataset');

        //then
        expect(result).toBe(true);
    }));

    it('should return false when tour has already been completed', inject(function($window, OnboardingService) {
        //given
        $window.localStorage.setItem(TOUR_OPTIONS_KEY, JSON.stringify({dataset: true}));

        //when
        var result = OnboardingService.shouldStartTour('dataset');

        //then
        expect(result).toBe(false);
    }));

    it('should configure intro.js options', inject(function(OnboardingService) {
        //given

        //when
        OnboardingService.startTour('dataset');

        //then
        expect(introJsMock.setOptions).toHaveBeenCalled();
        var options = introJsMock.setOptions.calls.argsFor(0)[0];
        expect(options.nextLabel).toBe('NEXT');
        expect(options.prevLabel).toBe('BACK');
        expect(options.skipLabel).toBe('SKIP');
        expect(options.doneLabel).toBe('LET ME TRY');
    }));

    it('should create/adapt dataset tour step', inject(function(OnboardingService) {
        //when
        OnboardingService.startTour('dataset');

        //then
        expect(introJsMock.setOptions).toHaveBeenCalled();
        var options = introJsMock.setOptions.calls.argsFor(0)[0];
        expect(options.steps[0]).toEqual({
            element: '.no-js',
            position: 'right',
            intro: '<div class="introjs-tooltiptitle"><center>Welcome to</br>Talend Data Preparation</center></div><div class="introjs-tooltipcontent">To quickly learn how to use it, click <b>Next</b>.</div>'
        });
    }));

    it('should create/adapt playground step', inject(function(OnboardingService) {
        //when
        OnboardingService.startTour('playground');

        //then
        expect(introJsMock.setOptions).toHaveBeenCalled();
        var options = introJsMock.setOptions.calls.argsFor(0)[0];
        expect(options.steps[0]).toEqual({
            element: '.no-js',
            position: 'right',
            intro: '<div class="introjs-tooltiptitle"><center>Welcome to the dataset view</center></div><div class="introjs-tooltipcontent">This grid corresponds to a sample extracted from your data.</br>From this view you can prepare the transformations to apply to your data.</div>'
        });
    }));

    it('should create/adapt column selection', inject(function(OnboardingService) {
        //when
        OnboardingService.startTour('playground');

        //then
        expect(introJsMock.setOptions).toHaveBeenCalled();
        var options = introJsMock.setOptions.calls.argsFor(0)[0];
        expect(options.steps[1]).toEqual({
            element: '#lookup-datagrid .slick-header-columns-right > .slick-header-column',
            position: 'right',
            intro: '<div class="introjs-tooltiptitle"></div><div class="introjs-tooltipcontent">Select a column to discover the transformation actions you can apply to the data.</div>'
        });
    }));

    it('should create/adapt recipe tour step', inject(function(OnboardingService) {
        //when
        OnboardingService.startTour('recipe');

        //then
        expect(introJsMock.setOptions).toHaveBeenCalled();
        var options = introJsMock.setOptions.calls.argsFor(0)[0];
        expect(options.steps[0]).toEqual({
            element: '#help-preparation-name',
            position: 'right',
            intro: '<div class="introjs-tooltiptitle"></div><div class="introjs-tooltipcontent">You can give a name to your brand new preparation.</br>It will be listed in the <b>All preparations</b> view.</div>'
        });
    }));

    it('should save "dataset" state in localstorage on tour complete', inject(function($window, OnboardingService) {
        //given
        $window.localStorage.removeItem(TOUR_OPTIONS_KEY);

        OnboardingService.startTour('dataset');
        expect(introJsMock.oncomplete).toHaveBeenCalled();

        var oncomplete = introJsMock.oncomplete.calls.argsFor(0)[0];

        //when
        oncomplete();

        //then
        var options = JSON.parse($window.localStorage.getItem(TOUR_OPTIONS_KEY));
        expect(options.dataset).toBe(true);
    }));

    it('should save "dataset" state in localstorage on tour exit', inject(function($window, OnboardingService) {
        //given
        $window.localStorage.removeItem(TOUR_OPTIONS_KEY);

        OnboardingService.startTour('dataset');
        expect(introJsMock.onexit).toHaveBeenCalled();

        var onexit = introJsMock.onexit.calls.argsFor(0)[0];

        //when
        onexit();

        //then
        var options = JSON.parse($window.localStorage.getItem(TOUR_OPTIONS_KEY));
        expect(options.dataset).toBe(true);
    }));

    it('should start onboarding', inject(function($window, OnboardingService) {
        //when
        OnboardingService.startTour('dataset');

        //then
        expect(introJsMock.start).toHaveBeenCalled();
    }));
});