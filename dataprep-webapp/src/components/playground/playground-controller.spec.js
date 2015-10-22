describe('Playground controller', function() {
    'use strict';

    var createController, scope, stateMock;

    beforeEach(module('data-prep.playground', function($provide) {
        stateMock = {playground: {}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function($rootScope, $q, $controller, $state, PlaygroundService) {
        scope = $rootScope.$new();

        createController = function() {
            return $controller('PlaygroundCtrl', {
                $scope: scope
            });
        };

        spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(true));
        spyOn($state, 'go').and.returnValue();

    }));

    describe('bindings', function() {
        it('should bind preparationName getter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(ctrl.preparationName).toBeFalsy();

            //when
            PlaygroundService.preparationName = 'My preparation';

            //then
            expect(ctrl.preparationName).toBe('My preparation');
        }));

        it('should bind preparationName setter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(PlaygroundService.preparationName).toBeFalsy();

            //when
            ctrl.preparationName = 'My preparation';

            //then
            expect(PlaygroundService.preparationName).toBe('My preparation');
        }));

        it('should bind previewInProgress getter with PreviewService', inject(function(PreviewService) {
            //given
            var ctrl = createController();
            expect(ctrl.previewInProgress).toBeFalsy();

            //when
            spyOn(PreviewService, 'previewInProgress').and.returnValue(true);

            //then
            expect(ctrl.previewInProgress).toBe(true);
        }));
    });

    describe('recipe header', function() {

        it('should create/update preparation with clean name on name edition confirmation', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            ctrl.preparationName = 'my old name';

            //when
            ctrl.confirmPrepNameEdition('  my new name  ');

            //then
            expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my new name');
        }));

        it('should change route to preparation route on name edition confirmation', inject(function($rootScope, $state) {
            //given
            var ctrl = createController();
            ctrl.preparationName = 'My old preparation ';
            stateMock.playground.preparation = {id: 'fe6843da512545e'};

            //when
            ctrl.confirmPrepNameEdition('My preparation ');
            $rootScope.$digest();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.home.preparations', {prepid : 'fe6843da512545e'}, {location:'replace', inherit:false});
        }));

        it('should not call service create/updateName service if name is blank on name edition confirmation', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();

            //when
            ctrl.confirmPrepNameEdition(' ');

            //then
            expect(PlaygroundService.createOrUpdatePreparation).not.toHaveBeenCalled();
        }));
    });

    describe('implicit preparation', function() {
        var ctrl;
        var preparation;

        beforeEach(inject(function($q, PreparationService, StateService) {
            preparation = {id: '9af874865e42b546', draft: true};
            stateMock.playground.preparation = preparation;

            spyOn(PreparationService, 'delete').and.returnValue($q.when(true));
            spyOn(StateService, 'hidePlayground').and.returnValue();

            ctrl = createController();
        }));

        it('should return true (allow playground close) with NOT implicit preparation', function() {
            //given
            preparation.draft = false;

            //when
            var result = ctrl.beforeClose();

            //then
            expect(result).toBe(true);
        });

        it('should return false (block playground close) with implicit preparation', function() {
            //when
            var result = ctrl.beforeClose();

            //then
            expect(result).toBe(false);
        });

        it('should show save/discard modal with implicit preparation', function() {
            //given
            expect(ctrl.showNameValidation).toBeFalsy();

            //when
            ctrl.beforeClose();

            //then
            expect(ctrl.showNameValidation).toBe(true);
        });

        it('should delete current preparation on save discard', inject(function(PreparationService) {
            //when
            ctrl.discardSaveOnClose();

            //then
            expect(PreparationService.delete).toHaveBeenCalledWith(preparation);
        }));

        it('should hide save/discard and playground modals on save discard', inject(function(StateService) {
            //given
            ctrl.showNameValidation = true;
            expect(StateService.hidePlayground).not.toHaveBeenCalled();

            //when
            ctrl.discardSaveOnClose();
            scope.$digest();

            //then
            expect(ctrl.showNameValidation).toBe(false);
            expect(StateService.hidePlayground).toHaveBeenCalled();
        }));

        it('should change preparation name on save confirm', inject(function(PlaygroundService) {
            //given
            ctrl.preparationName = '  my preparation ';

            //when
            ctrl.confirmSaveOnClose();

            //then
            expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my preparation');
        }));

        it('should manage saving flag on save confirm', function() {
            //given
            expect(ctrl.saveInProgress).toBeFalsy();

            //when
            ctrl.confirmSaveOnClose();
            expect(ctrl.saveInProgress).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.saveInProgress).toBe(false);
        });

        it('should hide save/discard and playground modals on save confirm', inject(function(StateService) {
            //given
            ctrl.showNameValidation = true;
            expect(StateService.hidePlayground).not.toHaveBeenCalled();

            //when
            ctrl.confirmSaveOnClose();
            scope.$digest();

            //then
            expect(ctrl.showNameValidation).toBe(false);
            expect(StateService.hidePlayground).toHaveBeenCalled();
        }));
    });
});
