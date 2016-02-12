/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:MessageService
 * @description Display message toasts
 */
export default function MessageService($translate, toaster) {
    'ngInject';

    /**
     * @ngdoc method
     * @name pop
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} type - the message type
     * @param {string} titleKey - the message title key (transformed by internationalization)
     * @param {string} contentKey - the message content key (transformed by internationalization)
     * @param {string} args - the message (title and content) arguments used by internationalization to replace vars
     * @param {number} timeout - the dismiss delay in ms. 0 to disable automatic dismiss
     * @description [PRIVATE] Display a toast
     */
    var pop = function (type, titleKey, contentKey, args, timeout) {
        $translate([titleKey, contentKey], args)
            .then(function (translations) {
                toaster.pop(type, translations[titleKey], translations[contentKey], timeout);
            });
    };

    /**
     * @ngdoc method
     * @name error
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} titleKey - the message title key (transformed by internationalization)
     * @param {string} contentKey - the message content key (transformed by internationalization)
     * @param {string} args - the message (title and content) arguments used by internationalization to replace vars
     * @description Display an error toast. Automatic dismiss is disabled
     */
    this.error = function (titleKey, contentKey, args) {
        pop('error', titleKey, contentKey, args, 0);
    };

    /**
     * @ngdoc method
     * @name success
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} titleKey - the message title key (transformed by internationalization)
     * @param {string} contentKey - the message content key (transformed by internationalization)
     * @param {string} args - the message (title and content) arguments used by internationalization to replace vars
     * @description Display a success toast. The toast disappear after 5000ms
     */
    this.success = function (titleKey, contentKey, args) {
        pop('success', titleKey, contentKey, args, 5000);
    };

    /**
     * @ngdoc method
     * @name warning
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} titleKey - the message title key (transformed by internationalization)
     * @param {string} contentKey - the message content key (transformed by internationalization)
     * @param {string} args - the message (title and content) arguments used by internationalization to replace vars
     * @description Display a warning toast. Automatic dismiss is disabled
     */
    this.warning = function (titleKey, contentKey, args) {
        pop('warning', titleKey, contentKey, args, 0);
    };
}