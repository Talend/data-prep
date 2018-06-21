import { HTTP_STATUS } from '@talend/react-cmf/lib/middlewares/http/constants';
import * as effects from '../../effects/http.effects';
import { REDIRECT_WINDOW } from '../../../constants/actions';
import { getPayload, IMMUTABLE_STATE, STATE } from './http.effects.mock';

jest.mock('@talend/react-cmf/lib/middlewares/http/constants', () => ({
	HTTP_STATUS: {
		UNAUTHORIZED: 'UNAUTHORIZED',
		FORBIDDEN: 'FORBIDDEN',
		NOT_FOUND: 'NOT_FOUND',
	},
}));


describe('http', () => {
	describe('handle', () => {
		it('should do nothing if there is no error', () => {
			const gen = effects.handle({ coin: 666 });

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle UNAUTHORIZED status', () => {
			const gen = effects.handle(getPayload(HTTP_STATUS.UNAUTHORIZED));

			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_STATE).value.PUT.action;

			expect(effect.type).toBe(REDIRECT_WINDOW);
			expect(effect.payload).toEqual({ url: STATE.uris.login });

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle FORBIDDEN status', () => {
			const gen = effects.handle(getPayload(HTTP_STATUS.FORBIDDEN));
			const effect = gen.next().value.PUT.action;

			expect(effect.type).toBe('@@router/CALL_HISTORY_METHOD');
			expect(effect.payload.args[0]).toBe('/FORBIDDEN');

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle NOT_FOUND status', () => {
			const gen = effects.handle(getPayload(HTTP_STATUS.NOT_FOUND));
			const effect = gen.next().value.PUT.action;

			expect(effect.type).toBe('@@router/CALL_HISTORY_METHOD');
			expect(effect.payload.args[0]).toBe('/NOT_FOUND');

			expect(gen.next().done).toBeTruthy();
		});
	});
});
