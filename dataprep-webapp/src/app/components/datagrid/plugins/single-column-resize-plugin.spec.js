import $ from 'jquery';
import SingleColumnResizePlugin from './single-column-resize-plugin';
import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Single column resize plugin', () => {
	let grid;
	let columns;
	let handler;
	let node;

	beforeEach(() => {
		jasmine.clock().install();

		// fake grid mock
		grid = new SlickGridMock();
		spyOn(grid.onHeaderCellRendered, 'subscribe').and.returnValue();

		// insert columns in grid
		columns = [
			{ id: 0, name: 'col0', resizable: false },
			{ id: 1, name: 'col1', resizable: true },
			{ id: 2, name: 'col2', resizable: true },
			{ id: 3, name: 'col3', resizable: true },
		];
		grid.setColumns(columns);

		// header node and resize handler
		handler = $('<div class="slick-resizable-handle"></div>');
		node = $('<div></div>');
		node.append(handler);
	});

	afterEach(() => {
		jasmine.clock().uninstall();
	});

	it('should attach onHeaderCellRendered listener', () => {
		// when
		SingleColumnResizePlugin.patch(grid);

		// then
		expect(grid.onHeaderCellRendered.subscribe).toHaveBeenCalled();
	});

	it('should save columns "resizable" original values on dragstart', () => {
		// given
		SingleColumnResizePlugin.patch(grid);
		const column = columns[2];

		const onHeaderCellRendered = grid.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
		onHeaderCellRendered(null, { column, node });
		jasmine.clock().tick(10);

		// when
		handler.trigger('dragstart');

		// then
		expect(columns[0].originalResizable).toBe(false);
		expect(columns[0].resizable).toBe(false);
		expect(columns[1].originalResizable).toBe(true);
		expect(columns[1].resizable).toBe(false);
		expect(columns[2].originalResizable).toBe(undefined);
		expect(columns[2].resizable).toBe(true);
		expect(columns[3].originalResizable).toBe(undefined);
		expect(columns[3].resizable).toBe(true);
	});

	it('should restore columns "resizable" original values on dragend', () => {
		// given
		SingleColumnResizePlugin.patch(grid);
		const column = columns[2];

		const onHeaderCellRendered = grid.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
		onHeaderCellRendered(null, { column, node });
		jasmine.clock().tick(10);

		columns[0].originalResizable = false;
		columns[0].resizable = false;
		columns[1].originalResizable = true;
		columns[1].resizable = false;

		// when
		handler.trigger('dragend');

		// then
		expect(columns[0].originalResizable).toBe(undefined);
		expect(columns[0].resizable).toBe(false);
		expect(columns[1].originalResizable).toBe(undefined);
		expect(columns[1].resizable).toBe(true);
	});
});
