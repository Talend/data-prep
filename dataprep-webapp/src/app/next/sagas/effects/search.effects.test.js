import { put } from 'redux-saga/effects';
import * as effects from './search.effects';
import { TDP_REDIRECT_WINDOW, OPEN_WINDOW } from '../../constants/actions';
import { IMMUTABLE_STATE, STATE } from './search.effects.mock';
import { default as creators } from '../../actions';



describe('Search', () => {
	describe('reset', () => {
		it('should reset the search collection', () => {
			const gen = effects.reset();
			const effect = gen.next().value;

			expect(effect.PUT.action.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.PUT.action.collectionId).toBe('search');
			expect(effect.PUT.action.data).toBe(null);
		});
	});

	describe('goto', () => {
		it('should handle preparation type', () => {
			const gen = effects.goto({ sectionIndex: 0, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put(creators.preparation.open(null, STATE[0].suggestions[0]))
			);
		});

		it('should handle dataset type', () => {
			const gen = effects.goto({ sectionIndex: 1, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put(creators.dataset.open(null, STATE[1].suggestions[0]))
			);
		});

		it('should handle folder type', () => {
			const gen = effects.goto({ sectionIndex: 2, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put(creators.folder.open(null, STATE[2].suggestions[0]))
			);
		});

		it('should handle documentation type', () => {
			const gen = effects.goto({ sectionIndex: 3, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put({
					type: OPEN_WINDOW,
					payload: { url: STATE[3].suggestions[0].url },
				}
			));
		});
	});
});
