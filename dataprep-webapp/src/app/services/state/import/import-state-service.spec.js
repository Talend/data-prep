/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import', () => {
	beforeEach(angular.mock.module('data-prep.services.state'));

	describe('state service', () => {
		it('should set import types', inject((importState, ImportStateService) => {
			// given
			const imports = [{ name: 'import 1' }, { name: 'import 2' }];
			// when
			ImportStateService.setImportTypes(imports);

			// then
			expect(importState.importTypes).toBe(imports);
		}));

		it('should hide import modal ', inject((importState, ImportStateService) => {
			// given
			importState.visible = true;

			// when
			ImportStateService.hideImport();

			// then
			expect(importState.visible).toBe(false);
		}));

		it('should show import modal ', inject((importState, ImportStateService) => {
			// given
			importState.visible = false;

			// when
			ImportStateService.showImport();

			// then
			expect(importState.visible).toBe(true);
		}));
	});
});
