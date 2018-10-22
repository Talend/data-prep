import actions from '../folder';


describe('Folder action', () => {
	it('should create an open action', () => {
		const event = {};
		const payload = {
			id: 42,
		};
		const action = actions.open(event, payload);

		expect(action).toMatchSnapshot();
	});

	it('should create an add action', () => {
		const action = actions.add();
		expect(action).toMatchSnapshot();
	});

	it('should create an openAddFolderModal action', () => {
		const action = actions.openAddFolderModal();
		expect(action).toMatchSnapshot();
	});

	it('should create an closeAddFolderModal action', () => {
		const action = actions.closeAddFolderModal();
		expect(action).toMatchSnapshot();
	});

	it('should create an remove action', () => {
		const action = actions.remove();
		expect(action).toMatchSnapshot();
	});

	it('should create an openRemoveFolderModal action', () => {
		const action = actions.openRemoveFolderModal(null, {
			model: { name: 'test' },
		});
		expect(action).toMatchSnapshot();
	});

	it('should create an closeRemoveFolderModal action', () => {
		const action = actions.closeRemoveFolderModal();
		expect(action).toMatchSnapshot();
	});
});
