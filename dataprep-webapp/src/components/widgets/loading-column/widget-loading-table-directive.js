(function () {
    'use strict';

    var columnOverlay = '<div class="loading-column"><div class="loading-column-inner"></div></div>';

    /**
     * Loading windows management on tables
     * <table talend-loading-table id="myTable">
     *     <tr>
     *         <th>Title 1</th>
     *         <th>Title 2</th>
     *     </tr>
     * </table>
     *
     * Id on table is mandatory. It defines the event message that TalendLoadingTable will manage
     * $rootScope.$emit('talend.loading.<table_id>.start', {col: colIndex}) : il will create a loading-column, attach it to the table parent DOM element, and set its position over the whole column
     * $rootScope.$emit('talend.loading.<table_id>.stop', {col: colIndex}) : il will remove a loading-column
     * $rootScope.$emit('talend.loading.<table_id>.all') : il will remove all loading-column
     */
    function TalendLoadingTable($rootScope, $compile, $timeout, $window, $animate) {
        return {
            restrict: 'A',
            link: {
                post: function (scope, iElement) {
                    //loading column elements
                    var loadingElements = [];
                    //loading column element scopes
                    var loadingScopes = [];
                    //loading column element resize and position functions
                    var loadingResize = [];

                    /**
                     * Remove loading on a col
                     * @param col - the table column number
                     */
                    var removeLoading = function (col) {
                        var element = loadingElements[col];
                        var scope = loadingScopes[col];
                        var resize = loadingResize[col];

                        if (scope) {
                            scope.$destroy();
                            loadingScopes[col] = null;
                        }

                        if(element) {
                            $animate.addClass(element, 'loading-column-hide').then(function() {
                                element.remove();
                                loadingElements[col] = null;
                            });
                        }

                        if(resize) {
                            $window.removeEventListener('resize', resize);
                            loadingResize[col] = null;
                        }
                    };

                    var removeAllLoading = function() {
                        for(var key in loadingElements) {
                            removeLoading(key);
                        }
                    };

                    /**
                     * Create a loading element on a col
                     * @param col - the table column number
                     * @returns element
                     */
                    var createElement = function(col) {
                        var scope = $rootScope.$new(true);
                        loadingScopes[col] = scope;

                        var element = angular.element(columnOverlay);
                        $compile(element)(scope);
                        loadingElements[col] = element;
                        iElement.parent().append(element);

                        var updateSizeAndPosition = function() {
                            var header = iElement[0].querySelectorAll('th')[col];
                            var rows = iElement[0].querySelectorAll('tr');
                            var lastRow = rows[rows.length - 1];

                            var headerPosition = header.getBoundingClientRect();
                            var lastRowPosition = lastRow.getBoundingClientRect();
                            var parentOffset = iElement.parent().offset();

                            var css = {
                                position: 'absolute',
                                top: (headerPosition.top - parentOffset.top) + 'px',
                                left: (headerPosition.left - parentOffset.left) + 'px',
                                width: header.offsetWidth + 'px',
                                height: (lastRowPosition.bottom - headerPosition.top) + 'px'
                            };

                            element.css(css);
                        };
                        loadingResize[col] = updateSizeAndPosition;
                        $window.addEventListener('resize', updateSizeAndPosition);

                        return element;
                    };

                    $timeout(function() {
                        /**
                         * Init a loading on table with a particular id, on the column il argument
                         * @type {function()|*}
                         */
                        var cleanUpListenerStart = $rootScope.$on('talend.loading.' + iElement[0].id + '.start', function (event, args) {
                            var col = args.col;
                            if(loadingElements[col]) {
                                return;
                            }

                            createElement(col);
                            loadingResize[col]();
                        });

                        /**
                         * Remove a column loading
                         * @type {function()|*}
                         */
                        var cleanUpListenerStop = $rootScope.$on('talend.loading.' + iElement[0].id + '.stop', function (event, args) {
                            $timeout(removeLoading.bind(this, args.col));
                        });

                        /**
                         * Remove all the column loadings
                         * @type {function()|*}
                         */
                        var cleanUpListenerStopAll = $rootScope.$on('talend.loading.' + iElement[0].id + '.stop.all', function () {
                            $timeout(removeAllLoading);
                        });

                        /**
                         * Clean the listeners on scope destroy
                         */
                        scope.$on('$destroy', function () {
                            $timeout(removeAllLoading);
                            cleanUpListenerStart();
                            cleanUpListenerStop();
                            cleanUpListenerStopAll();
                        });
                    });
                }
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendLoadingTable', TalendLoadingTable);
})();