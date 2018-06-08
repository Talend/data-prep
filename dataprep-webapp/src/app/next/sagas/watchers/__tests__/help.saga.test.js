import { call, take } from 'redux-saga/effects';
import sagas from '../help.saga';
import * as effects from '../../effects/help.effects';
import { OPEN_ABOUT } from '../../../constants/actions';


describe('help', () => {
	describe('open', () => {
		it('should wait for OPEN_ABOUT action', () => {
			const gen = sagas['about:open']();

			expect(gen.next().value).toEqual(take(OPEN_ABOUT));
			expect(gen.next().value).toEqual(call(effects.open));
		});
	});
});
