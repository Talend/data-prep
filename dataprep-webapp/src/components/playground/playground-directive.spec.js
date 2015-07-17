describe('Playground directive', function () {
    'use strict';

    var scope, createElement, element;

    var metadata = {
        'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        'name': 'US States',
        'author': 'anonymousUser',
        'created': '02-03-2015 14:52',
        records: '3'
    };

    beforeEach(module('data-prep.playground'));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'FILE_DETAILS': 'File: {{name}} ({{records}} lines)'
        });
        $translateProvider.preferredLanguage('en');
    }));


    beforeEach(inject(function ($state, $rootScope, $compile, $q, $timeout, PreparationService, PlaygroundService, ExportService) {
        scope = $rootScope.$new();

        createElement = function () {
            element = angular.element('<playground></playground>');
            angular.element('body').append(element);

            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };

        spyOn(PreparationService, 'refreshPreparations').and.returnValue();
        spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(true));
        spyOn($state, 'go').and.returnValue();
        spyOn(ExportService, 'refreshTypes').and.returnValue($q.when([]));
        spyOn(ExportService, 'getParameters').and.returnValue({});

    }));

    afterEach(inject(function ($stateParams) {
        scope.$destroy();
        element.remove();

        $stateParams.prepid = null;
        $stateParams.datasetid = null;
    }));

    it('should render playground elements', inject(function (PlaygroundService) {
        //given
        PlaygroundService.currentMetadata = metadata;

        //when
        createElement();

        //then
        var playground = angular.element('body').find('.playground').eq(0);
        var playgroundModal = playground.parent();

        //check header is present and contains description and search filter
        expect(playgroundModal.find('.modal-header').length).toBe(1);
        expect(playgroundModal.find('.modal-header').eq(0).find('li').eq(0).text().trim()).toBe('File: US States (3 lines)');

        //check left slidable is hidden recipe with left slide action
        expect(playground.eq(0).find('.slidable').eq(0).hasClass('recipe')).toBe(true);
        expect(playground.eq(0).find('.slidable').eq(0).hasClass('slide-hide')).toBe(true);
        expect(playground.eq(0).find('.slidable').eq(0).find('.action').eq(0).hasClass('right')).toBe(false);

        //check right slidable is displayed transformations with right slide action
        expect(playground.eq(0).find('.slidable').eq(1).hasClass('suggestions')).toBe(true);
        expect(playground.eq(0).find('.slidable').eq(1).hasClass('slide-hide')).toBe(false);
        expect(playground.eq(0).find('.slidable').eq(1).find('.action').eq(0).hasClass('right')).toBe(true);

        //check datagrid and filters are present
        expect(playground.eq(0).find('.filter-list').length).toBe(1);
        expect(playground.eq(0).find('.filter-list').find('.filter-search').length).toBe(1);
        expect(playground.eq(0).find('datagrid').length).toBe(1);
    }));

    describe('recipe header', function () {
        it('should show/hide action buttons in the recipe header', inject(function (PlaygroundService) {
            //given
            PlaygroundService.currentMetadata = metadata;
            createElement();

            var stepsHeader = angular.element('body > talend-modal').find('.steps-header').eq(0);
            var nonEditionSection = stepsHeader.find('div[ng-show="!playgroundCtrl.editionMode"]').eq(0);
            var editionSection = stepsHeader.find('div[ng-show="playgroundCtrl.editionMode"]').eq(0);

            var confirmBtn = stepsHeader.find('a.check-btn').eq(0);
            var editionBtn = stepsHeader.find('a.edit-btn').eq(0);

            expect(nonEditionSection.is(':visible')).toBe(false);
            expect(editionSection.is(':visible')).toBe(true);

            element.controller('playground').preparationName = 'My Preparation';

            //when
            var event = new angular.element.Event('click');
            confirmBtn.trigger(event);

            //then
            expect(nonEditionSection.is(':visible')).toBe(true);
            expect(editionSection.is(':visible')).toBe(false);

            //when
            var event2 = new angular.element.Event('click');
            editionBtn.trigger(event2);

            //then
            expect(nonEditionSection.is(':visible')).toBe(false);
            expect(editionSection.is(':visible')).toBe(true);

        }));

        it('should toggle recipe on click on the On/Off switch', inject(function (RecipeBulletService) {
            //given
            spyOn(RecipeBulletService, 'toggleRecipe').and.returnValue();

            createElement();
            var chkboxOnOff = angular.element('body').find('.label-switch > input[type="checkbox"]');

            //when
            chkboxOnOff.trigger('click');

            //then
            expect(RecipeBulletService.toggleRecipe).toHaveBeenCalled();
        }));

        it('should switch OFF the On/Off switch when the 1st step is INACTIVE', inject(function ($rootScope, PlaygroundService, RecipeService) {
            //given
            PlaygroundService.currentMetadata = metadata;
            var step = {
                inactive: false,
                transformation: {
                    stepId: '92771a304130e9',
                    name: 'propercase',
                    parameters: [],
                    items: [],
                    dynamic: false
                }
            };
            RecipeService.getRecipe().push(step);
            createElement();

            var chkboxOnOff = angular.element('body').find('.label-switch > input[type="checkbox"]');
            expect(chkboxOnOff.prop('checked')).toBe(true);

            //when
            step.inactive = true;
            $rootScope.$digest();

            //then
            expect(chkboxOnOff.prop('checked')).toBe(false);
        }));

        it('should switch ON the On/Off switch when the 1st step is ACTIVE', inject(function ($rootScope, PlaygroundService, RecipeService) {
            //given
            PlaygroundService.currentMetadata = metadata;
            var step = {
                inactive: true,
                transformation: {
                    stepId: '92771a304130e9',
                    name: 'propercase',
                    parameters: [],
                    items: [],
                    dynamic: false
                }
            };
            RecipeService.getRecipe().push(step);
            createElement();

            var chkboxOnOff = angular.element('body').find('.label-switch > input[type="checkbox"]');
            expect(chkboxOnOff.prop('checked')).toBe(false);

            //when
            step.inactive = false;
            $rootScope.$digest();

            //then
            expect(chkboxOnOff.prop('checked')).toBe(true);
        }));

        it('should confirm preparation name edition on ENTER keydown', inject(function (PlaygroundService) {
            //given
            PlaygroundService.currentMetadata = metadata;
            PlaygroundService.preparationName = 'PrepName';

            createElement();
            var ctrl = element.controller('playground');
            spyOn(ctrl, 'confirmPrepNameEdition').and.returnValue();

            var event = new angular.element.Event('keydown', {keyCode: 13});

            //when
            var input = angular.element('body > talend-modal .steps-header').find('input#prepNameInput');
            input.trigger(event);

            //then
            expect(ctrl.confirmPrepNameEdition).toHaveBeenCalled();
        }));

        it('should cancel preparation name edition on ENTER keydown', inject(function ($timeout, PlaygroundService) {
            //given
            PlaygroundService.currentMetadata = metadata;
            PlaygroundService.preparationName = 'PrepName';

            createElement();
            var ctrl = element.controller('playground');
            spyOn(ctrl, 'cancelPrepNameEdition').and.returnValue();

            var event = new angular.element.Event('keydown', {keyCode: 27});

            //when
            var input = angular.element('body > talend-modal .steps-header').find('input#prepNameInput');
            input.trigger(event);
            $timeout.flush();

            //then
            expect(ctrl.cancelPrepNameEdition).toHaveBeenCalled();
        }));

        it('should do nothing special on keydown other than ENTER/ESC', inject(function ($timeout, PlaygroundService) {
            //given
            PlaygroundService.currentMetadata = metadata;
            PlaygroundService.preparationName = 'PrepName';

            createElement();
            var ctrl = element.controller('playground');
            spyOn(ctrl, 'confirmPrepNameEdition').and.returnValue();
            spyOn(ctrl, 'cancelPrepNameEdition').and.returnValue();

            var event = new angular.element.Event('keydown', {keyCode: 19});

            //when
            var input = angular.element('body > talend-modal .steps-header').find('input#prepNameInput');
            input.trigger(event);

            //then
            expect(ctrl.confirmPrepNameEdition).not.toHaveBeenCalled();
            expect(ctrl.cancelPrepNameEdition).not.toHaveBeenCalled();
        }));
    });

    describe('hide playground', function () {
        beforeEach(inject(function (PlaygroundService, PreparationService) {
            PlaygroundService.currentMetadata = metadata;
            createElement();

            PlaygroundService.show();
            scope.$apply();
            expect(PreparationService.refreshPreparations).not.toHaveBeenCalled();
        }));

        it('should change route to preparations list on preparation playground hide', inject(function ($state, $stateParams, PlaygroundService) {
            //given: simulate playground route with preparation id
            $stateParams.prepid = '1234';

            //when
            PlaygroundService.hide();
            scope.$apply();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.home.preparations', {prepid: null});
        }));

        it('should change route to datasets list on dataset playground hide', inject(function ($state, $stateParams, PlaygroundService) {
            //given: simulate playground route with preparation id
            $stateParams.datasetid = '1234';

            //when
            PlaygroundService.hide();
            scope.$apply();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.home.datasets', {datasetid: null});
        }));

        it('should do nothing if playground is not routed', inject(function ($state, $stateParams, PlaygroundService, PreparationService) {
            //given: simulate no preparation id in route
            $stateParams.prepid = null;
            $stateParams.datasetid = null;

            //when
            PlaygroundService.hide();
            scope.$apply();

            //then
            expect(PreparationService.refreshPreparations).toHaveBeenCalled();
            expect($state.go).not.toHaveBeenCalled();
        }));

        it('should refresh preparations on playground hide', inject(function (PlaygroundService, PreparationService) {
            //when
            PlaygroundService.hide();
            scope.$apply();

            //then
            expect(PreparationService.refreshPreparations).toHaveBeenCalled();
        }));
    });

});