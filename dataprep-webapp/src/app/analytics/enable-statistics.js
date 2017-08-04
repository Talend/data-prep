import pendo from './pendo';

export default function (config) {
	pendo();

	window.pendo.initialize({
		visitor: { id: config.analyticsVisitor },
		account: { id: config.analyticsAccount },
	});
}
