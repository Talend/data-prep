/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';

const DATE_FORMAT = 'YYYY-MM-DD';
const D3_DATE_FORMAT = '%Y-%m-%d';
const D3_NUMBER_DECIMAL = ',';
const d3DateFormatter = d3.time.format(D3_DATE_FORMAT);
const d3NumberFormatter = d3.format(D3_NUMBER_DECIMAL);

const numberFormatter = {
	format: value => d3NumberFormatter(value),
};
const dateFormatter = {
	parse: string => d3DateFormatter.parse(string),
	format: timestamp => d3DateFormatter(new Date(timestamp)),
};

/**
 * Checks max interval >= the max of data values and  if the min interval < max interval
 */
function adaptSelection(selection, maxValue) {
	return {
		min: selection.min,
		max: selection.max,
		isMaxReached: selection.max >= maxValue || selection.min >= selection,
	};
}

/**
 * Check if date is correct
 */
function dateIsCorrect(value) {
	return dateFormatter.parse(value);
}

/**
 * Check if number is correct
 */
function numberIsCorrect(value) {
	return /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/.test(value.trim());
}

/**
 * Check if string has comma
 */
function hasComma(value) {
	return value.indexOf(',') > -1;
}

/**
 * Return the number of decimal digit
 */
function getNbDecimalDigit(num) {
	const match = ('' + num).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);
	return Math.max(
        0,
        // Number of digits right of decimal point.
        (match[1] ? match[1].length : 0) -
        // Adjust for scientific notation.
        (match[2] ? +match[2] : 0));
}

/**
 * @ngdoc controller
 * @name talend.widget.controller:RangeSliderCtrl
 * @description The rangeSlider controller
 */
export default class RangeSliderCtrl {
	constructor() {
		this.dateFormat = DATE_FORMAT;
	}

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------UTILS----------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name isDateType
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Check if the range limits are date types
     **/
	isDateType() {
		return this.rangeLimits.type === 'date';
	}

    /**
     * @ngdoc method
     * @name adaptToInputValue
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Adapt values to input format, i.e. strings. The dates are formatted to the default format
     * @param {Object} values The values to adapt
     * @return {Object} The adapted values
     **/
	adaptToInputValue(values) {
		return {
			min: this.isDateType() ? dateFormatter.format(values.min) : '' + values.min,
			max: this.isDateType() ? dateFormatter.format(values.max) : '' + values.max,
		};
	}

    /**
     * @ngdoc method
     * @name adaptFromInputValue
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Adapt values from input to model format (timestamp for dates, number otherwise)
     * @param {Object} values The values to adapt
     * @return {Object} The adapted values
     **/
	adaptFromInputValue(values) {
		return {
			min: this.isDateType() ? +dateFormatter.parse(values.min) : +values.min,
			max: this.isDateType() ? +dateFormatter.parse(values.max) : +values.max,
		};
	}

    /**
     * @ngdoc method
     * @name getLimitsText
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Get the min and max labels
     * @return {Object} The adapted values
     **/
	getLimitsText() {
		const minText = this.isDateType() ?
            dateFormatter.format(this.rangeLimits.min) :
            numberFormatter.format(this.rangeLimits.min);
		const maxText = this.isDateType() ?
            dateFormatter.format(this.rangeLimits.max) :
            numberFormatter.format(this.rangeLimits.max);
		return { minText, maxText };
	}

    /**
     * @ngdoc method
     * @name adaptToInboundValues
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Adapt values to be within [min, max] interval
     * @param {Object} values The values to adapt
     * @return {Object} The adapted values
     **/
	adaptToInboundValues(values) {
		return {
			min: Math.max(this.rangeLimits.min, values.min),
			max: Math.min(this.rangeLimits.max, values.max),
		};
	}

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------MODEL----------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name initModel
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Initialize the model (last brush and input values) and configuration
     **/
	initModel() {
		const minBrush = typeof this.rangeLimits.minBrush !== 'undefined' ? this.rangeLimits.minBrush : this.rangeLimits.min;
		const maxBrush = typeof this.rangeLimits.maxBrush !== 'undefined' ? this.rangeLimits.maxBrush : this.rangeLimits.max;
		const minFilter = typeof this.rangeLimits.minFilterVal !== 'undefined' ? this.rangeLimits.minFilterVal : this.rangeLimits.min;
		const maxFilter = typeof this.rangeLimits.maxFilterVal !== 'undefined' ? this.rangeLimits.maxFilterVal : this.rangeLimits.max;

		this.nbDecimals = Math.max(getNbDecimalDigit(minBrush), getNbDecimalDigit(maxBrush));

		this.lastValues = {
            // the brush values
			brush: {
				min: minBrush,
				max: maxBrush,
			},
            // the input values
			input: {
				min: minFilter,
				max: maxFilter,
			},
		};
	}

    /**
     * @ngdoc method
     * @name setLastBrushValues
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Update last brush values
     * @params {Object} values The new brush values
     **/
	setLastBrushValues(values) {
		this.lastValues.brush.min = values.min;
		this.lastValues.brush.max = values.max;
	}

    /**
     * @ngdoc method
     * @name setLastInputValues
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Update last input values
     * @params {Object} values The new input values
     **/
	setLastInputValues(values) {
		this.lastValues.input.min = values.min;
		this.lastValues.input.max = values.max;
	}

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------BRUSH----------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name updateBrush
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Update current brush values with an animation on transition
     * @params {Object} values The new brush values
     **/
	updateBrush(values) {
		const min = values.min;
		let max = values.max;
		if (min === max) {
			const exp = '1e-' + (this.nbDecimals + 2);
			max += Number(exp);
		}

		this.brushg
            .transition()
            .call(this.brush.extent([min, max]));
	}

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------INPUT----------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name setInputValue
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Update current input values
     * @params {Object} values The new input values
     **/
	setInputValue(values) {
		this.minMaxModel = this.adaptToInputValue(values);
	}

    /**
     * @ngdoc method
     * @name resetInputValues
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Reset current input values to the last registered values
     **/
	resetInputValues() {
		this.hideMsgErr();
		this.setInputValue(this.lastValues.input);
	}

    /**
     * @ngdoc method
     * @name showMsgErr
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Show error messages
     **/
	showMsgErr() {
		this.invalidNumber = true;
		const minMaxStr = this.minMaxModel.min + this.minMaxModel.max;
		this.invalidNumberWithComma = hasComma(minMaxStr);
	}

    /**
     * @ngdoc method
     * @name hideMsgErr
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Hide error messages
     **/
	hideMsgErr() {
		this.invalidNumber = false;
		this.invalidNumberWithComma = false;
	}

    /**
     * @ngdoc method
     * @name inputsAreValid
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Check if inputs values are valid (date and number with wanted format)
     **/
	inputsAreValid() {
		const check = this.isDateType() ? dateIsCorrect : numberIsCorrect;
		return check(this.minMaxModel.min) && check(this.minMaxModel.max);
	}

    /**
     * @ngdoc method
     * @name validateInputs
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Check inputs and show/hide error messages
     **/
	validateInputs() {
		if (this.inputsAreValid()) {
			this.hideMsgErr();
		}
		else {
			this.showMsgErr();
		}
	}

    /**
     * @ngdoc method
     * @name handleKey
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Keydown event on inputs
     * @param {Object} event The key event
     **/
	handleKey(event) {
		switch (event.keyCode) {
		case 13:
			this.onInputChange();
			break;
		case 27:
			this.resetInputValues();
			break;
		}
	}

    // -----------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------PROPAGATION-------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name onChange
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Propagate new values to the parent component
     * @param {Object} values The values to propagate
     **/
	onChange(values) {
		const interval = adaptSelection(values, this.rangeLimits.max);
		this.onBrushEnd({ interval });
	}

    /**
     * @ngdoc method
     * @name onBrushChange
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description When user change brush values with the mouse,
     * we propagate it in the last registered values and to the parent component
     **/
	onBrushChange(values) {
		this.setLastBrushValues(values);
		this.setLastInputValues(values);
		this.onChange(values);
	}

    /**
     * @ngdoc method
     * @name onInputChange
     * @methodOf talend.widget.controller:RangeSliderCtrl
     * @description Whe user validate (blur or enter) an input,
     * we propagate it in the last registered values and to the parent component, and update the brush
     * If the values are invalid, they are reset to the last registered values
     **/
	onInputChange() {
		if (this.inputsAreValid()) {
			const adaptedValue = this.adaptFromInputValue(this.minMaxModel);
			if (adaptedValue.min === this.lastValues.input.min &&
                adaptedValue.max === this.lastValues.input.max) {
				return;
			}

			const adaptedBrushValues = this.adaptToInboundValues(adaptedValue);
			this.updateBrush(adaptedBrushValues);
			this.setLastBrushValues(adaptedBrushValues);
			this.setLastInputValues(adaptedValue);
			this.onChange(adaptedValue);
		}
		else {
			this.resetInputValues();
		}
	}
}
