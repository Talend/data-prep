// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.preparation.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.datepattern.DatePatternRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.stream.Collectors;

@RestController
@Api(value = "datepatterns", basePath = "/datepatterns", description = "Operations on datepattern")
public class DatePatternService
{
    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger( DatePatternService.class);

    /** Where the datepattern are stored. */
    @Autowired
    private DatePatternRepository datePatternRepository;


    @RequestMapping(value = "/datepatterns", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "list all datepatterns", produces = APPLICATION_JSON_VALUE)
    public Iterable<DatePattern> all(@RequestParam(required = false) String keyword){

        return datePatternRepository //
            .all() //
            .stream() //
            .filter( datePattern -> StringUtils.contains( datePattern.getPattern(), keyword) ) //
            .collect( Collectors.toSet());
    }

    @RequestMapping(value = "/datepatterns", method = PUT)
    @ApiOperation(value = "add a new datepattern")
    public void add(@RequestParam String datePattern){
        if ( StringUtils.isBlank( datePattern )){
            return;
        }
        LOGGER.debug( "creating datepattern: {}", datePattern );
        datePatternRepository.add( new DatePattern(datePattern) );
    }

    @RequestMapping(value = "/datepatterns", method = DELETE)
    @ApiOperation(value = "remove a datepattern")
    public void remove(@RequestParam String datePattern){
        if ( StringUtils.isBlank( datePattern )){
            return;
        }
        LOGGER.debug( "removing datepattern: {}", datePattern );
        datePatternRepository.remove( new DatePattern(datePattern) );
    }

}
