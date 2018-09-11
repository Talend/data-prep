import Constants from '@talend/react-containers/lib/AboutDialog/AboutDialog.constant';
import { IMMUTABLE_SETTINGS } from './help.effects.mock';
import * as effects from '../../effects/help.effects';


describe('help', () => {
	describe('open', () => {
		it('should update also fetch versions if they are not already present in the store', () => {
			const gen = effects.open();
			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_SETTINGS).value.PUT.action;
			expect(effect.type).toEqual(Constants.ABOUT_DIALOG_SHOW);
			expect(effect.url).toEqual('/api/version');

			expect(gen.next().done).toBeTruthy();
		});
	});
});
