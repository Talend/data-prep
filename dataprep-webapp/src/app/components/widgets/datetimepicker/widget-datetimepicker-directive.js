/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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
 * @usage
 <talend-datetime-picker
 ng-model="ctrl.value"
 on-select="ctrl.onSelect"
 on-blur="ctrl.onBlur"
 is-date-time></talend-datetimepicker>
 * @param {string}   value Variable to bind input ngModel
 * @param {function} onSelect Event handler when date is picked
 * @param {function} onBlur Event handler when input is blurred
 * @param {string} datetimepickerStyle Component style
 */
export default function TalendDatetimePicker($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		template: `
            <input type="text"
                   class="datetimepicker"
                   ng-model="ctrl.value"
                   ng-blur="ctrl.onBlur()" />
        `,
		scope: {
			value: '=ngModel',
			onSelect: '&',
			onBlur: '&',
			datetimepickerStyle: '@',
		},
		bindToController: true,
		controller: () => {
		},
		controllerAs: 'ctrl',
		link(scope, iElement, iAttrs, ctrl) {
			// datetimepicker uses unix-like date format
			// @see http://www.xaprb.com/media/2005/12/date-formatting-demo
			const timepicker = _.has(iAttrs, 'isDateTime');
			const formatTime = iAttrs.unixFormatTime || 'H:i';
			const formatDate = iAttrs.unixFormatDate || 'Y-m-d';
			const format = iAttrs.unixFormat || timepicker ? `${formatDate} ${formatTime}` : formatDate;
			const style = ctrl.datetimepickerStyle;

			const $input = iElement.find('.datetimepicker');

			function onSelectDate() {
				$timeout(() => {
					ctrl.onSelect();
				});
			}

			$input.datetimepicker({
				format,
				formatDate,
				formatTime,
				timepicker,
				style,
				lazyInit: true,
				onSelectDate,
				onShow() {
					const $this = $(this);
					const $thisHeight = $this.outerHeight();
					const $inputHeight = $input.outerHeight();
					const $inputOffset = $input.offset();
					const $thisNewTop = $inputHeight + $thisHeight;
					// datetimepicker could be on top of input
					if ($inputOffset && $inputOffset.top >= $thisNewTop) {
						$this.css('margin-top', `-${$thisNewTop}px`);
					}
					else {
						$this.css('margin-top', 0);
					}
				},
			});

			$input.bind('keydown', (event) => {
				// hide calendar on 'ESC' keydown
				if (event.keyCode === 27) {
					$input.datetimepicker('hide');
					event.stopPropagation();
				}
			});

			scope.$on('$destroy', () => {
				$input.datetimepicker('destroy');
			});
		},
	};
}
