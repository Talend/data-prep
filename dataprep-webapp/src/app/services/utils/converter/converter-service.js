/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:ConverterService
 * @description Converter service. This service help to convert data
 */
export default function ConverterService($translate) {
	'ngInject';

	const i18n = {
		INTEGER: $translate.instant('INTEGER'),
		DECIMAL: $translate.instant('DECIMAL'),
		BOOLEAN: $translate.instant('BOOLEAN'),
		TEXT: $translate.instant('TEXT'),
		DATE: $translate.instant('DATE'),
		UNKNOWN: $translate.instant('UNKNOWN'),
	};

	return {
		isNumber,

		// types
		toInputType,
		simplifyType,
		simplifyTypeLabel,
		adaptValue,
	};

	/**
	 * @ngdoc method
	 * @name isNumber
	 * @methodOf data-prep.services.utils.service:ConverterService
	 * @param {String} value The value to test
	 * @description Check if the entered string is valid number
	 * @return {boolean}
	 */
	function isNumber(value) {
		return /^\s*[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?\s*$/.test(value);
	}

	// --------------------------------------------------------------------------------------------
	// ----------------------------------------------TYPES-----------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name toInputType
	 * @methodOf data-prep.services.utils.service:ConverterService
	 * @param {string} type The type to convert
	 * @description Convert backend type to HTML input type
	 * @returns {string} The converted type
	 */
	function toInputType(type) {
		switch (type) {
		case 'numeric':
		case 'integer':
		case 'double':
		case 'float':
			return 'number';
		case 'boolean':
			return 'checkbox';
		case 'password':
			return 'password';
		default:
			return 'text';
		}
	}

	/**
	 * @ngdoc method
	 * @name simplifyType
	 * @methodOf data-prep.services.utils.service:ConverterService
	 * @param {string} type The type to convert
	 * @description Convert backend type to a simplified, more user friendly, one
	 * @returns {string} The simplified type
	 */
	function simplifyType(type) {
		switch (type.toLowerCase()) {
		case 'numeric':
		case 'integer':
			return 'integer';
		case 'double':
		case 'float':
		case 'decimal':
			return 'decimal';
		case 'boolean':
			return 'boolean';
		case 'string':
		case 'char':
			return 'text';
		case 'date':
			return 'date';
		default:
			return 'unknown';
		}
	}

	/**
	 * @ngdoc method
	 * @name simplifyTypeLabel
	 * @methodOf data-prep.services.utils.service:ConverterService
	 * @param {string} type The type to convert
	 * @description Convert backend type to a simplified, more user friendly with translation
	 * @returns {string} The simplified type
	 */
	function simplifyTypeLabel(type) {
		switch (type.toLowerCase()) {
		case 'numeric':
		case 'integer':
			return i18n.INTEGER;
		case 'double':
		case 'float':
		case 'decimal':
			return i18n.DECIMAL;
		case 'boolean':
			return i18n.BOOLEAN;
		case 'string':
		case 'char':
			return i18n.TEXT;
		case 'date':
			return i18n.DATE;
		default:
			return i18n.UNKNOWN;
		}
	}

	/**
	 * @ngdoc method
	 * @name adaptValue
	 * @methodOf data-prep.services.utils.service:ConverterService
	 * @param {object} type The target type of the given value
	 * @param {object} value The value to adapt
	 * @description [PRIVATE] Adapt the given value to the target type
	 * @returns {Object} The adapted value
	 */
	function adaptValue(type, value) {
		switch (type) {
		case 'numeric':
		case 'integer':
		case 'double':
		case 'float':
			return parseFloat(value) || 0;
		case 'boolean':
			return value === 'true' || value === true;
		default:
			return value;
		}
	}
}
