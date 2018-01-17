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
 * @name data-prep.validation.directive:CanBeBlankValidation
 * @description This directive perform a validation on the input value if needed.
 * @restrict E
 * @usage <input ... can-be-blank="false" />
 * @param {string} canBeBlank 'false' = should check for non blank value. Any other value (including empty) is considered as 'true'
 */
export default function CanBeBlankValidation() {
	return {
		require: 'ngModel',
		link(scope, elm, attrs, ctrl) {
			ctrl.$validators.canBeBlankValidation = function (modelValue) {
				const mandatory = attrs.canBeBlank === 'false';

                // not mandatory OR (value not null AND not blank)
				return !mandatory || (modelValue !== null && !!('' + modelValue).trim());
			};
		},
	};
}
