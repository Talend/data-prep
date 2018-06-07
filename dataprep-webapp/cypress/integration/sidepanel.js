function getToggle() {
	return cy.get('.tc-side-panel-toggle-btn .btn');
}
function getPreparations() {
	return cy.get('#menu\\:preparations');
}
function getDatasets() {
	return cy.get('#menu\\:datasets');
}
function getConnections() {
	return cy.get('#menu\\:datastores');
}

export default {
	getToggle,
	getPreparations,
	getDatasets,
	getConnections,
};
