function getLogo() {
	return cy.get('.tc-header-bar-logo');
}
function getBrand() {
	return cy.get('.tc-header-bar-brand');
}
function getHelp() {
	return cy.get('#header-help');
}
function getInfo() {
	return cy.get('#headerbar\\:information');
}

export default {
	getLogo,
	getBrand,
	getHelp,
	getInfo,
};
