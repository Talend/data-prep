(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:verticalBarchart
     * @description This directive renders the vertical bar chart.
     * @restrict E
     * @usage
     * <vertical-barchart id="vBarChart"
     *     width="320"
     *     height="400"
     *     on-click="columnProfileCtrl.vBarchartClickFn"
     *     visu-data="columnProfileCtrl.histogram.data"
     *     key-field="data"
     *     key-label="{{columnProfileCtrl.histogram.label}}">
     *     value-field="{{columnProfileCtrl.histogram.key}}"
     *     active-limits="columnProfileCtrl.histogram.existingFilter"
     * </vertical-barchart>
     * @param {number} width The chart width
     * @param {number} height The chart height
     * @param {function} onClick The callback on chart click
     * @param {array} visuData The value array to render
     * @param {string} keyField The key property name in visuData elements
     * @param {object} keyLabel The label property name in visuData elements
     * @param {string} valueField The value property name in visuData elements used for 1st column
     * @param {string} valueField2 The value property name in visuData elements used for 2nd column
     * @param {array} activeLimits The filter limits
     * */

    function VerticalBarchart() {
        return {
            restrict: 'E',
            scope: {
                onClick: '&',
                visuData: '=',
                keyField: '@',
                valueField: '@',
                valueField2: '@',
                keyLabel: '@',
                activeLimits: '='
            },
            link: function (scope, element, attrs) {
                var xField = scope.keyField;//data
                var yField = scope.valueField;
                var yField2 = scope.valueField2;
                var labelTooltip = scope.keyLabel;
                var activeLimits = scope.activeLimits;
                var renderTimeout, updateBarsTimeout;
                var tip;
                var finishedRendering = false;

                function renderVBarchart(statData) {
                    var container = attrs.id;
                    var width = +attrs.width;
                    var height = +attrs.height;

                    //the left and right margins MUST be the same as the rangeSlider ones
                    var margin = {top: 20, right: 20, bottom: 10, left: 15},
                        w = width - margin.left - margin.right,
                        h = height - margin.top - margin.bottom;

                    var x = d3.scale.ordinal()
                        .rangeRoundBands([0, w], 0.2);
                    var y = d3.scale.linear()
                        .range([h, 0]);

                    tip = d3.tip()
                        .attr('class', 'vertical-barchart-cls d3-tip')
                        .offset([0, -11])
                        .direction('w')
                        .html(function (d) {
                            var interval =  d[xField];
                            var uniqueValue = interval[0] === interval[1];
                            return '<strong>' + labelTooltip + ':</strong> <span style="color:yellow">' + d[yField2] + ' / ' + d[yField] + '</span>' +
                                '<br/>' +
                                '<br/>' +
                                '<strong>' + (uniqueValue ? 'Value:' : 'Range:') + '</strong> ' +
                                '<span style="color:yellow">' + (uniqueValue ? interval[0] : '[' + interval + '[') + '</span>';
                        });

                    var svg = d3.select('#' + container).append('svg')
                        .attr('class', 'vertical-barchart-cls')
                        .attr('width', w + margin.left + margin.right)
                        .attr('height', h + margin.top + margin.bottom)
                        .append('g')
                        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

                    svg.call(tip);

                    // Parse numbers, and sort by value.
                    statData.forEach(function (d) {
                        d[yField] = +d[yField];
                    });

                    // Set the scale domain.
                    x.domain(statData.map(function (d) {
                        return d[xField];
                    }));
                    y.domain([0, d3.max(statData, function (d) {
                        return d[yField];
                    })]);

                    svg.append('g').selectAll('.bar')
                        .data(statData)
                        .enter().append('rect')
                        .attr('class', 'bar')
                        .attr('x', function (d) {
                            return x(d[xField]);
                        })
                        .attr('width', x.rangeBand())
                        .attr('y', function () {
                            return y(0);
                        })
                        .attr('height', 0)
                        .transition().ease('cubic').delay(function (d, i) {
                            return i * 10;
                        })
                        .attr('height', function (d) {
                            return h - y(d[yField]);
                        })
                        .attr('y', function (d) {
                            return y(d[yField]);
                        });

                    svg.append('g').selectAll('.filterBar')
                        .data(statData)
                        .enter().append('rect')
                        .attr('class', 'filterBar')
                        .attr('x', function (d) {
                            return x(d[xField]);
                        })
                        .attr('width', x.rangeBand())
                        .attr('y', function () {
                            return y(0);
                        })
                        .attr('height', 0)
                        .transition().ease('cubic').delay(function (d, i) {
                            return i * 10;
                        })
                        .attr('height', function (d) {
                            if (typeof d[yField2] !== 'undefined') {
                                return h - y(d[yField2]);
                            }
                            return h - y(0);
                        })
                        .attr('y', function (d) {
                            if (typeof d[yField2] !== 'undefined') {
                                return y(d[yField2]);
                            }
                            return y(0);
                        });


                    scope.buckets = d3.selectAll('rect.bar');
                    /****************** Horizontal grid **********************/
                    var hGrid = svg.append('g')
                        .attr('class', 'grid')
                        .call(d3.svg.axis()
                            .scale(y)
                            .orient('right')
                            .tickSize(w, 0, 0)
                            .tickFormat(d3.format(',d'))
                            .ticks(Math.ceil(h / 18))
                    );

                    hGrid.selectAll('.tick text')
                        .attr('y', -5)
                        .attr('x', w / 2)
                        .attr('dy', '.15em')
                        .style('text-anchor', 'middle');

                    /********************* Y axis label *********************/
                    svg.append('g')
                        .append('text')
                        .attr('x', -h / 2)
                        .attr('y', -2)
                        .attr('transform', 'rotate(-90)')
                        .style('text-anchor', 'middle')
                        .text(labelTooltip);

                    /************background Rect*********/
                    var bgBar = svg.selectAll('g.bg-rect')
                        .data(statData)
                        .enter().append('g')
                        .attr('transform', function (d) {
                            return 'translate(' + (x(d[xField]) - 2) + ', 0)';
                        });

                    bgBar.append('rect')
                        .attr('width', x.rangeBand() + 4)
                        .attr('height', h)
                        .attr('class', 'bg-rect')
                        .style('opacity', 0)
                        .attr('z-index', 100)
                        .on('mouseenter', function (d) {
                            d3.select(this).style('opacity', 0.4);
                            tip.show(d);
                        })
                        .on('mouseleave', function (d) {
                            d3.select(this).style('opacity', 0);
                            tip.hide(d);
                        })
                        .on('click', function (d) {
                            scope.onClick({interval: d.data});
                        });
                    finishedRendering = true;
                }

                function updateBarsLookFeel() {
                    if (activeLimits) {
                        scope.buckets.transition()
                            .delay(function (d, i) {
                                return i * 10;
                            })
                            .style('opacity', function (d, i) {
                                if (((d.data)[0] >= activeLimits[0] && (d.data)[1] <= activeLimits[1]) ||
                                    ((d.data)[0] < activeLimits[1] && (d.data)[1] > activeLimits[0])
                                ) {
                                    if (d3.select(scope.buckets[0][i]).style('opacity') === '.4') {
                                        return '1';
                                    }
                                }
                                else {
                                    return '.4';
                                }
                            });
                    }
                }

                scope.$watch('visuData',
                    function (statData) {
                        element.empty();
                        //because the tooltip is not a child of the vertical barchart element
                        d3.selectAll('.vertical-barchart-cls.d3-tip').remove();
                        if (statData) {
                            finishedRendering = false;
                            clearTimeout(renderTimeout);
                            renderTimeout = setTimeout(renderVBarchart.bind(this, statData), 100);
                        }
                    }
                );

                scope.$watch('activeLimits',
                    function (newFilter) {
                        if (newFilter) {
                            var wait = 600;
                            if (finishedRendering) {
                                wait = 100;
                            }
                            clearTimeout(updateBarsTimeout);
                            updateBarsTimeout = setTimeout(function () {
                                activeLimits = newFilter;
                                updateBarsLookFeel();
                            }, wait);
                        }
                    }
                );
            }
        };
    }

    angular.module('talend.widget')
        .directive('verticalBarchart', VerticalBarchart);
})();