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

    function RangeSlider($timeout) {
        return {
            restrict: 'E',
            scope: {
                rangeLimits: '=',
                onBrushEnd: '&'
            },
            controller: 'RangeSliderCtrl',
            controllerAs: 'rangeSliderCtrl',
            bindToController: true,
            templateUrl: 'components/widgets/charts/range-slider/range-slider.html',

            link: function (scope, element, attrs, ctrl) {
                var renderTimeout;
                var filterToApply;

                ctrl.showRangeInputs = true;

                //the left and right margins MUST be the same as the vertical Barchart ones
                var margin = {top: 5, right: 20, bottom: 0, left: 15};
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
                    var rangeLimits = ctrl.rangeLimits;
                    var minBrush = typeof rangeLimits.minBrush !== 'undefined' ? rangeLimits.minBrush : rangeLimits.min;
                    var maxBrush = typeof rangeLimits.maxBrush !== 'undefined' ? rangeLimits.maxBrush : rangeLimits.max;
                    var minFilter = typeof rangeLimits.minFilterVal !== 'undefined' ? rangeLimits.minFilterVal : rangeLimits.min;
                    var maxFilter = typeof rangeLimits.maxFilterVal !== 'undefined' ? rangeLimits.maxFilterVal : rangeLimits.max;

                    var nbDecimals = d3.max([ctrl.decimalPlaces(minBrush), ctrl.decimalPlaces(maxBrush)]);
                    lastValues.input = {
                        min: minFilter,
                        max: maxFilter
                    };

                    ctrl.minMaxModel = {
                        minModel : ''+lastValues.input.min,
                        maxModel : ''+lastValues.input.max
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

                    var svg = d3.select('.range-slider-id').append('svg')
                        .attr('width', width + margin.left + margin.right)
                        .attr('height', height + margin.top + margin.bottom)
                        .attr('class', 'range-slider-cls')
                        .append('g')
                        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

                    //when brush has a single value (min = max), the brush is empty
                    //an empty brush is not rendered --> we set a delta
                    function handleUniqueBrushValue() {
                        if (ctrl.brush.empty()) {
                            var min = ctrl.brush.extent()[0];
                            var max = ctrl.brush.extent()[1];
                            var exp = '1e-' + (nbDecimals + 2);
                            svg.select('.brush').call(ctrl.brush.clear().extent([min, max + Number(exp)]));
                        }
                    }

                    //--------------------------------------------------------------------------------------------------
                    //--------------------PROCESS THE FILTER TO TRIGGER-------------------------------------------------
                    //--------------------------------------------------------------------------------------------------

                    function triggerFilter(filterToTrigger){
                        //check if the minFilter < maxFilter
                        filterToTrigger = filterToTrigger[0] > filterToTrigger[1] ? [filterToTrigger[1], filterToTrigger[0]] : filterToTrigger;
                        ctrl.onBrushEnd()(filterToTrigger);
                    }

                    function prepareBrushFilter(initialBrushValues){
                        var brushValues = ctrl.brush.extent();
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

                    ctrl.prepareInputFilter = function prepareInputFilter(){
                        if(!ctrl.areMinMaxNumbers()){
                            initInputValues();
                        }
                        else {
                            var enteredMin = +ctrl.minMaxModel.minModel;
                            var enteredMax = +ctrl.minMaxModel.maxModel;

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
                            brushg.transition().call(ctrl.brush.extent([lastValues.brush.min, lastValues.brush.max]));
                            //resize the brush with the delta
                            handleUniqueBrushValue();

                            filterToApply = [lastValues.input.min,lastValues.input.max];

                            triggerFilter(filterToApply);
                        }
                    };

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
                            .attr('y', height)
                            .attr('text-anchor', 'start')
                            .attr('fill', 'grey')
                            .text(function () {
                                return d3.format(',')(rangeLimits.min);
                            });

                        svg.append('g').append('text')
                            .attr('class', 'the-maximum-label')
                            .attr('x', width + 10)
                            .attr('y', height)
                            .attr('text-anchor', 'end')
                            .attr('fill', 'grey')
                            .text(function () {
                                return d3.format(',')(rangeLimits.max);
                            });

                        ctrl.brush = d3.svg.brush()
                                .x(scale)
                                .extent([centerValue, centerValue]);

                        brushg = svg.append('g')
                            .attr('transform', 'translate(0,' + (margin.top + 10) + ')')
                            .attr('class', 'brush')
                            .call(ctrl.brush);

                        brushg.call(ctrl.brush.event)
                            .transition().duration(400)
                            .call(ctrl.brush.extent([minBrush, maxBrush]));

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
                        ctrl.brush

                            .on('brushstart', function brushstart() {
                                //Will memorize the ancient extent
                                initialBrushValues = ctrl.brush.extent();
                            })

                            //It will update the min and max inputs, and create a brush on a single value when the user clicks on the slider without making a drag( the created brush will be empty )
                            .on('brush', function brushmove() {
                                var brushValues = ctrl.brush.extent();
                                if (initialBrushValues[0] !== brushValues[0]) {
                                    $timeout(function(){
                                        ctrl.minMaxModel.minModel = brushValues[0].toFixed(nbDecimals);
                                    });
                                }
                                if (initialBrushValues[1] !== brushValues[1]) {
                                    $timeout(function(){
                                        ctrl.minMaxModel.maxModel = brushValues[1].toFixed(nbDecimals);
                                    });
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
                        ctrl.invalidNumber = true;
                        var minMaxStr = ctrl.minMaxModel.minModel + ctrl.minMaxModel.maxModel;
                        ctrl.invalidNumberWithComma = ctrl.checkCommaExistence(minMaxStr);
                    }

                    //hides the message Error
                    function hideMsgErr() {
                        ctrl.invalidNumber = false;
                        ctrl.invalidNumberWithComma = false;
                    }

                    //Init min/max inputs values with existing filter values if defined, min/max otherwise
                    function initInputValues() {
                        hideMsgErr();
                        ctrl.minMaxModel.minModel = ''+lastValues.input.min;
                        ctrl.minMaxModel.maxModel = ''+lastValues.input.max;

                        filterToApply = [lastValues.input.min, lastValues.input.max];
                    }

                    function initRangeInputsListeners() {
                        var minCorrectness, maxCorrectness;
                        //create a key listener closure
                        ctrl.handleKey = function handleKey(rangeType) {
                            return function (e) {
                                switch (e.keyCode) {
                                    case 9:
                                    case 13:
                                        if (minCorrectness !== null && maxCorrectness !== null) {
                                            ctrl.prepareInputFilter();
                                        }
                                        else {
                                            initInputValues();
                                        }
                                        break;
                                    case 27:
                                        initInputValues();
                                        break;
                                    default:
                                        minCorrectness = rangeType === 'min' ? ctrl.toNumber(ctrl.minMaxModel.minModel) : minCorrectness;
                                        maxCorrectness = rangeType === 'max' ? ctrl.toNumber(ctrl.minMaxModel.maxModel) : maxCorrectness;

                                        if (minCorrectness === null || maxCorrectness === null) {
                                            showMsgErr();
                                        }
                                        else {
                                            hideMsgErr();
                                        }
                                }
                            };
                        };

                        //stop event propagation
                        ctrl.stopPropagation = function stopPropagation(e) {
                            e.stopPropagation();
                        };
                    }

                    //--------------------------------------------------------------------------------------------------
                    //--------------------WIDGET INITIALIZATION 1 RENDER------------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                    initInputValues();
                    initRangeInputsListeners();
                    initBrush();
                }


                function shouldRerender(newRangeLimits, oldRangeLimits) {
                    return !ctrl.brush ||
                        oldRangeLimits.min !== newRangeLimits.min ||
                        oldRangeLimits.max !== newRangeLimits.max ||
                        lastValues.brush.min !== newRangeLimits.minBrush ||
                        lastValues.brush.max !== newRangeLimits.maxBrush;
                }

                scope.$watch(function(){
                        return ctrl.rangeLimits;
                    },
                    function (newRangeLimits, oldRangeLimits) {
                        if (!newRangeLimits) {
                            element.find('svg').remove();
                            ctrl.showRangeInputs = false;
                            ctrl.brush = null;
                        }

                        else if (shouldRerender(newRangeLimits, oldRangeLimits)) {
                            element.find('svg').remove();
                            ctrl.showRangeInputs = false;
                            if (newRangeLimits.min !== newRangeLimits.max) {
                                clearTimeout(renderTimeout);
                                ctrl.showRangeInputs = true;
                                renderTimeout = setTimeout(function(){
                                    renderRangerSlider();
                                }, 100);
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