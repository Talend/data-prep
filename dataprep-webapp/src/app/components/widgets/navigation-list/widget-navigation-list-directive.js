/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.widget.directive:NavigationList
 * @description This directive display a navigable list of elements
 * @restrict E
 * @usage
 * <navigation-list
     list="list"
     on-click="callbackOn(item)"
     selected-item="selectedItem"
     get-label="callback(item)"
     on-add-item="true">
 </navigation-list>
 * @param {Array} list the collection to navigate into
 * @param {object} selectedItem selected Item from the given list
 * @param {function} onClick The callback executed on an item clicked
 * @param {function} getLabel The function that returns the label to show
 * @param {function} onAddItem The function is called when the add button is clicked
 */
export default function NavigationList($timeout) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: 'app/components/widgets/navigation-list/navigation-list.html',
        scope: {
            list: '=',
            selectedItem: '=',
            onClick: '&',
            getLabel: '&',
            onAddItem: '&'
        },
        bindToController: true,
        controllerAs: 'navigationListCtrl',
        controller: function () {
        },
        link: function (scope, iElement, iAttrs, ctrl) {
            var ITEM_WIDTH = 200;
            ctrl.showAddButton = !!iAttrs.onAddItem;

            $timeout(function () {

                var leftButton = iElement.find('.arrow-left').eq(0);
                var rightButton = iElement.find('.arrow-right').eq(0);
                var wrapper = iElement.find('.items-list-wrapper').eq(0);
                var itemsList = iElement.find('.items-list').eq(0);
                var posLeft = 0;

                function translate(leftPosition) {
                    itemsList.css('transform', 'translateX(' + leftPosition + 'px)');
                }

                function itemIsVisible(item) {
                    var itemPosition = ctrl.list.indexOf(item) * ITEM_WIDTH;
                    var actualPosition = posLeft + itemPosition;
                    return actualPosition >= 0 && actualPosition <= (wrapper.width() - ITEM_WIDTH);
                }

                function getVisiblePosition(item) {
                    var itemIndex = ctrl.list.indexOf(item);
                    var itemPosition = itemIndex * ITEM_WIDTH;
                    var actualPosition = posLeft + itemPosition;

                    //is not visible on the left
                    if (actualPosition < 0) {
                        return -itemPosition;
                    }

                    //is not visible on the right
                    var nbVisibleItems = Math.floor(wrapper.width() / ITEM_WIDTH) || 1;
                    return -(itemIndex + 1 - nbVisibleItems) * ITEM_WIDTH;
                }

                leftButton.on('click', function () {
                    if (posLeft < 0) {
                        posLeft += ITEM_WIDTH;
                        translate(posLeft);
                    }
                });

                rightButton.on('click', function () {
                    if ((posLeft + ctrl.list.length * ITEM_WIDTH) >= wrapper.width()) {
                        posLeft -= ITEM_WIDTH;
                        translate(posLeft);
                    }
                });

                function getSelectedItem() {
                    return ctrl.selectedItem;
                }

                function getList() {
                    return ctrl.list;
                }

                scope.$watchGroup([getList, getSelectedItem],
                    function () {
                        var hasCorrectSelectedItem = ctrl.list && ctrl.selectedItem && ctrl.list.indexOf(ctrl.selectedItem) > -1;
                        if (!hasCorrectSelectedItem) {
                            posLeft = 0;
                            translate(posLeft);
                        }
                        else if (!itemIsVisible(ctrl.selectedItem)) {
                            posLeft = getVisiblePosition(ctrl.selectedItem);
                            translate(posLeft);
                        }
                    }
                );
            }, 500, false);
        }
    };
}