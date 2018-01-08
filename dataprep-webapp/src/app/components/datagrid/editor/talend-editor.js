/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/*eslint-disable */

export default function TalendEditor(validationFn, translatedMsg) {
    return function TalendEditorClosure(args) {
        var $container;
        var $input;
        var $checkboxContainer;
        var $checkbox;
        var defaultValue;
        var previousValue;
        var currentValue;
        var inputLineHeight;

        function updateRowsNb() {
            currentValue = $input.val();
            if (currentValue === previousValue) {
                return;
            }

            $input.attr('rows', 1);
            inputLineHeight = inputLineHeight || parseInt($input.css('lineHeight'), 10);
            var lines = parseInt($input.prop('scrollHeight') / inputLineHeight, 10);
            $input.attr('rows', lines);

            previousValue = currentValue;
        }

        function init() {
            $container = $('<div></div>')
                .appendTo(args.container);
            $input = $('<textarea class="form-control" hidefocus rows="1"></textarea>')
                .appendTo($container)
                .keydown(function (e) {
                    switch (e.keyCode) {
                        case $.ui.keyCode.ESCAPE:
                            $input.val(defaultValue);
                            args.cancelChanges();
                            break;

                        case $.ui.keyCode.ENTER:
                            if (e.altKey) {
                                var value = $input.val() + '\n';
                                $input.val(value);
                            }
                            else {
                                args.commitChanges();
                            }

                            break;
                    }

                    e.stopImmediatePropagation();
                    updateRowsNb();
                })
                .keyup(function (e) {
                    if (e.keyCode === $.ui.keyCode.BACKSPACE || e.keyCode === $.ui.keyCode.DELETE) {
                        updateRowsNb();
                    }
                })
                .mouseenter(function (e) {
                    e.stopImmediatePropagation();
                })
                .focus()
                .select();

            $checkboxContainer = $('<div class="checkboxContainer"><form><label><input type="checkbox">' + translatedMsg + '</label></form></div>')
                .appendTo($container);
            $checkbox = $checkboxContainer.find('input[type="checkbox"]');
        }

        /*********** REQUIRED METHODS ***********/

        this.destroy = function () {
            // remove all data, events & dom elements created in the constructor
            $input.remove();
        };

        this.focus = function () {
            // set the focus on the main input control (if any)
            $input.focus();
        };

        this.isValueChanged = function () {
            // return true if the value(s) being edited by the user has/have been changed
            return (!($input.val() === '' && defaultValue === null)) && ($input.val() !== defaultValue);
        };

        this.serializeValue = function () {
            // return the value(s) being edited by the user in a serialized form
            // can be an arbitrary object
            // the only restriction is that it must be a simple object that can be passed around even
            // when the editor itself has been destroyed
            return $input.val();
        };

        this.loadValue = function (item) {
            // load the value(s) from the data item and update the UI
            // this method will be called immediately after the editor is initialized
            // it may also be called by the grid if if the row/cell being edited is updated via grid.updateRow/updateCell
            $input.val(defaultValue = item[args.column.field]);
            updateRowsNb();
            $input.select();
        };

        this.applyValue = function (item, state) {
            // deserialize the value(s) saved to 'state' and apply them to the data item
            // this method may get called after the editor itself has been destroyed
            // treat it as an equivalent of a Java/C# 'static' method - no instance variables should be accessed
            validationFn(item, args.column.tdpColMetadata, state, $checkbox[0].checked);
        };

        this.validate = function () {
            // validate user input and return the result along with the validation message, if any
            // if the input is valid, return {valid:true,msg:null}
            return { valid: true, msg: null };
        };

        /*********** OPTIONAL METHODS ***********/

        this.position = function (cellBox) {
            // if implemented, this will be called by the grid if any of the cell containers are scrolled
            // and the absolute position of the edited cell is changed
            // if your UI is constructed as a child of document BODY, implement this to update the
            // position of the elements as the position of the cell changes
            //
            // the cellBox: { top, left, bottom, right, width, height, visible }
            if (args.gridPosition.bottom - cellBox.bottom < 50) {
                $container.addClass('bottom');
            }
            else {
                $container.removeClass('bottom');
            }

            if (cellBox.left + $input.width() > args.gridPosition.right) {
                $container.css('right', 0);
                $container.css('position', 'absolute');
            }
        };

        /*********** Initialization ***********/

        init();
    };
}

/*eslint-enable */
