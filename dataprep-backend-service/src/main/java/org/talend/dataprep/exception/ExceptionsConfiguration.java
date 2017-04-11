//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.exception;

import static org.talend.dataprep.conversions.BeanConversionService.RegistrationBuilder.fromBean;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.processor.BeanConversionServiceWrapper;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class ExceptionsConfiguration {

    @Bean
    public Aspects getAspect() {
        return new Aspects();
    }

    @Component
    public class PersistentPreparationConversions extends BeanConversionServiceWrapper {

        @Override
        public BeanConversionService doWith(BeanConversionService conversionService, String beanName, ApplicationContext applicationContext) {

            conversionService.register(fromBean(TalendRuntimeException.class)
                    .toBeans(TdpExceptionDto.class).using(TdpExceptionDto.class, (internal, dto) -> {
                        ErrorCode errorCode = internal.getCode();
                        String serializedCode = errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode();
                        TdpExceptionDto cause = internal.getCause() instanceof TDPException
                                ? conversionService.convert(internal.getCause(), TdpExceptionDto.class) : null;
                        Map<String, Object> context = new HashMap<>();
                        for (Map.Entry<String, Object> contextEntry : internal.getContext().entries()) {
                            context.put(contextEntry.getKey(), contextEntry.getValue());
                        }

                        dto.setCode(serializedCode);
                        dto.setCause(cause);
                        dto.setContext(context);
                        return dto;
                    }).build());

            return conversionService;
        }
    }

}
