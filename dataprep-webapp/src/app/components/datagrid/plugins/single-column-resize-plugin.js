import $ from 'jquery';

function freezeOtherColumnsOnDrag(columns, { column, node }) {
	const currentColumnIndex = columns.indexOf(column);

	setTimeout(() => {
		$(node).find('.slick-resizable-handle')
			.eq(0)
			.bind('dragstart', () => {
				for (let i = 0; i < currentColumnIndex; i++) {
					const col = columns[i];
					col.originalResizable = col.resizable;
					col.resizable = false;
				}
			})
			.bind('dragend', () => {
				for (let i = 0; i < currentColumnIndex; i++) {
					const col = columns[i];
					col.resizable = col.originalResizable;
					delete col.originalResizable;
				}
			});
	}, 0);
}

export default {
	patch(grid) {
		grid.onHeaderCellRendered.subscribe(
			(event, columnsArgs) => freezeOtherColumnsOnDrag(grid.getColumns(), columnsArgs)
		);
	},
};
