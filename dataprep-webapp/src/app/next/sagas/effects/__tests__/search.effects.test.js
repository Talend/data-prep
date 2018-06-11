import api from '@talend/react-cmf';
import { put, all, call } from 'redux-saga/effects';
import * as effects from '../search.effects';
import { OPEN_WINDOW } from '../../../constants/actions';
import { default as creators } from '../../../actions';
import { IMMUTABLE_STATE, STATE, PROVIDERS } from './search.effects.mock';


jest.mock('../../../services/search.service', () => {
	return () => ({
		build: (_, payload) => {
			return [jest.fn, payload];
		},
		transform: () => [
			{
				title: 'test',
				suggestions: [
					{ title: 'test' },
				],
			},
		],
	});
});

describe('Search', () => {
	describe('reset', () => {
		it('should reset the search collection', () => {
			const gen = effects.reset();
			const effect = gen.next().value;

			expect(effect.PUT.action.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.PUT.action.collectionId).toBe('search');
			expect(effect.PUT.action.data).toBe(null);

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('goto', () => {
		it('should handle preparation type', () => {
			const gen = effects.goto({ sectionIndex: 0, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put(creators.preparation.open(null, STATE[0].suggestions[0])),
			);

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle dataset type', () => {
			const gen = effects.goto({ sectionIndex: 1, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put(creators.dataset.open(null, STATE[1].suggestions[0])),
			);

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle folder type', () => {
			const gen = effects.goto({ sectionIndex: 2, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put(creators.folder.open(null, STATE[2].suggestions[0])),
			);

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle documentation type', () => {
			const gen = effects.goto({ sectionIndex: 3, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_STATE).value).toEqual(
				put({
					type: OPEN_WINDOW,
					payload: { url: STATE[3].suggestions[0].url },
				}),
			);

			expect(gen.next().done).toBeTruthy();
		});

		it('should do nothing if type is unknown', () => {
			const gen = effects.goto({ sectionIndex: 4, itemIndex: 0 });

			expect(gen.next().value.SELECT).toBeDefined();

			expect(gen.next(IMMUTABLE_STATE).done).toBeTruthy();
		});
	});

	describe('search', () => {
		const expected = [
			{
				title: 'test',
				suggestions: [
					{ title: 'test' },
				],
			},
			{
				title: 'test',
				suggestions: [
					{ title: 'test' },
				],
			},
		];

		beforeEach(() => {
			api.registry.getFromRegistry = jest.fn(() => PROVIDERS);
		});

		it('should start the research', () => {
			let effect;

			const gen = effects.search('payload');
			effect = gen.next().value.PUT.action.cmf.componentState;

			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('headerbar:search');
			expect(effect.componentName).toEqual('Container(Typeahead)');
			expect(effect.componentState).toEqual({ searching: true });

			expect(gen.next().value).toEqual(
				all([
					call(jest.fn, 'payload'),
					call(jest.fn, 'payload'),
				])
			);

			effect = gen.next(expected).value.PUT.action.cmf.componentState;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('headerbar:search');
			expect(effect.componentName).toEqual('Container(Typeahead)');
			expect(effect.componentState).toEqual({ searching: false });

			effect = gen.next().value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('search');
			expect(effect.data).toEqual(expected);

			expect(gen.next().done).toBeTruthy();
		});
	});
});
