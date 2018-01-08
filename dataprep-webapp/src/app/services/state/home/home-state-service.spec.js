/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import i18n from '../../../../i18n/en.json';

describe('home state', () => {

	beforeEach(angular.mock.module('data-prep.services.state'));

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	describe('toggleSidepanel', () => {
		it('should toggle sidepanel', inject((homeState, HomeStateService) => {
			//given
			homeState.sidePanelDocked = false;

			//when
			HomeStateService.toggleSidepanel();

			//then
			expect(homeState.sidePanelDocked).toBe(true);
		}));
	});

	describe('toggleCopyMovePreparation', () => {
		it('should toggle copy/move preparation', inject((homeState, HomeStateService) => {
			//given
			homeState.preparations.copyMove.isVisible = false;

			//when
			HomeStateService.toggleCopyMovePreparation();

			//then
			expect(homeState.preparations.copyMove.isVisible).toBe(true);
		}));

		it('should set preparation and its initial folder',
			inject((homeState, HomeStateService) => {
				//given
				homeState.preparations.copyMove.initialFolder = { id: 'folder 1' };
				homeState.preparations.copyMove.preparation = { id: 'prep 1' };

				const initialFolder = { id: 'folder 2' };
				const preparation = { id: 'prep 2' };

				//when
				HomeStateService.toggleCopyMovePreparation(initialFolder, preparation);

				//then
				expect(homeState.preparations.copyMove.initialFolder).toBe(initialFolder);
				expect(homeState.preparations.copyMove.preparation).toBe(preparation);
			})
		);

		it('should set copy move tree',
			inject((homeState, HomeStateService) => {
				//given
				homeState.preparations.copyMove.tree = { id: 'folder 1' };

				const tree = { id: 'folder 2' };

				//when
				HomeStateService.setCopyMoveTree(tree);

				//then
				expect(homeState.preparations.copyMove.tree).toBe(tree);
			})
		);

		it('should set copy move tree loading',
			inject((homeState, HomeStateService) => {
				//given
				homeState.preparations.copyMove.isTreeLoading = false;

				//when
				HomeStateService.setCopyMoveTreeLoading(true);

				//then
				expect(homeState.preparations.copyMove.isTreeLoading).toBe(true);
			})
		);
	});

	describe('togglePreparationCreator', () => {
		it('should toggle preparation creator', inject((homeState, HomeStateService) => {
			//given
			homeState.preparations.creator.isVisible = false;

			//when
			HomeStateService.togglePreparationCreator();

			//then
			expect(homeState.preparations.creator.isVisible).toBe(true);
		}));
	});

	describe('toggleFolderCreator', () => {
		it('should toggle folder creator', inject((homeState, HomeStateService) => {
			//given
			homeState.folders.creator.isVisible = false;

			//when
			HomeStateService.toggleFolderCreator();

			//then
			expect(homeState.folders.creator.isVisible).toBe(true);
		}));
	});

	describe('About', () => {
		it('should toggle about modal', inject((homeState, HomeStateService) => {
			//when
			HomeStateService.toggleAbout();

			//then
			expect(homeState.about.isVisible).toBe(true);
		}));

		it('should populate builds', inject((homeState, HomeStateService) => {
			//given
			const allBuildDetails = [
				{
					"versionId": "2.0.0-SNAPSHOT",
					"buildId": "2adb70d",
					"serviceName": "API"
				},
				{
					"versionId": "2.0.0-SNAPSHOT",
					"buildId": "2adb70d",
					"serviceName": "DATASET"
				},
				{
					"versionId": "2.0.0-SNAPSHOT",
					"buildId": "2adb70d",
					"serviceName": "PREPARATION"
				},
				{
					"versionId": "2.0.0-SNAPSHOT",
					"buildId": "2adb70d",
					"serviceName": "TRANSFORMATION"
				}
			];

			//when
			HomeStateService.setBuilds(allBuildDetails);

			//then
			expect(homeState.about.builds).toBe(allBuildDetails);
		}));
	});
});
