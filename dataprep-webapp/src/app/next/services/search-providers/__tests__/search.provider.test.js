import SearchProvider from '../search.provider';


describe('SearchProvider', () => {
	it('should throw if build is called on the mother class', () => {
		const provider = new SearchProvider();
		expect(() => {
			provider.build();
		}).toThrowError();
	});

	it('should throw if transform is called on the mother class', () => {
		const provider = new SearchProvider();
		expect(() => {
			provider.transform();
		}).toThrowError();
	});
});
