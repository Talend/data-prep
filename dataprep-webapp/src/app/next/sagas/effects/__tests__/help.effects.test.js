import { call } from 'redux-saga/effects';
import { VERSIONS, IMMUTABLE_VERSIONS, IMMUTABLE_SETTINGS } from './help.effects.mock';
import http from '../http';
import * as effects from '../../effects/help.effects';


describe('help', () => {
	describe('open', () => {
		it('should update AboutModal store', () => {
			const gen = effects.open();
			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_VERSIONS).value.PUT.action;

			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual('AboutModal');
			expect(effect.componentState).toEqual({ show: true });

			expect(gen.next().done).toBeTruthy();
		});

		it('should update also fetch versions if they are not already present in the store', () => {
			const gen = effects.open();
			expect(gen.next(null).value.SELECT).toBeDefined();

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value).toEqual(
				call(http.get, '/api/version')
			);

			let effect = gen.next(VERSIONS).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('versions');

			effect = gen.next(VERSIONS).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual('AboutModal');
			expect(effect.componentState).toEqual({ show: true });

			expect(gen.next().done).toBeTruthy();
		});
	});
});
