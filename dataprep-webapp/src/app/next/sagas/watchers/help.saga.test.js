import { call, take } from 'redux-saga/effects';
import sagas from './help.saga';
import * as effects from '../effects/help.effects';
import { OPEN_ABOUT } from '../../constants/actions';

describe('help', () => {
	describe('open', () => {
		const gen = sagas['about:open']();
		it('should wait for OPEN_ABOUT action', () => {
			expect(gen.next().value).toEqual(take(OPEN_ABOUT));
		});
		it('should call open effect', () => {
			expect(gen.next().value).toEqual(call(effects.open));
		});
	});
});
