import PreparationService from '../preparation.service';
import {
	RAW_FOLDERS,
	RAW_PREPARATIONS,
	FORMATTED_PREPARATIONS,
	FORMATTED_FOLDERS,
	RAW_FOLDERS_TREE,
	FORMATTED_FOLDERS_TREE,
	RAW_FOLDERS_HIERARCHY,
	FORMATTED_FOLDERS_HIERARCHY,
} from './preparation.service.mock';

describe('PreparationService', () => {
	it('should transform preparations', () => {
		expect(PreparationService.transform({ preparations: RAW_PREPARATIONS })).toEqual(
			FORMATTED_PREPARATIONS,
		);
	});

	it('should transform folders', () => {
		expect(PreparationService.transform({ folders: RAW_FOLDERS })).toEqual(FORMATTED_FOLDERS);
	});

	it('should concatenate preparations and folders', () => {
		expect(
			PreparationService.transform({ preparations: RAW_PREPARATIONS, folders: RAW_FOLDERS }),
		).toEqual([...FORMATTED_FOLDERS, ...FORMATTED_PREPARATIONS]);
	});

	it('should transform folders tree', () => {
		expect(PreparationService.transformTree(RAW_FOLDERS_TREE)).toEqual(FORMATTED_FOLDERS_TREE);
	});
	it('should transform folders', () => {
		expect(PreparationService.transformFolder(RAW_FOLDERS_HIERARCHY)).toEqual(
			FORMATTED_FOLDERS_HIERARCHY,
		);
	});
});
