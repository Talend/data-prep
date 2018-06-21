import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

// https://github.com/facebook/jest/issues/890#issuecomment-209698782
Object.defineProperty(window.location, 'origin', {
	writable: true,
	value: 'http://dataprep.com',
});
