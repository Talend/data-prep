import actions from '../preparation';

jest.mock('@talend/react-cmf/lib/sagaRouter', () => {
	return () => ({
		matchPath: () => {
			return 'yo';
		},
	});
});


describe('Preparation action', () => {
	describe('open', () => {
		it('should create an open folder action', () => {
			const event = {};
			const payload = {
				type: 'folder',
				id: 42,
			};
			const action = actions.open(event, payload);

			expect(action).toMatchSnapshot();
		});

		it('should create an open preparation action', () => {
			const event = {};
			const payload = {
				type: 'preparation',
				id: 42,
			};
			const action = actions.open(event, payload);

			expect(action).toMatchSnapshot();
		});

		it('should not create anything if type is unknown', () => {
			const event = {};
			const payload = {
				type: 'nop',
			};
			const action = actions.open(event, payload);

			expect(action).toMatchSnapshot();
		});
	});

	it('should create a fetch action', () => {
		const payload = {
			folderId: 42,
		};
		const action = actions.fetch(payload);

		expect(action).toMatchSnapshot();
	});

	it('should create a rename action', () => {
		const event = {};
		const payload = {
			vale: 'name',
			model: {
				id: 42,
			},
		};
		const action = actions.rename(event, payload);

		expect(action).toMatchSnapshot();
	});

	it('should create a cancel rename action', () => {
		const event = {};
		const payload = {
			id: 42,
		};
		const action = actions.cancelRename(event, payload);

		expect(action).toMatchSnapshot();
	});

	it('should create a set title edition mode action', () => {
		const event = {};
		const payload = {
			model: {
				id: 42,
			},
		};
		const action = actions.setTitleEditionMode(event, payload);

		expect(action).toMatchSnapshot();
	});

	it('should create an open creator action', () => {
		const action = actions.openPreparationCreatorModal();

		expect(action).toMatchSnapshot();
	});

	it('should create a preparation remove action', () => {
		const event = {};
		const payload = {
			model: {
				id: 42,
				type: 'preparation',
			},
		};
		const action = actions.remove(event, payload);

		expect(action).toMatchSnapshot();
	});

	it('should create a folder remove action', () => {
		const event = {};
		const payload = {
			model: {
				id: 42,
				type: 'folder',
			},
		};
		const action = actions.remove(event, payload);

		expect(action).toMatchSnapshot();
	});
});
