/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import { chain } from 'lodash';

const MIN_QUALITY_WIDTH = 10;

/**
 * @ngdoc controller
 * @name talend.widget.controller:QualityBarCtrl
 * @description Quality bar controller
 */
export default class QualityBarCtrl {

	constructor() {
		this.hashQuality = this.hashQuality.bind(this);
	}

    /**
     * @ngdoc method
     * @name getMinimalPercent
     * @methodOf talend.widget.controller:QualityBarCtrl
     * @param {string} type The bar type
     * @description [PRIVATE] Return the adapted width to have a min value if the real value is greater than 0
     */
	getMinimalPercent(type) {
		if (this.quality[type] <= 0) {
			return 0;
		}

		const percent = this.percent[type];
		if (percent < MIN_QUALITY_WIDTH) {
			return MIN_QUALITY_WIDTH;
		}

		return percent;
	}

    /**
     * @ngdoc method
     * @name reduce
     * @methodOf talend.widget.controller:QualityBarCtrl
     * @param {object} widthObject Object containing the 3 bars width
     * @description [PRIVATE] Return the modifiable object keys sorted by object value desc.
     * An entry is modifiable if the value is greater than the minimum width
     */
	getOrderedModifiableKeys(widthObject) {
		return chain(Object.keys(widthObject))
            // filter : only keep values > min width.
            // those with min width are not reducable
            .filter(function (key) {
	return widthObject[key] > MIN_QUALITY_WIDTH;
})
            // sort by width value in reverse order
            .sortBy(function (key) {
	return widthObject[key];
})
            .reverse()
            .value();
	}

    /**
     * @ngdoc method
     * @name reduce
     * @methodOf talend.widget.controller:QualityBarCtrl
     * @param {object} widthObject Object containing the 3 bars width
     * @param {number} amount The amount to remove from the bars
     * @description [PRIVATE] Reduce the bars width to fit 100%. The amount value is removed.
     */
	reduce(widthObject, amount) {
		if (amount <= 0) {
			return;
		}

		const orderedKeys = this.getOrderedModifiableKeys(widthObject);
		if (amount <= 2) {
			widthObject[orderedKeys[0]] -= amount;
			return;
		}

		const bigAmountKey = orderedKeys[0];
		const smallAmountKey = orderedKeys.length > 1 ? orderedKeys[1] : orderedKeys[0];
		widthObject[bigAmountKey] -= 2;
		widthObject[smallAmountKey] -= 1;

		this.reduce(widthObject, amount - 3);
	}

    /**
     * @ngdoc method
     * @name computeWidth
     * @methodOf talend.widget.controller:QualityBarCtrl
     * @description [PRIVATE] Compute quality bars width
     * WARNING : the percentages must be computed before this function call
     */
	computeQualityWidth() {
		const widthObject = {
			invalid: this.getMinimalPercent('invalid'),
			empty: this.getMinimalPercent('empty'),
			valid: this.getMinimalPercent('valid'),
		};

		widthObject.isVariableInvalid = (widthObject.invalid > MIN_QUALITY_WIDTH);
		widthObject.isVariableEmpty = (widthObject.empty > MIN_QUALITY_WIDTH);
		widthObject.isVariableValid = (widthObject.valid > MIN_QUALITY_WIDTH);

		const diff = (widthObject.invalid + widthObject.empty + widthObject.valid) - 100;
		if (diff > 0) {
			this.reduce(widthObject, diff);
		}

		this.width = widthObject;
	}

    /**
     * @ngdoc method
     * @name computePercent
     * @methodOf talend.widget.controller:QualityBarCtrl
     * @description [PRIVATE] Compute quality bars percentage
     */
	computePercent() {
		const total = this.quality.empty + this.quality.invalid + this.quality.valid;

		this.percent = {
			invalid: this.quality.invalid <= 0 ? 0 : Math.round((this.quality.invalid * 100) / total),
			empty: this.quality.empty <= 0 ? 0 : Math.round((this.quality.empty * 100) / total),
			valid: this.quality.valid <= 0 ? 0 : Math.round((this.quality.valid * 100) / total),
		};
	}

    /**
     * @ngdoc method
     * @name hashQuality
     * @methodOf talend.widget.controller:QualityBarCtrl
     * @description [PRIVATE] Calculate a simple hash from concatenating values
     */
	hashQuality() {
		return this.quality.empty + '' + this.quality.invalid + '' + this.quality.valid;
	}
}
