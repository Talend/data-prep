import { take, call } from 'redux-saga/effects';
import { OPEN_ABOUT } from '../../constants/actions';
import * as effects from '../effects/help.effects';


function* open() {
	while (true) {
		yield take(OPEN_ABOUT);
		yield call(effects.open);
	}
}

export default {
	'about:open': open,
};
