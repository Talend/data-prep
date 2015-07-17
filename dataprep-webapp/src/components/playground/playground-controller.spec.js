describe('Playground controller', function() {
    'use strict';

    var createController, scope;

    beforeEach(module('data-prep.playground'));

    beforeEach(inject(function($rootScope, $q, $controller, $state, PlaygroundService) {
        scope = $rootScope.$new();

        createController = function() {
            var ctrl =  $controller('PlaygroundCtrl', {
                $scope: scope
            });
            return ctrl;
        };

        spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(true));
        spyOn($state, 'go').and.returnValue();

    }));

    describe('bindings', function() {
        it('should bind showPlayground getter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(ctrl.showPlayground).toBe(false);

            //when
            PlaygroundService.show();

            //then
            expect(ctrl.showPlayground).toBe(true);
        }));

        it('should bind showPlayground setter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(PlaygroundService.visible).toBe(false);

            //when
            ctrl.showPlayground = true;

            //then
            expect(PlaygroundService.visible).toBe(true);
        }));

        it('should bind editionMode getter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(ctrl.editionMode).toBe(true);

            //when
            PlaygroundService.preparationNameEditionMode = false;

            //then
            expect(ctrl.editionMode).toBe(false);
        }));

        it('should bind editionMode setter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(PlaygroundService.preparationNameEditionMode).toBe(true);

            //when
            ctrl.editionMode = false;

            //then
            expect(PlaygroundService.preparationNameEditionMode).toBe(false);
        }));

        it('should bind metadata getter with PlaygroundService', inject(function(PlaygroundService) {
            //given
            var metadata = {name: 'my dataset'};
            var ctrl = createController();
            expect(ctrl.metadata).toBeFalsy();

            //when
            PlaygroundService.currentMetadata = metadata;

            //then
            expect(ctrl.metadata).toBe(metadata);
        }));

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

        it('should bind showRecipe getter to PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(ctrl.showRecipe).toBeFalsy();

            //when
            PlaygroundService.showRecipe = true;

            //then
            expect(ctrl.showRecipe).toBe(true);
        }));

        it('should bind showRecipe setter to PlaygroundService', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(PlaygroundService.showRecipe).toBeFalsy();

            //when
            ctrl.showRecipe = true;

            //then
            expect(PlaygroundService.showRecipe).toBe(true);
        }));
    });

    describe('recipe header', function() {
        it('should toggle edition mode flag', function() {
            //given
            var ctrl = createController();
            expect(ctrl.editionMode).toBe(true);

            //when
            ctrl.toggleEditionMode();

            //then
            expect(ctrl.editionMode).toBe(false);
        });

        it('should create/update preparation with clean name on name edition confirmation', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();

            ctrl.preparationName = '  my new name  ';

            //when
            ctrl.confirmPrepNameEdition();

            //then
            expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my new name');
        }));

        it('should toggle edition mode flag on name edition confirmation', function() {
            //given
            var ctrl = createController();
            expect(ctrl.editionMode).toBe(true);

            ctrl.preparationName = 'my new name';

            //when
            ctrl.confirmPrepNameEdition();

            //then
            expect(ctrl.editionMode).toBe(false);
        });

        it('should change route to preparation route on name edition confirmation', inject(function($rootScope, $state, PreparationService) {
            //given
            var ctrl = createController();
            ctrl.preparationName = 'My preparation ';
            PreparationService.currentPreparationId = 'fe6843da512545e';

            //when
            ctrl.confirmPrepNameEdition();
            $rootScope.$digest();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.home.preparations', {prepid : 'fe6843da512545e'}, {location:'replace', inherit:false});
        }));

        it('should not call service create/updateName service if name is blank on name edition confirmation', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            ctrl.preparationName = ' ';

            //when
            ctrl.confirmPrepNameEdition();

            //then
            expect(PlaygroundService.createOrUpdatePreparation).not.toHaveBeenCalled();
        }));

        it('should reset name and toggle edition mode flag on name edition cancelation', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(ctrl.editionMode).toBe(true);

            ctrl.preparationName = 'my new name';
            PlaygroundService.originalPreparationName = 'my old name';

            //when
            ctrl.cancelPrepNameEdition();

            //then
            expect(ctrl.preparationName).toBe('my old name');
            expect(ctrl.editionMode).toBe(false);
        }));
    });

    describe('implicit preparation', function() {
        var ctrl;

        beforeEach(inject(function($q, PlaygroundService, PreparationService) {
            PlaygroundService.originalPreparationName = '';
            PreparationService.currentPreparationId = '9af874865e42b546';

            spyOn(PreparationService, 'deleteCurrentPreparation').and.returnValue($q.when(true));

            ctrl = createController();
        }));

        it('should return true (allow playground close) with NOT implicit preparation', inject(function(PlaygroundService) {
            //given
            PlaygroundService.originalPreparationName = 'my preparation';

            //when
            var result = ctrl.beforeClose();

            //then
            expect(result).toBe(true);
        }));

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
            expect(PreparationService.deleteCurrentPreparation).toHaveBeenCalled();
        }));

        it('should hide save/discard and playground modals on save discard', function() {
            //given
            ctrl.showNameValidation = true;
            ctrl.showPlayground = true;

            //when
            ctrl.discardSaveOnClose();
            scope.$digest();

            //then
            expect(ctrl.showNameValidation).toBe(false);
            expect(ctrl.showPlayground).toBe(false);
        });

        it('should change preparation name on save confirm', inject(function(PlaygroundService) {
            //given
            ctrl.preparationName = '  my preparation ';

            //when
            ctrl.confirmSaveOnClose();

            //then
            expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my preparation');
        }));

        it('should toggle edition mode on save confirm', function() {
            //given
            ctrl.editionMode = true;

            //when
            ctrl.confirmSaveOnClose();
            scope.$digest();

            //then
            expect(ctrl.editionMode).toBe(false);
        });

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

        it('should hide save/discard and playground modals on save confirm', function() {
            //given
            ctrl.showNameValidation = true;
            ctrl.showPlayground = true;

            //when
            ctrl.confirmSaveOnClose();
            scope.$digest();

            //then
            expect(ctrl.showNameValidation).toBe(false);
            expect(ctrl.showPlayground).toBe(false);
        });
    });
});
