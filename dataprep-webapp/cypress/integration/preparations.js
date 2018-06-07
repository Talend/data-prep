function getAddPreparation() {
	return cy.get('#preparation\\:add\\:open');
}

function getAddFolder() {
	return cy.get('#folder\\:add\\:open');
}

export default {
	getAddPreparation,
	getAddFolder,
};
