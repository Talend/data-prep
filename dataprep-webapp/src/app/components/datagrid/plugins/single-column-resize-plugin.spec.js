import $ from 'jquery';
import SingleColumnResizePlugin from './single-column-resize-plugin';
import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Single column resize plugin', () => {
	beforeEach(() => {
		jasmine.clock().install();
	});

	afterEach(() => {
		jasmine.clock().uninstall();
	});

	it('should attach onHeaderCellRendered listener', () => {
		// given
		const grid = new SlickGridMock();
		spyOn(grid.onHeaderCellRendered, 'subscribe').and.returnValue();

		// when
		SingleColumnResizePlugin.apply(grid);

		// then
		expect(grid.onHeaderCellRendered.subscribe).toHaveBeenCalled();
	});

	it('should save columns "resizable" original values on dragstart', () => {
		// given
		const grid = new SlickGridMock();
		spyOn(grid.onHeaderCellRendered, 'subscribe').and.returnValue();
		SingleColumnResizePlugin.apply(grid);
		const onHeaderCellRendered = grid.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];

		const columns = [
			{ id: 0, name: 'col0', resizable: false },
			{ id: 1, name: 'col1', resizable: true },
			{ id: 2, name: 'col2', resizable: true },
			{ id: 3, name: 'col3', resizable: true },
		];
		grid.setColumns(columns);
		const column = columns[2];
		const handler = $('<div class="slick-resizable-handle"></div>');
		const node = $('<div></div>');
		node.append(handler);

		// when
		onHeaderCellRendered(null, { column, node });
		handler.trigger('dragstart');
		jasmine.clock().tick(10);

		// then
		console.log(columns[0])
	});

	it('should restore columns "resizable" original values on dragend', () => {
		// given

		// when

		// then
	});
});
