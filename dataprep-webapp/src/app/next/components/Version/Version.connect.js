import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import Version from './Version.component';

export default cmfConnect({
	defaultState: new Map({ versions: {} }),
})(Version);
