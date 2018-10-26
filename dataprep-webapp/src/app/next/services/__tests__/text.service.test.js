import TextService from '../text.service';

describe('TextService', () => {
	it('should sanitize the given string', () => {
		expect(TextService.sanitize(' y o ')).toEqual('y o');
	});
});
