/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.history.service:HistoryService
 * @description History service. This service expose functions to manage actions in history
 */
export default function HistoryService($q) {
    'ngInject';

    /**
     * @ngdoc property
     * @name history
     * @propertyOf data-prep.services.history.service:HistoryService
     * @description The history actions. These actions can be canceled by the undo
     */
    var history = [];
    /**
     * @ngdoc property
     * @name undoneHistory
     * @propertyOf data-prep.services.history.service:HistoryService
     * @description The history undone actions. These actions can be reexecuted by the redo.
     */
    var undoneHistory = [];

    var service = {
        undoing: false,
        redoing: false,

        addAction: addAction,
        clear: clear,

        canUndo: canUndo,
        undo: undo,

        canRedo: canRedo,
        redo: redo
    };
    return service;

    //----------------------------------------------------------------------------------------------------
    //-----------------------------------------------ADD HISTORY------------------------------------------
    //----------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name addAction
     * @methodOf data-prep.services.history.service:HistoryService
     * @param {function} undoAction Undo action to execute to cancel the action
     * @param {function} redoAction Redo action to execute to execute the action again (if undone)
     * @description Create an entry un the history list and flush the undoneHistory list
     */
    function addAction(undoAction, redoAction) {
        history.push({
            undo: undoAction,
            redo: redoAction
        });
        undoneHistory = [];
    }

    /**
     * @ngdoc method
     * @name clear
     * @methodOf data-prep.services.history.service:HistoryService
     * @description Reset the history
     */
    function clear() {
        history = [];
        undoneHistory = [];
    }

    //----------------------------------------------------------------------------------------------------
    //---------------------------------------------------UNDO---------------------------------------------
    //----------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name canUndo
     * @methodOf data-prep.services.history.service:HistoryService
     * @description Test if an undo action can be executed
     * @returns {number} The number of action in history (Truthy if we can undo any action)
     */
    function canUndo() {
        return history.length;
    }

    /**
     * @ngdoc method
     * @name undo
     * @methodOf data-prep.services.history.service:HistoryService
     * @description Perform the last action cancelation and push it to the undone list
     */
    function undo() {
        if (!canUndo()) {
            return;
        }

        service.undoing = true;
        var action = history.pop();
        $q.when()
            .then(function () {
                return action.undo();
            })
            .then(function () {
                undoneHistory.unshift(action);
            })
            .catch(function () {
                history.push(action);
            })
            .finally(function () {
                service.undoing = false;
            });
    }

    //----------------------------------------------------------------------------------------------------
    //---------------------------------------------------REDO---------------------------------------------
    //----------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name canRedo
     * @methodOf data-prep.services.history.service:HistoryService
     * @description Test if a redo action can be executed
     * @returns {number} The number of action in undone history (Truthy if we can redo any action)
     */
    function canRedo() {
        return undoneHistory.length;
    }

    /**
     * @ngdoc method
     * @name redo
     * @methodOf data-prep.services.history.service:HistoryService
     * @description Perform the last undone action again and push it to the history list
     */
    function redo() {
        if (!canRedo()) {
            return;
        }

        service.redoing = true;
        var action = undoneHistory.shift();
        $q.when()
            .then(function () {
                return action.redo();
            })
            .then(function () {
                history.push(action);
            })
            .catch(function () {
                undoneHistory.unshift(action);
            })
            .finally(function () {
                service.redoing = false;
            });
    }
}