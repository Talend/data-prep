/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export default function FilterMonitor() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/filter/monitor/filter-monitor.html',
        scope: {
            filters: '=',
            onReset: '&',
            nbLines: '=',
            nbTotalLines: '=',
            percentage: '='
        },
        bindToController: true,
        controller: () => {},
        controllerAs: 'filterMonitorCtrl'
    };
}