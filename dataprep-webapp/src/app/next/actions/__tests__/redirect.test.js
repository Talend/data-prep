import actions from '../redirect';


describe('Redirect action', () => {
	it('should create a CMF redirection action', () => {
		const event = {};
		const data = {
			action: { path: '/my/resource/$id/history' },
			model: { id: 'myModelId' },
		};
		const action = actions.redirect(event, data);

		expect(action).toMatchSnapshot();
	});
});
