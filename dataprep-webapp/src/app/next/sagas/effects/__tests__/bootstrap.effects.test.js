import store from 'store';
import * as effects from '../../effects/bootstrap.effects';

import i18next from '../../../../i18n';

describe('bootstrap', () => {
	describe('fetch', () => {
		it('should update cmf store', () => {
			store.set('settings', { actions: [], uris: { preparation: 'api/preparation' } });
			const gen = effects.fetch();
			const expected = store.get('settings');
			expect(gen.next().value).toEqual(expected);
			const effect = gen.next(expected).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('settings');
			expect(effect.data).toEqual(expected);

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('setLanguage', () => {
		beforeEach(() => {
			i18next.changeLanguage = jest.fn();
		});

		it('should change language', () => {
			store.set('settings', { context: { language: 'fr' } });
			effects.setLanguage();
			expect(i18next.changeLanguage).toHaveBeenCalledWith('fr');
		});
	});
});
