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
 * @name talend.widget.directive:TalendDatetimePicker
 * @description This directive create a datetimepicker element.
 * @restrict E
 */
export default function TalendDatetimePicker() {

    return {
        restrict: 'E',
        template: '<input class="datetimepicker" type="text" ng-model="ctrl.value" />',
        scope: {
            value: '=ngModel'
        },
        bindToController: true,
        controller: () => {},
        controllerAs: 'ctrl',
        link: function (scope, iElement, iAttrs) {
            
            Date.parseDate = function (input, format) {
                return moment(input, format).toDate();
            };
            Date.prototype.dateFormat = function (format) {
                return moment(this).format(format);
            };

            var format = iAttrs.format ? iAttrs.format : 'DD/MM/YYYY HH:mm:ss';
            var formatTime = iAttrs.formatTime ? iAttrs.formatTime : 'HH:mm:ss';
            var formatDate = iAttrs.formatDate ? iAttrs.formatDate : 'DD/MM/YYYY';

            var input = iElement.find('.datetimepicker');
            input.datetimepicker({
                    format: format,
                    formatTime: formatTime,
                    formatDate: formatDate
                }
            );
            input.bind('keydown', function (event) {
                // hide calendar on 'ESC' keydown
                if (event.keyCode === 27) {
                    input.datetimepicker('hide');
                    event.stopPropagation();
                }
            });
        }
    };

}
