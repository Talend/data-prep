import { call, take } from 'redux-saga/effects';
import {
	OPEN_WITH_BUTTON_CLICKED,
} from '@talend/dataset/lib/app/components/SampleView/DatasetDetailSubHeaderActions/DatasetDetailSubHeaderActions.constants';
import sagas from '../preparation.saga';
import * as effects from '../../effects/preparation.effects';
import * as actions from '../../../constants/actions';

describe('preparation', () => {
	describe('fetch', () => {
		it('should wait for FETCH_PREPARATIONS action and call fetch', () => {
			const gen = sagas['preparation:fetch']();
			const action = {
				payload: { folderId: 'folderId' },
			};

			expect(gen.next().value).toEqual(take(actions.FETCH_PREPARATIONS));
			expect(gen.next(action).value).toEqual(call(effects.refresh, action.payload));
			expect(gen.next().value).toEqual(take(actions.FETCH_PREPARATIONS));
		});
	});

	describe('create', () => {
		it('should wait for CREATE_PREPARATIONS action and call create', () => {
			const gen = sagas['preparation:create']();
			const action = {
				payload: { folderId: 'folderId' },
			};

			expect(gen.next().value).toEqual(take(actions.CREATE_PREPARATIONS));
			expect(gen.next(action).value).toEqual(call(effects.create, action.payload));
			expect(gen.next().value).toEqual(take(actions.CREATE_PREPARATIONS));
		});
	});

	describe('openWith', () => {
		it('should wait for OPEN_WITH_BUTTON_CLICKED action and call create', () => {
			const gen = sagas['preparation:openWith']();
			const action = {
				datasetId: 'datasetId',
			};

			expect(gen.next().value).toEqual(take(OPEN_WITH_BUTTON_CLICKED));
			expect(gen.next(action).value).toEqual(call(effects.create, action));
			expect(gen.next().value).toEqual(take(OPEN_WITH_BUTTON_CLICKED));
		});
	});
});
