/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================ */

import { DATA_FEATURE_ATTR } from './inventory-list-service';
import {
	DATA_FEATURE_RENAME,
	DATA_FEATURE_FAVORITE,
} from './inventory-list-service';

describe('Inventory List Service', () => {
	beforeEach(angular.mock.module('data-prep.services.inventory'));

	describe('adaptAction', () => {
		let action;
		let hostModel;
		let createAction;
		let createHostModel;

		beforeEach(() => {
			createAction = (dataFeature) => {
				action = {
					[[DATA_FEATURE_ATTR]]: dataFeature,
				};
			};
		});

		afterEach(() => {
			action = undefined;
			hostModel = undefined;
			createHostModel = undefined;
		});

		describe('rename', () => {
			beforeEach(() => {
				createAction(DATA_FEATURE_RENAME);
				createHostModel = (type) => {
					hostModel = {
						type,
					};
				};
			});

			it('should not adapt rename action if there is no type', inject((InventoryListService) => {
				// given
				createHostModel(undefined);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(DATA_FEATURE_RENAME);
			}));

			it('should adapt rename action if there is folder type', inject((InventoryListService) => {
				// given
				const modelType = 'folder';
				createHostModel(modelType);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(`${modelType}.rename`);
			}));

			it('should adapt rename action if there is preparation type', inject((InventoryListService) => {
				// given
				const modelType = 'preparation';
				createHostModel(modelType);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(`${modelType}.rename`);
			}));

			it('should adapt rename action if there is dataset type', inject((InventoryListService) => {
				// given
				const modelType = 'dataset';
				createHostModel(modelType);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(`${modelType}.rename`);
			}));
		});

		describe('favorite', () => {
			beforeEach(() => {
				createAction(`${DATA_FEATURE_FAVORITE}.add`);
				createHostModel = (favorite) => {
					hostModel = {
						model: {
							favorite,
						},
					};
				};
			});

			it('should not adapt favorite action if there is no favorite information', inject((InventoryListService) => {
				// given
				createHostModel(undefined);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(`${DATA_FEATURE_FAVORITE}.add`);
			}));

			it('should adapt favorite action when it is in favorites', inject((InventoryListService) => {
				// given
				createHostModel(true);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(`${DATA_FEATURE_FAVORITE}.remove`);
			}));

			it('should adapt favorite action when it is not in favorites', inject((InventoryListService) => {
				// given
				createHostModel(false);

				// when
				const adaptedAction = InventoryListService.adaptAction(action, hostModel);

				// then
				expect(adaptedAction[DATA_FEATURE_ATTR])
					.toBe(`${DATA_FEATURE_FAVORITE}.add`);
			}));
		});
	});
});
