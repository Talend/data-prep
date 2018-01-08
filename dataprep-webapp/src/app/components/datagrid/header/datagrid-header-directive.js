/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './datagrid-header.html';

/**
 * @ngdoc directive
 * @name data-prep.datagrid-header.directive:DatagridHeader
 * @description This directive takes care of a datagrid header item. It creates the header dropdown menu.
 * On creation, it calculate the quality bar values.
 * On double-click, it allows to rename the column name.
 *
 * Watchers:
 * <ul>
 *    <li>Close transformation menu on retrieve error, base on {@link data-prep.datagrid-header.controller:DatagridHeaderCtrl controller}.transformationsRetrieveError flag</li>
 * </ul>
 *
 * @restrict E
 * @usage <datagrid-header column="column"></datagrid-header>
 * @param {object} column The column metadata
 */
export default function DatagridHeader($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			column: '=',
		},
		bindToController: true,
		controllerAs: 'datagridHeaderCtrl',
		controller: 'DatagridHeaderCtrl',
		link: {
			post(scope, iElement, iAttrs, ctrl) {
				let gridHeader;
				let gridHeaderTitle;
				let gridHeaderTitleInput;

				/**
				 * @ngdoc method
				 * @name setEditionMode
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @param {boolean} value The new edition mode value
				 * @description Set edition mode to the provided value. This trigger a $digest.
				 */
				function setEditionMode(value) {
					ctrl.setEditMode(value);
					scope.$digest();
				}

				/**
				 * @ngdoc method
				 * @name setEditionMode
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @description Update column name if it has changed, just toggle edition mode otherwise
				 */
				function executeRenameAction() {
					ctrl.columnNameEdition.$commitViewValue();
					if (ctrl.nameHasChanged()) {
						ctrl.updateColumnName();
					}
					else {
						ctrl.resetColumnName();
						setEditionMode(false);
					}
				}

				/**
				 * @ngdoc method
				 * @name attachBlurListener
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @description Attach a 'Blur' event listener on input. It executes the column rename.
				 */
				function attachBlurListener() {
					gridHeaderTitleInput.on('blur', executeRenameAction);
				}

				/**
				 * @ngdoc method
				 * @name attachKeyListener
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @description Attach a 'Keydown' event listener on input. It handles the ENTER and ESC button
				 */
				function attachKeyListener() {
					gridHeaderTitleInput
						.keydown(function (event) {
							event.stopPropagation();
							switch (event.keyCode) {
							case 13: // ENTER
								gridHeaderTitleInput.blur();
								break;
							case 27: // ESC
								ctrl.resetColumnName();
								gridHeaderTitleInput.blur();
								break;
							}
						});
				}

				/**
				 * @ngdoc method
				 * @name attachDisableInputClick
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @description Disable all click in the input to prevent header and dropdown actions
				 */
				function attachDisableInputClick() {
					gridHeaderTitleInput.on('click', function (e) {
						e.preventDefault();
						e.stopPropagation();
					});
				}

				/**
				 * @ngdoc method
				 * @name attachDblClickListener
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @description Attach a 'DblClick' event listener on title. It toggle edition mode and focus/select input text
				 */
				function attachDblClickListener() {
					gridHeaderTitle.on('dblclick', function () {
						setEditionMode(true);

						$timeout(function () {
							gridHeaderTitleInput.focus();
							gridHeaderTitleInput.select();
						}, 100, false);
					});
				}


				/**
				 * @ngdoc method
				 * @name attachClickListener
				 * @methodOf data-prep.datagrid-header.directive:DatagridHeader
				 * @description Attach a 'Click' event listener on grid header
				 */
				function attachClickListener() {
					gridHeader.mouseup(function (event) {
						if (event.which === 3) { // Right click
							gridHeader.find('.grid-header-caret').click();
						}
					});
				}

				/**
				 * Get the title and input elements, attach their listeners
				 */
				$timeout(function () {
					gridHeader = iElement.find('.grid-header').eq(0);
					gridHeaderTitle = gridHeader.find('.grid-header-title').eq(0);
					gridHeaderTitleInput = gridHeader.find('.grid-header-title-input').eq(0);

					attachKeyListener();
					attachDblClickListener();
					attachBlurListener();
					attachDisableInputClick();
					attachClickListener();
				}, 0, false);

				/**
				 * Close transformation menu on retrieve error
				 */
				scope.$watch(
					function () {
						return ctrl.transformationsRetrieveError;
					},

					function (newValue) {
						if (newValue) {
							$timeout(() => {
								const headerDropdownAction = iElement.find('.grid-header-caret').eq(0);
								headerDropdownAction.click();
							});
						}
					});

				iElement.on('$destroy', function () {
					scope.$destroy();
				});
			},
		},
	};
}
