import { List } from 'immutable';
import * as effects from '../notification.effects';

describe('Notification', () => {
	describe('success', () => {
		it('should add success notification', () => {
			const action = {
				payload: {
					title: 'success',
					message: 'hi',
				},
			};
			const gen = effects.success(action);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(new List([])).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual(effects.COMPONENT_KEY);
			expect(effect.componentName).toEqual(effects.COMPONENT_NAME);
			expect(effect.componentState.notifications.get(0).title).toEqual('success');
			expect(effect.componentState.notifications.get(0).message).toEqual('hi');

			expect(gen.next().done).toBeTruthy();
		});
	});
	describe('error', () => {
		it('should add error notification', () => {
			const action = {
				payload: {
					title: 'error',
					message: 'hi',
				},
			};
			const gen = effects.success(action);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(new List([])).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual(effects.COMPONENT_KEY);
			expect(effect.componentName).toEqual(effects.COMPONENT_NAME);
			expect(effect.componentState.notifications.get(0).title).toEqual('error');
			expect(effect.componentState.notifications.get(0).message).toEqual('hi');

			expect(gen.next().done).toBeTruthy();
		});
	});
	describe('warning', () => {
		it('should add warning notification', () => {
			const action = {
				payload: {
					title: 'warning',
					message: 'hi',
				},
			};
			const gen = effects.success(action);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(new List([])).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual(effects.COMPONENT_KEY);
			expect(effect.componentName).toEqual(effects.COMPONENT_NAME);
			expect(effect.componentState.notifications.get(0).title).toEqual('warning');
			expect(effect.componentState.notifications.get(0).message).toEqual('hi');

			expect(gen.next().done).toBeTruthy();
		});
	});
});
