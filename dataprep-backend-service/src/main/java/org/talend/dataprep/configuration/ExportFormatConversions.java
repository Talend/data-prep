// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.configuration;

import static org.talend.dataprep.conversions.BeanConversionService.fromBean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;

@Component
public class ExportFormatConversions extends BeanConversionServiceWrapper {

    @Override
    public BeanConversionService doWith(BeanConversionService conversionService, String beanName,
            ApplicationContext applicationContext) {
        final MessagesBundle messagesBundle = applicationContext.getBean(MessagesBundle.class);
        conversionService.register(fromBean(ExportFormat.class) //
                .toBeans(ExportFormatMessage.class) //
                .using(ExportFormatMessage.class, (exportFormat, exportFormatMessage) -> {
                    final String code = "export." + exportFormat.getName() + ".display";
                    final String displayName = messagesBundle.getString(LocaleContextHolder.getLocale(), code);
                    exportFormatMessage.setId(exportFormat.getName());
                    exportFormatMessage.setName(displayName);
                    exportFormatMessage.setSupportSampling(exportFormat.supportSampling());
                    return exportFormatMessage;
                }) //
                .build());
        return conversionService;
    }

}
