import home from './home';

describe('My First Test', () => {
	beforeEach(() => {
		cy.visit('http://localhost:3000/');
	});

	it('Can play with header bar', () => {
		home.headerbar.getLogo().click();
		home.headerbar.getBrand().click();
		home.headerbar.getBrand().click();
		home.headerbar.getHelp().focus();
		home.headerbar.getInfo().click();
	});

	it('Can play with side panel', () => {
		home.sidepanel.getToggle().click();
		home.sidepanel.getPreparations().click();
		home.sidepanel.getDatasets().click();
		home.sidepanel.getConnections().click();
	});

	it('Can play with preparation list', () => {
		// home.preparations.getAddFolder().click();
		home.preparations.getAddPreparation().click();
		home.modal.getFooterButtons().click();
	});
});
