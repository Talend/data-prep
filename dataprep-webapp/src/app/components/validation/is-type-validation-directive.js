/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

const INTEGER_REGEXP = /^\-?\d+$/;
const DOUBLE_REGEXP = /^\-?\d+(\.\d+)?$/;

/**
 * @ngdoc directive
 * @name data-prep.validation.directive:IsTypeValidation
 * @description This directive perform a type validation on input value modification.
 * @restrict E
 * @usage <input ... is-type="integer" />
 * @param {string} isType The wanted type (integer | numeric | double | float)
 */
export default function IsTypeValidation() {
	return {
		require: 'ngModel',
		link: (scope, elm, attrs, ctrl) => {
			const type = attrs.isType;
			ctrl.$validators.isTypeValidation = (modelValue, viewValue) => {
				if (ctrl.$isEmpty(modelValue)) {
					return true;
				}

				switch (type) {
				case 'integer':
					if (!INTEGER_REGEXP.test(viewValue)) {
						return false;
					}

					break;
				case 'numeric':
				case 'double':
				case 'float':
					if (!DOUBLE_REGEXP.test(viewValue)) {
						return false;
					}

					break;
				default:
					break;
				}

				return true;
			};
		},
	};
}
