import { cmfConnect } from '@talend/react-cmf';
import OpenWithButton from './OpenWithButton.component';

export default cmfConnect({
	componentId: 'default',
})(OpenWithButton);
