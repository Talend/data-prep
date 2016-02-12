/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.history-control.directive:HistoryControl
 * @description This directive add the buttons and shortcuts to control the history actions
 * @restrict E
 */
export default function HistoryControl($document, HistoryService) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: 'app/components/history-control/history-control.html',
        controller: function () {
            this.service = HistoryService;
        },
        controllerAs: 'historyCtrl',
        link: function () {
            //attach Ctrl+Z and Ctrl+Y event
            $document.on('keydown', function (evtobj) {

                //CTRL+Z
                if (evtobj.keyCode === 90 && evtobj.ctrlKey) {
                    HistoryService.undo();
                }
                //Ctrl+Y
                else if (evtobj.keyCode === 89 && evtobj.ctrlKey) {
                    HistoryService.redo();
                }
            });
        }
    };
}