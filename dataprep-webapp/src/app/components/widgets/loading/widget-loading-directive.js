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
 * @name talend.widget.directive:TalendLoading
 * @description This directive create the loading screen. A 120ms delay is set before display, in order to
 * avoid the screen on really fast request for example<br/>
 *
 * Listeners :
 * <ul>
 *     <li>`talend.loading.start` : display the loading screen</li>
 *     <li>`talend.loading.stop` : stop the loading screen</li>
 * </ul>
 * @restrict E
 * @usage <talend-loading></talend-loading>
 */
export default function TalendLoading($rootScope, $timeout) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: 'app/components/widgets/loading/loading.html',
        link: function (scope, iElement) {
            var loadingTimeout;

            var unregisterStartFn = $rootScope.$on('talend.loading.start', function () {
                $timeout.cancel(loadingTimeout);
                iElement[0].className = 'is-loading';

                loadingTimeout = $timeout(function () {
                    iElement[0].className = 'is-loading show-loading';
                }, 200, false);
            });
            var unregisterStopFn = $rootScope.$on('talend.loading.stop', function () {
                $timeout.cancel(loadingTimeout);
                iElement[0].className = '';
            });

            scope.$on('$destroy', function() {
                unregisterStartFn();
                unregisterStopFn();
            });
        }
    };
}