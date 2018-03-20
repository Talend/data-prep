/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('History service', function () {
    'use strict';

    var firstUndoAction;
    var firstRedoAction;
    var secondUndoAction;
    var secondRedoAction;
    var errorUndoAction;
    var errorRedoAction;

    beforeEach(angular.mock.module('data-prep.services.history'));
    beforeEach(inject(($q) => {
        firstUndoAction = jasmine.createSpy('firstUndoAction');
        firstRedoAction = jasmine.createSpy('firstRedoAction');
        secondUndoAction = jasmine.createSpy('secondUndoAction');
        secondRedoAction = jasmine.createSpy('secondRedoAction');
        errorUndoAction = jasmine.createSpy('errorUndoAction').and.returnValue($q.reject('undo error'));
        errorRedoAction = jasmine.createSpy('errorRedoAction').and.returnValue($q.reject('redo error'));
    }));

    describe('init', () => {
        it('should init "in progress" flags to false', inject((HistoryService) => {
            //then
            expect(HistoryService.redoing).toBe(false);
            expect(HistoryService.undoing).toBe(false);
        }));
    });

    describe('add/remove', () => {
        it('should add action into history', inject((HistoryService) => {
            //given
            expect(HistoryService.canUndo()).toBeFalsy();

            //when
            HistoryService.addAction(firstUndoAction, firstRedoAction);

            //then
            expect(HistoryService.canUndo()).toBeTruthy();
        }));

        it('should clear history', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.addAction(secondUndoAction, secondRedoAction);

            HistoryService.undo();
            $rootScope.$digest();

            expect(HistoryService.canUndo()).toBeTruthy();
            expect(HistoryService.canRedo()).toBeTruthy();

            //when
            HistoryService.clear();

            //then
            expect(HistoryService.canUndo()).toBeFalsy();
            expect(HistoryService.canRedo()).toBeFalsy();
        }));
    });

    describe('undo', () => {
        it('should undo last action', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.addAction(secondUndoAction, secondRedoAction);

            //when
            HistoryService.undo();
            $rootScope.$digest();

            //then
            expect(firstUndoAction).not.toHaveBeenCalled();
            expect(firstRedoAction).not.toHaveBeenCalled();
            expect(secondUndoAction).toHaveBeenCalled();
            expect(secondRedoAction).not.toHaveBeenCalled();
        }));

        it('should undo all actions', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.addAction(secondUndoAction, secondRedoAction);

            //when
            HistoryService.undo();
            $rootScope.$digest();
            HistoryService.undo();
            $rootScope.$digest();

            //then
            expect(firstUndoAction).toHaveBeenCalled();
            expect(firstRedoAction).not.toHaveBeenCalled();
            expect(secondUndoAction).toHaveBeenCalled();
            expect(secondRedoAction).not.toHaveBeenCalled();
        }));

        it('should push back undo action on error', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(errorUndoAction, errorRedoAction);
            expect(HistoryService.canUndo()).toBeTruthy();
            expect(HistoryService.canRedo()).toBeFalsy();

            //when
            HistoryService.undo();
            $rootScope.$digest();

            //then
            expect(HistoryService.canUndo()).toBeTruthy();
            expect(HistoryService.canRedo()).toBeFalsy();
        }));

        it('should manage undoing flag', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);

            //when
            HistoryService.undo();
            expect(HistoryService.undoing).toBeTruthy();
            $rootScope.$digest();

            //then
            expect(HistoryService.undoing).toBeFalsy();
        }));

        it('should do nothing when undo list is empty', inject(($rootScope, HistoryService) => {
            //when
            HistoryService.undo();

            //then
            expect(HistoryService.undoing).toBeFalsy();
        }));

        it('should do nothing when already performing undo', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.undoing = true;

            //when
            HistoryService.undo();
            $rootScope.$digest();

            //then
            expect(firstUndoAction).not.toHaveBeenCalled();
        }));

        it('should do nothing when already performing redo', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.redoing = true;

            //when
            HistoryService.undo();
            $rootScope.$digest();

            //then
            expect(firstUndoAction).not.toHaveBeenCalled();
        }));
    });

    describe('redo', () => {
        it('should redo last undone action', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.addAction(secondUndoAction, secondRedoAction);

            HistoryService.undo();
            $rootScope.$digest();

            //when
            HistoryService.redo();
            $rootScope.$digest();

            //then
            expect(firstUndoAction).not.toHaveBeenCalled();
            expect(firstRedoAction).not.toHaveBeenCalled();
            expect(secondUndoAction).toHaveBeenCalled();
            expect(secondRedoAction).toHaveBeenCalled();
        }));

        it('should redo all actions', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);
            HistoryService.addAction(secondUndoAction, secondRedoAction);

            HistoryService.undo();
            $rootScope.$digest();
            HistoryService.undo();
            $rootScope.$digest();

            //when
            HistoryService.redo();
            $rootScope.$digest();
            HistoryService.redo();
            $rootScope.$digest();

            //then
            expect(firstUndoAction).toHaveBeenCalled();
            expect(firstRedoAction).toHaveBeenCalled();
            expect(secondUndoAction).toHaveBeenCalled();
            expect(secondRedoAction).toHaveBeenCalled();
        }));

        it('should push back redo action on error', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, errorRedoAction);
            HistoryService.undo();
            $rootScope.$digest();

            expect(HistoryService.canUndo()).toBeFalsy();
            expect(HistoryService.canRedo()).toBeTruthy();

            //when
            HistoryService.redo();
            $rootScope.$digest();

            //then
            expect(HistoryService.canUndo()).toBeFalsy();
            expect(HistoryService.canRedo()).toBeTruthy();
        }));

        it('should manage redoing flag', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);

            HistoryService.undo();
            $rootScope.$digest();

            //when
            HistoryService.redo();
            expect(HistoryService.redoing).toBeTruthy();
            $rootScope.$digest();

            //then
            expect(HistoryService.redoing).toBeFalsy();
        }));

        it('should do nothing when redo list is empty', inject(($rootScope, HistoryService) => {
            //when
            HistoryService.redo();

            //then
            expect(HistoryService.redoing).toBeFalsy();
        }));

        it('should do nothing when already performing undo', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);

            HistoryService.undo();
            $rootScope.$digest();

            HistoryService.undoing = true;

            //when
            HistoryService.redo();
            $rootScope.$digest();

            //then
            expect(firstRedoAction).not.toHaveBeenCalled();
        }));

        it('should do nothing when already performing redo', inject(($rootScope, HistoryService) => {
            //given
            HistoryService.addAction(firstUndoAction, firstRedoAction);

            HistoryService.undo();
            $rootScope.$digest();

            HistoryService.redoing = true;

            //when
            HistoryService.redo();
            $rootScope.$digest();

            //then
            expect(firstRedoAction).not.toHaveBeenCalled();
        }));
    });
});
