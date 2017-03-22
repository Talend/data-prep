// fork https://github.com/naresh-n/slickgrid-column-data-autosize

(function ($) {
	$.extend(true, window, {
		Slick: {
			AutoColumnSize,
		},
	});

	function AutoColumnSize(maxWidth) {
		const keyCodes = {
			A: 65,
		};

		let grid;
		let $container;
		let context;

		function init(_grid) {
			grid = _grid;
			maxWidth = maxWidth || 200;

			$container = $(grid.getContainerNode());
			$container.on('dblclick.autosize', '.slick-resizable-handle', reSizeColumn);
			$container.keydown(handleControlKeys);

			context = document.createElement('canvas').getContext('2d');
		}

		function destroy() {
			$container.off();
		}

		function handleControlKeys(event) {
			if (event.ctrlKey && event.shiftKey && event.keyCode === keyCodes.A) {
				resizeAllColumns();
			}
		}

		function resizeAllColumns() {
			const elHeaders = $container.find('.slick-header-column');
			const allColumns = grid.getColumns();
			elHeaders.each(function (index, el) {
				const columnDef = $(el).data('column');
				const headerWidth = getElementWidth(el);
				const colIndex = grid.getColumnIndex(columnDef.id);
				const column = allColumns[colIndex];
				let autoSizeWidth = Math.max(headerWidth, getMaxColumnTextWidth(columnDef, colIndex)) + 1;
				autoSizeWidth = Math.min(maxWidth, autoSizeWidth);
				column.width = autoSizeWidth;
			});
			grid.setColumns(allColumns);
			grid.onColumnsResized.notify();
		}

		function reSizeColumn(e) {
			const headerEl = $(e.currentTarget).closest('.slick-header-column');
			const columnDef = headerEl.data('column');

			if (!columnDef || !columnDef.resizable) {
				return;
			}

			e.preventDefault();
			e.stopPropagation();

			const headerWidth = getElementWidth(headerEl[0]);
			const colIndex = grid.getColumnIndex(columnDef.id);
			const allColumns = grid.getColumns();
			const column = allColumns[colIndex];

			const autoSizeWidth = Math.max(headerWidth, getMaxColumnTextWidth(columnDef, colIndex)) + 1;

			if (autoSizeWidth !== column.width) {
				column.width = autoSizeWidth;
				grid.setColumns(allColumns);
				grid.onColumnsResized.notify();
			}
		}

		function getMaxColumnTextWidth(columnDef, colIndex) {
			const texts = [];
			const rowEl = createRow(columnDef);
			let data = grid.getData();
			if (Slick.Data && data instanceof Slick.Data.DataView) {
				data = data.getItems();
			}
			for (let i = 0; i < data.length; i++) {
				texts.push(data[i][columnDef.field]);
			}
			const template = getMaxTextTemplate(texts, columnDef, colIndex, data, rowEl);
			const width = getTemplateWidth(rowEl, template);
			deleteRow(rowEl);
			return width;
		}

		function getTemplateWidth(rowEl, template) {
			const cell = $(rowEl.find('.slick-cell'));
			cell.append(template);
			$(cell).find('*').css('position', 'relative');
			return $(cell).outerWidth(true) + 1;
		}

		function getMaxTextTemplate(texts, columnDef, colIndex, data, rowEl) {
			let max = 0;
			let maxTemplate = null;
			const formatFun = columnDef.formatter;
			$(texts).each(function (index, text) {
				let template;
				if (formatFun) {
					template = $('<span>' + formatFun(index, colIndex, text, columnDef, data[index]) + '</span>');
					text = template.text() || text;
				}
				const length = text ? getElementWidthUsingCanvas(rowEl, text) : 0;
				if (length > max) {
					max = length;
					maxTemplate = template || text;
				}
			});
			return maxTemplate;
		}

		function createRow(columnDef) { // eslint-disable-line no-unused-vars
			const rowEl = $('<div class="slick-row"><div class="slick-cell"></div></div>');
			rowEl.find('.slick-cell').css({
				visibility: 'hidden',
				'text-overflow': 'initial',
				'white-space': 'nowrap',
			});
			const gridCanvas = $container.find('.grid-canvas');
			$(gridCanvas).append(rowEl);
			return rowEl;
		}

		function deleteRow(rowEl) {
			$(rowEl).remove();
		}

		function getElementWidth(element) {
			const clone = element.cloneNode(true);
			clone.style.cssText = 'position: absolute; visibility: hidden;right: auto;text-overflow: initial;white-space: nowrap;';
			element.parentNode.insertBefore(clone, element);
			const width = clone.offsetWidth;
			clone.parentNode.removeChild(clone);
			return width;
		}

		function getElementWidthUsingCanvas(element, text) {
			context.font = element.css('font-size') + ' ' + element.css('font-family');
			const metrics = context.measureText(text);
			return metrics.width;
		}

		return {
			init,
			destroy,
		};
	}
}(jQuery)); // eslint-disable-line no-undef
