/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const parametersState = {
	visible: false,
	isSending: false,
	configuration: {
		encodings: [],
		textEnclosureCharacter: '\"',
		escapeCharacter: '\"',
		separators: [
            { label: ';', value: ';' },
            { label: ',', value: ',' },
            { label: '<space>', value: ' ' },
            { label: '<tab>', value: '\t' },
            { label: '|', value: '|' },
		],
	},
	values: {
		separator: null,
		encoding: null,
		textEnclosureCharacter: null,
		escapeCharacter: null,
	},
};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:ParameterService
 * @description Parameters service.
 */
export function ParametersStateService() {
	return {
		show,
		hide,
		setIsSending,
		setEncodings,
		update,
		reset,
	};

    /**
     * @ngdoc method
     * @name show
     * @methodOf data-prep.services.state.service:ParameterService
     * @description Set the parameters visible
     */
	function show() {
		parametersState.visible = true;
	}

    /**
     * @ngdoc method
     * @name hide
     * @methodOf data-prep.services.state.service:ParameterService
     * @description Set the parameters visible
     */
	function hide() {
		parametersState.visible = false;
	}

    /**
     * @ngdoc method
     * @name setIsSending
     * @methodOf data-prep.services.state.service:ParameterService
     * @param {boolean} sending True if the app is sending new parameters
     * @description Set the parameters sending flag
     */
	function setIsSending(sending) {
		parametersState.isSending = sending;
	}

    /**
     * @ngdoc method
     * @name setSupportedEncodings
     * @methodOf data-prep.services.state.service:ParameterService
     * @param {array} encodings The array of supported encodings
     * @description Set the array of supported encodings
     */
	function setEncodings(encodings) {
		parametersState.configuration.encodings = encodings;
	}

    /**
     * @ngdoc method
     * @name update
     * @methodOf data-prep.services.state.service:ParameterService
     * @param {object} dataset The dataset where to extract parameters
     * @description Set the lookup visible
     */
	function update(dataset) {
		parametersState.values.separator = dataset.parameters.SEPARATOR;
		parametersState.values.escapeCharacter = dataset.parameters.ESCAPE_CHAR;
		parametersState.values.textEnclosureCharacter = dataset.parameters.TEXT_ENCLOSURE_CHAR;
		parametersState.values.encoding = dataset.encoding;
	}

    /**
     * @ngdoc method
     * @name reset
     * @methodOf data-prep.services.state.service:ParameterService
     * @description Reset the parameters internal state
     */
	function reset() {
		parametersState.visible = false;
		parametersState.isSending = false;
		parametersState.values = {
			separator: null,
			encoding: null,
			escapeCharacter: null,
			textEnclosureCharacter: null,
		};
	}
}
