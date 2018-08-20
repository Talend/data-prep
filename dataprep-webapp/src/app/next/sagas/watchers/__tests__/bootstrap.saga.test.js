import { call } from 'redux-saga/effects';
import { bootstrap } from '../bootstrap.saga';
import * as effects from '../../effects/bootstrap.effects';


describe('bootstrap', () => {
	describe('fetch', () => {
		it('should call fetch effect', () => {
			const gen = bootstrap();

			expect(gen.next().value).toEqual(call(effects.bootstrap));

			expect(gen.next().done).toBeTruthy();
		});
	});
});
