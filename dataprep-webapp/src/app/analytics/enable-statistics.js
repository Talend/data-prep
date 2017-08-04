import pendo from './pendo';
import mixpanel from './mixpanel';

export default function (config) {
	pendo();
	mixpanel();

	window.pendo.initialize({
		visitor: { id: config.analyticsVisitor },
		account: { id: config.analyticsAccount },
	});
}
