import PreparationService from '../preparation.service';
import {
	RAW_PREPARATIONS,
	SORTED_PREPARATIONS,
} from './preparation.service.mock';

describe('PreparationService', () => {
	it('should sort preparations', () => {
		expect([RAW_PREPARATIONS.sort(PreparationService.sort).toJSON()])
			.toEqual(SORTED_PREPARATIONS);
	});
});
