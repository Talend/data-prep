/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('confirm state service', () => {
	beforeEach(angular.mock.module('data-prep.services.state'));

	beforeEach(inject(confirmState => {
		confirmState.visible = false;
		confirmState.title = null;
		confirmState.texts = [];
	}));

	it('should set visible to true', inject((ConfirmStateService, confirmState) => {
		expect(confirmState.visible).toBe(false);
		expect(confirmState.title).toBeNull();
		expect(confirmState.texts).toEqual([]);
		ConfirmStateService.show('TEST', ['test']);
		expect(confirmState.visible).toBe(true);
		expect(confirmState.title).toEqual('TEST');
		expect(confirmState.texts).toEqual(['test']);
	}));

	it('should set visible to false', inject((ConfirmStateService, confirmState) => {
		confirmState.visible = true;
		confirmState.title = 'HEY';
		confirmState.texts = ['hey'];
		ConfirmStateService.hide();
		expect(confirmState.visible).toBe(false);
		expect(confirmState.title).toBeNull();
		expect(confirmState.texts).toEqual([]);
	}));

	it('should reset visible state', inject((ConfirmStateService, confirmState) => {
		confirmState.visible = true;
		confirmState.title = 'YO';
		confirmState.texts = ['yo'];
		ConfirmStateService.reset();
		expect(confirmState.visible).toBe(false);
		expect(confirmState.title).toBeNull();
		expect(confirmState.texts).toEqual([]);
	}));
});
