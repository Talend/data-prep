import { HTTP_STATUS } from '@talend/react-cmf/lib/middlewares/http/constants';
import * as effects from '../../effects/http.effects';
import { REDIRECT_WINDOW, ERROR_NOTIFICATION } from '../../../constants/actions';
import { getPayload, IMMUTABLE_STATE, STATE } from './http.effects.mock';

jest.mock('@talend/react-cmf/lib/middlewares/http/constants', () => ({
	HTTP_STATUS: {
		UNAUTHORIZED: 'UNAUTHORIZED',
		FORBIDDEN: 'FORBIDDEN',
		NOT_FOUND: 'NOT_FOUND',
		GATEWAY_TIMEOUT: 'GATEWAY_TIMEOUT',
		INTERNAL_SERVER_ERROR: 'INTERNAL_SERVER_ERROR',
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

		it('should handle GATEWAY_TIMEOUT status', () => {
			const gen = effects.handle(getPayload(HTTP_STATUS.GATEWAY_TIMEOUT));
			const effect = gen.next().value.PUT.action;

			expect(effect.type).toBe(ERROR_NOTIFICATION);
			expect(effect.payload.title).toBe('An error has occurred');
			expect(effect.payload.message).toBe('Service unavailable');

			expect(gen.next().done).toBeTruthy();
		});

		it('should handle INTERNAL_SERVER_ERROR status', () => {
			const gen = effects.handle(getPayload(HTTP_STATUS.INTERNAL_SERVER_ERROR));
			const effect = gen.next().value.PUT.action;

			expect(effect.type).toBe(ERROR_NOTIFICATION);
			expect(effect.payload.title).toBe('An error has occurred');
			expect(effect.payload.message).toBe('Sorry an unexpected error occurred and we could not complete your last operation, but you can keep using Data Preparation.');

			expect(gen.next().done).toBeTruthy();
		});
	});
});
