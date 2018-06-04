import SagaTester from 'redux-saga-tester';
import { HTTP_STATUS } from '@talend/react-cmf/lib/middlewares/http/constants';
import httpSagas from './http.saga';

describe('HTTP sagas', () => {
	it('should redirect to 403', () => {
		// given
		const sagaTester = new SagaTester({
			initialState: {},
		});
		sagaTester.start(() => httpSagas());

		// when
		sagaTester.dispatch({
			type: '@@HTTP/ERRORS',
			error: {
				stack: {
					status: HTTP_STATUS.FORBIDDEN,
				},
			},
		});

		// then
		const actions = sagaTester.getCalledActions();
		expect(actions[actions.length - 1]).toEqual({
			type: '@@router/CALL_HISTORY_METHOD',
			payload: {
				method: 'replace',
				args: [`/${HTTP_STATUS.FORBIDDEN}`],
			},
		});
	});

	it('should redirect to 404', () => {
		// given
		const sagaTester = new SagaTester({
			initialState: {},
		});
		sagaTester.start(() => httpSagas());

		// when
		sagaTester.dispatch({
			type: '@@HTTP/ERRORS',
			error: {
				stack: {
					status: HTTP_STATUS.NOT_FOUND,
				},
			},
		});

		// then
		const actions = sagaTester.getCalledActions();
		expect(actions[actions.length - 1]).toEqual({
			type: '@@router/CALL_HISTORY_METHOD',
			payload: {
				method: 'replace',
				args: [`/${HTTP_STATUS.NOT_FOUND}`],
			},
		});
	});
});
