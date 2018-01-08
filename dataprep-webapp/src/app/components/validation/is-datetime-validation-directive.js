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
 * @name data-prep.validation.directive:IsDateTimeValidation
 * @description This directive perform a datetime validation on input value modification. you can use format attribute
 * to set a datetime pattern to use otherwise a default is used YYYY-MM-DD HH:mm
 * @restrict E
 * @usage <input ... is-date-time />
 */
export default function IsDateTimeValidation() {
	return {
		require: 'ngModel',
		link(scope, elm, attributes, ctrl) {
			ctrl.$validators.isDateTimeValidation = function (modelValue) {
				if (ctrl.$isEmpty(modelValue)) {
					return false;
				}
				const format = attributes.format || 'YYYY-MM-DD HH:mm';
				return moment(modelValue, format, true).isValid();
			};
		},
	};
}
