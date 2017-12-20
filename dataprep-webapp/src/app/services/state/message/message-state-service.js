/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


export const messageState = {
	messages: [],
};

export class MessageStateService {
	push(message) {
		messageState.messages = [...messageState.messages, {
			...message,
			id: Math.random(),
		}];
	}

	pop(message) {
		const index = messageState.messages.findIndex(item => item.id === message.id);
		if (index > -1) {
			const copy = [...messageState.messages];
			copy.splice(index, 1);
			messageState.messages = copy;
		}
	}
}
