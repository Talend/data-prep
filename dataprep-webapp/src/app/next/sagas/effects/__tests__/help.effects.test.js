import * as effects from '../../effects/help.effects';

describe('help', () => {
	describe('open', () => {
		it('should update AboutModal store', () => {
			const gen = effects.open();
			const effect = gen.next().value.PUT.action;

			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.componentName).toBe('AboutModal');
			expect(effect.componentState).toEqual({ show: true });

			expect(gen.next().done).toBeTruthy();
		});
	});
});
