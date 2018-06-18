import PreparationService from '../preparation.service';
import {
	RAW_FOLDERS,
	RAW_PREPARATIONS,
	FORMATTED_PREPARATIONS,
	FORMATTED_FOLDERS,
} from './preparation.service.mock';


describe('PreparationService', () => {
	it('should transform preparations', () => {
		expect(PreparationService.transform({ preparations: RAW_PREPARATIONS })).toEqual(
			FORMATTED_PREPARATIONS,
		);
	});

	it('should transform folders', () => {
		expect(PreparationService.transform({ folders: RAW_FOLDERS })).toEqual(
			FORMATTED_FOLDERS,
		);
	});

	it('should concatenate preparations and folders', () => {
		expect(
			PreparationService.transform({ preparations: RAW_PREPARATIONS, folders: RAW_FOLDERS }),
		).toEqual([...FORMATTED_FOLDERS, ...FORMATTED_PREPARATIONS]);
	});
});
