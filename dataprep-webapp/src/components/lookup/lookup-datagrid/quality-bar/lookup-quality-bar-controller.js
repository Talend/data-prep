(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.quality-bar.controller:QualityBarCtrl
     * @description Quality bar controller
     * @requires data-prep.services.filter.service:FilterService
     * @requires data-prep.services.transformation.service:ColumnSuggestionService
     * @requires data-prep.services.transformation.service:TransformationApplicationService
     */
    function LookupQualityBarCtrl(ColumnSuggestionService) {
        var MIN_QUALITY_WIDTH = 10;
        var vm = this;

        vm.columnSuggestionService = ColumnSuggestionService;

        /**
         * @ngdoc method
         * @name getMinimalPercent
         * @methodOf data-prep.quality-bar.controller:QualityBarCtrl
         * @param {string} type The bar type
         * @description [PRIVATE] Return the adapted width to have a min value if the real value is greater than 0
         */
        var getMinimalPercent = function getMinimalPercent(type) {
            if (vm.quality[type] <= 0) {
                return 0;
            }

            var percent = vm.percent[type];
            if (percent < MIN_QUALITY_WIDTH) {
                return MIN_QUALITY_WIDTH;
            }
            return percent;
        };

        /**
         * @ngdoc method
         * @name reduce
         * @methodOf data-prep.quality-bar.controller:QualityBarCtrl
         * @param {object} widthObject Object containing the 3 bars width
         * @description [PRIVATE] Return the modifiable object keys sorted by object value desc.
         * An entry is modifiable if the value is greater than the minimum width
         */
        var getOrderedModifiableKeys = function getOrderedKeys(widthObject) {
            return _.chain(Object.keys(widthObject))
                //filter : only keep values > min width.
                //those with min width are not reducable
                .filter(function (key) {
                    return widthObject[key] > MIN_QUALITY_WIDTH;
                })
                //sort by width value in reverse order
                .sortBy(function (key) {
                    return widthObject[key];
                })
                .reverse()
                .value();
        };

        /**
         * @ngdoc method
         * @name reduce
         * @methodOf data-prep.quality-bar.controller:QualityBarCtrl
         * @param {object} widthObject Object containing the 3 bars width
         * @param {number} amount The amount to remove from the bars
         * @description [PRIVATE] Reduce the bars width to fit 100%. The amount value is removed.
         */
        var reduce = function reduce(widthObject, amount) {
            if (amount <= 0) {
                return;
            }

            var orderedKeys = getOrderedModifiableKeys(widthObject);
            if (amount <= 2) {
                widthObject[orderedKeys[0]] -= amount;
                return;
            }

            var bigAmountKey = orderedKeys[0];
            var smallAmountKey = orderedKeys.length > 1 ? orderedKeys[1] : orderedKeys[0];
            widthObject[bigAmountKey] -= 2;
            widthObject[smallAmountKey] -= 1;

            reduce(widthObject, amount - 3);
        };

        /**
         * @ngdoc method
         * @name computeWidth
         * @methodOf data-prep.quality-bar.controller:QualityBarCtrl
         * @description [PRIVATE] Compute quality bars width
         * WARNING : the percentages must be computed before this function call
         */
        vm.computeQualityWidth = function computeQualityWidth() {
            var widthObject = {
                invalid: getMinimalPercent('invalid'),
                empty: getMinimalPercent('empty'),
                valid: getMinimalPercent('valid')
            };

            var diff = (widthObject.invalid + widthObject.empty + widthObject.valid) - 100;
            if (diff > 0) {
                reduce(widthObject, diff);
            }

            vm.width = widthObject;
        };

        /**
         * @ngdoc method
         * @name computePercent
         * @methodOf data-prep.quality-bar.controller:QualityBarCtrl
         * @description [PRIVATE] Compute quality bars percentage
         */
        vm.computePercent = function computePercent() {
            var total = vm.quality.empty + vm.quality.invalid + vm.quality.valid;

            vm.percent = {
                invalid: vm.quality.invalid <= 0 ? 0 : Math.round(vm.quality.invalid * 100 / total),
                empty: vm.quality.empty <= 0 ? 0 : Math.round(vm.quality.empty * 100 / total),
                valid: vm.quality.valid <= 0 ? 0 : Math.round(vm.quality.valid * 100 / total)
            };
        };

        /**
         * @ngdoc method
         * @name hashQuality
         * @methodOf data-prep.quality-bar.controller:QualityBarCtrl
         * @description [PRIVATE] Calculate a simple hash from concatenating values
         */
        vm.hashQuality = function hashQuality() {
            return vm.quality.empty + '' + vm.quality.invalid + '' + vm.quality.valid;
        };
    }

    /**
     * @ngdoc property
     * @name transformationsForEmptyCells
     * @propertyOf data-prep.quality-bar.controller:QualityBarCtrl
     * @description The transformations applied to empty cells.
     * This is bound to {@link data-prep.services.transformation.service:ColumnSuggestionService ColumnSuggestionService}.transformationsForEmptyCells
     */
    Object.defineProperty(LookupQualityBarCtrl.prototype,
        'transformationsForEmptyCells', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.columnSuggestionService.transformationsForEmptyCells;
            }
        });

    /**
     * @ngdoc property
     * @name transformationsForInvalidCells
     * @propertyOf data-prep.quality-bar.controller:QualityBarCtrl
     * @description The transformations column applied to invalid cells.
     * This is bound to {@link data-prep.services.transformation.service:ColumnSuggestionService ColumnSuggestionService}.transformationsForInvalidCells
     */
    Object.defineProperty(LookupQualityBarCtrl.prototype,
        'transformationsForInvalidCells', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.columnSuggestionService.transformationsForInvalidCells;
            }
        });

    angular.module('data-prep.lookup-quality-bar')
        .controller('LookupQualityBarCtrl', LookupQualityBarCtrl);

})();