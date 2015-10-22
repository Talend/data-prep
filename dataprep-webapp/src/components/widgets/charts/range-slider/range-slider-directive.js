(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:rangeSlider
     * @description This directive renders the rangeSlider.
     * @restrict E
     * @usage
     *     <range-slider
     *             id="domId"
     *             width="300"
     *             height="100"
     *             range-limits="statsDetailsCtrl.rangeLimits"
     *             on-brush-end="statsDetailsCtrl.onBrushEndFn"
     *         ></range-slider>
     * @param {string} id The element id
     * @param {number} width The width of the slider
     * @param {number} height The height of the slider
     * @param {object} rangeLimits {min: number, max: number} The limits of the slider
     * @param {function} onBrushEnd The callback on slider move
     * */

    function RangeSlider($translate) {
        return {
            restrict: 'E',
            scope: {
                rangeLimits: '=',
                onBrushEnd: '&'
            },
            controller: 'RangeSliderCtrl',
            controllerAs: 'rangeSliderCtrl',
            link: function (scope, element, attrs, ctrl) {
                var renderTimeout;
                var container = attrs.id;
                var filterToApply;

                //the left and right margins MUST be the same as the vertical Barchart ones
                var margin = {top: 5, right: 20, bottom: 10, left: 15};
                var width = attrs.width - margin.left - margin.right;
                var height = attrs.height - margin.top - margin.bottom;

                var lastValues = {
                    input : {},
                    brush : {}
                };

                var brushg;

                /**
                 * @ngdoc method
                 * @name renderRangerSlider
                 * @methodOf data-prep.rangeSlider.directive:RangeSlider
                 * @description Render the slider and attach the actions listeners
                 **/
                function renderRangerSlider(){
                    var rangeLimits = scope.rangeLimits;
                    var minBrush = typeof rangeLimits.minBrush !== 'undefined' ? rangeLimits.minBrush : rangeLimits.min;
                    var maxBrush = typeof rangeLimits.maxBrush !== 'undefined' ? rangeLimits.maxBrush : rangeLimits.max;
                    var minFilter = typeof rangeLimits.minFilterVal !== 'undefined' ? rangeLimits.minFilterVal : rangeLimits.min;
                    var maxfilter = typeof rangeLimits.maxFilterVal !== 'undefined' ? rangeLimits.maxFilterVal : rangeLimits.max;

                    var nbDecimals = d3.max([ctrl.decimalPlaces(minBrush), ctrl.decimalPlaces(maxBrush)]);
                    lastValues.input = {
                        min: minFilter,
                        max: maxfilter
                    };
                    lastValues.brush = {
                        min: minBrush,
                        max: maxBrush
                    };

                    var centerValue = (minBrush + maxBrush) / 2;

                    //--------------------------------------------------------------------------------------------------
                    //----------------------------------------------CONTAINER-------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                    var scale = d3.scale.linear()
                        .domain([rangeLimits.min, rangeLimits.max])
                        .range([0, width]);

                    var svg = d3.select('#' + container).append('svg')
                        .attr('width', width + margin.left + margin.right)
                        .attr('height', height + margin.top + margin.bottom)
                        .attr('class', 'range-slider-cls')
                        .append('g')
                        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

                    //when brush has a single value (min = max), the brush is empty
                    //an empty brush is not rendered --> we set a delta
                    function handleUniqueBrushValue() {
                        if (scope.brush.empty()) {
                            var min = scope.brush.extent()[0];
                            var max = scope.brush.extent()[1];
                            var exp = '1e-' + (nbDecimals + 2);
                            svg.select('.brush').call(scope.brush.clear().extent([min, max + Number(exp)]));
                        }
                    }

                    //--------------------------------------------------------------------------------------------------
                    //--------------------PROCESS THE FILTER TO TRIGGER-------------------------------------------------
                    //--------------------------------------------------------------------------------------------------

                    function triggerFilter(filterToTrigger){
                        //check if the minFilter < maxFilter
                        filterToTrigger = filterToTrigger[0] > filterToTrigger[1] ? [filterToTrigger[1], filterToTrigger[0]] : filterToTrigger;
                        scope.onBrushEnd()(filterToTrigger);
                    }

                    function prepareBrushFilter(initialBrushValues){
                        var brushValues = scope.brush.extent();
                        lastValues.brush.min = +brushValues[0].toFixed(nbDecimals);
                        lastValues.brush.max = +brushValues[1].toFixed(nbDecimals);

                        //The case where the user clicks on an existing brush
                        // but DOES NOT drag n drop it
                        if(lastValues.brush.min === initialBrushValues[0] &&
                            lastValues.brush.max === initialBrushValues[1]){
                            return;
                        }

                        //left brush moved
                        if (initialBrushValues[0] !== lastValues.brush.min) {
                            filterToApply[0] = lastValues.brush.min;
                            lastValues.input.min = lastValues.brush.min;
                        }
                        //right brush moved
                        if (initialBrushValues[1] !== lastValues.brush.max) {
                            filterToApply[1] = lastValues.brush.max;
                            lastValues.input.max = lastValues.brush.max;
                        }

                        handleUniqueBrushValue();
                        triggerFilter(filterToApply);
                    }

                    function prepareInputFilter(){
                        var enteredMin = +document.getElementsByName('minRange')[0].value;
                        var enteredMax = +document.getElementsByName('maxRange')[0].value;

                        //2 Cases:
                        //- ONBLUR: the user selects the input then he selects sth else
                        //- ONENTER without changing anything
                        if(lastValues.input.min === enteredMin &&
                            lastValues.input.max === enteredMax){
                            return;
                        }
                        lastValues.input.min = enteredMin;
                        lastValues.input.max = enteredMax;

                        //resize the brush to the right extent
                        nbDecimals = d3.max([ctrl.decimalPlaces(enteredMin), ctrl.decimalPlaces(enteredMax)]);
                        lastValues.brush = ctrl.adaptRangeValues(enteredMin, enteredMax, rangeLimits.min, rangeLimits.max);
                        brushg.transition().call(scope.brush.extent([lastValues.brush.min, lastValues.brush.max]));
                        //resize the brush with the delta
                        handleUniqueBrushValue();

                        filterToApply = [lastValues.input.min,lastValues.input.max];

                        triggerFilter(filterToApply);
                    }

                    //--------------------------------------------------------------------------------------------------
                    //--------------------BRUSH WIDGET------------------------------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                    function initBrush() {
                        //create axis + brush
                        var axisTicksNumber = rangeLimits.max > 1e12 || rangeLimits.min < 1e-10 ? 2 : 3;
                        svg.append('g')
                            .attr('class', 'x axis')
                            .attr('transform', 'translate(0,' + (margin.top + 20) + ')')
                            .call(d3.svg.axis()
                                .scale(scale)
                                .orient('top')
                                .ticks(axisTicksNumber)
                                .tickFormat(function (d) {
                                    return d3.format(',')(d);
                                }))
                            .selectAll('text').attr('y', -13);

                        svg.append('g').append('text')
                            .attr('class', 'the-minimum-label')
                            .attr('x', -10)
                            .attr('y', height / 2)
                            .attr('text-anchor', 'start')
                            .attr('fill', 'grey')
                            .text(function () {
                                return d3.format(',')(rangeLimits.min);
                            });

                        svg.append('g').append('text')
                            .attr('class', 'the-maximum-label')
                            .attr('x', width + 10)
                            .attr('y', height / 2)
                            .attr('text-anchor', 'end')
                            .attr('fill', 'grey')
                            .text(function () {
                                return d3.format(',')(rangeLimits.max);
                            });

                        scope.brush = d3.svg.brush()
                                .x(scale)
                                .extent([centerValue, centerValue]);

                        brushg = svg.append('g')
                            .attr('transform', 'translate(0,' + (margin.top + 10) + ')')
                            .attr('class', 'brush')
                            .call(scope.brush);

                        brushg.call(scope.brush.event)
                            .transition().duration(400)
                            .call(scope.brush.extent([minBrush, maxBrush]));

                        brushg.selectAll('.resize')
                            .append('rect')
                            .attr('transform', function (d, i) {
                                return i ? 'translate(-10, 0)' : 'translate(0,0)';
                            })
                            .attr('width', 10);

                        brushg.selectAll('rect')
                            .attr('height', 20);

                        brushg.select('.extent')
                            .attr('height', 14)
                            .attr('transform', 'translate(0,3)');

                        //The case where the applied filter have the same value and the user click back on the column or
                        // on the CHART tab
                        handleUniqueBrushValue();

                        //The case where the user interacts with the brush
                        var initialBrushValues;
                        scope.brush

                            .on('brushstart', function brushstart() {
                                //Will memorize the ancient extent
                                initialBrushValues = scope.brush.extent();
                            })

                            //It will update the min and max inputs, and create a brush on a single value when the user clicks on the slider without making a drag( the created brush will be empty )
                            .on('brush', function brushmove() {
                                var brushValues = scope.brush.extent();
                                if (initialBrushValues[0] !== brushValues[0]) {
                                    document.getElementsByName('minRange')[0].value = brushValues[0].toFixed(nbDecimals);
                                }
                                if (initialBrushValues[1] !== brushValues[1]) {
                                    document.getElementsByName('maxRange')[0].value = brushValues[1].toFixed(nbDecimals);
                                }
                            })

                            //It will propagate the new filter limits to the rest of the app, it's triggered when the user finishes a brush
                            .on('brushend', function brushend() {
                                prepareBrushFilter(initialBrushValues);
                            });
                    }

                    //--------------------------------------------------------------------------------------------------
                    //--------------------INPUT WIDGET------------------------------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                    //shows the message Error with details on comma existence
                    function showMsgErr() {
                        var msgErr = $translate.instant('INVALID_VALUE_RANGE_SLIDER') ;
                        var minMaxStr = document.getElementsByName('minRange')[0].value + document.getElementsByName('maxRange')[0].value;
                        var finalMsgErr = ctrl.checkCommaExistence(minMaxStr) ? msgErr + $translate.instant('INVALID_VALUE_RANGE_SLIDER_CONTENT') : msgErr;
                        d3.select('text.invalid-value-msg').text(finalMsgErr);
                    }

                    //hides the message Error
                    function hideMsgErr() {
                        d3.select('text.invalid-value-msg').text('');
                    }

                    //Init min/max inputs values with existing filter values if defined, min/max otherwise
                    function initInputValues() {
                        hideMsgErr();
                        document.getElementsByName('minRange')[0].value = lastValues.input.min;
                        document.getElementsByName('maxRange')[0].value = lastValues.input.max;
                        filterToApply = [lastValues.input.min, lastValues.input.max];
                    }

                    function initRangeInputsListeners() {
                        var minCorrectness, maxCorrectness;

                        //create a key listener closure
                        function handleKey(rangeType) {
                            return function (e) {
                                switch (e.keyCode) {
                                    case 9:
                                    case 13:
                                        if (minCorrectness !== null && maxCorrectness !== null) {
                                            prepareInputFilter();
                                        }
                                        else {
                                            initInputValues();
                                            hideMsgErr();
                                        }
                                        break;
                                    case 27:
                                        initInputValues();
                                        break;
                                    default:
                                        minCorrectness = rangeType === 'min' ? ctrl.toNumber(this.value) : minCorrectness;
                                        maxCorrectness = rangeType === 'max' ? ctrl.toNumber(this.value) : maxCorrectness;

                                        if (minCorrectness === null || maxCorrectness === null) {
                                            showMsgErr();
                                        }
                                        else {
                                            hideMsgErr();
                                        }
                                }
                            };
                        }

                        //stop event propagation
                        function stopPropagation(e) {
                            e.stopPropagation();
                        }

                        var minRange = document.getElementsByName('minRange')[0];
                        var maxRange = document.getElementsByName('maxRange')[0];

                        minRange.onblur = prepareInputFilter;
                        minRange.onkeydown = stopPropagation;
                        minRange.onkeyup = handleKey('min');

                        maxRange.onblur = prepareInputFilter;
                        maxRange.onkeydown = stopPropagation;
                        maxRange.onkeyup = handleKey('max');
                    }

                    function initInput() {
                        //create inputs
                        svg.append('g').append('foreignObject')
                            .attr('width', width)
                            .attr('height', 40)
                            .attr('transform', 'translate(0,' + (height - 45) + ')')
                            .append('xhtml:div')
                            .html('<span><b>Min </b><input type="text" name="minRange"></span> <span style="float:right;"><b>Max </b> <input type="text" name="maxRange"/></span>');

                        svg.append('g').append('text')
                            .attr('class', 'invalid-value-msg')
                            .attr('x', width / 2)
                            .attr('y', height)
                            .attr('text-anchor', 'middle')
                            .attr('fill', 'red');

                        initInputValues();

                        initRangeInputsListeners();
                    }

                    //--------------------------------------------------------------------------------------------------
                    //--------------------WIDGET INITIALIZATION 1 RENDER------------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                    initInput();
                    initBrush();
                }


                function shouldRerender(newRangeLimits, oldRangeLimits) {
                    return !scope.brush ||
                        oldRangeLimits.min !== newRangeLimits.min ||
                        oldRangeLimits.max !== newRangeLimits.max ||
                        lastValues.brush.min !== newRangeLimits.minBrush ||
                        lastValues.brush.max !== newRangeLimits.maxBrush;
                }

                scope.$watch('rangeLimits',
                    function (newRangeLimits, oldRangeLimits) {
                        if (!newRangeLimits) {
                            element.empty();
                            scope.brush = null;
                        }

                        else if (shouldRerender(newRangeLimits, oldRangeLimits)) {
                            element.empty();
                            if (newRangeLimits.min !== newRangeLimits.max) {
                                clearTimeout(renderTimeout);
                                renderTimeout = setTimeout(renderRangerSlider, 100);
                            }
                        }
                    }
                );
            }
        };
    }

    angular.module('talend.widget')
        .directive('rangeSlider', RangeSlider);
})();