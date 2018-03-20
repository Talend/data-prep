//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.mail;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * This class contains all the data needed to send feedback to Talend.
 */
public class MailDetails implements Serializable {

    private String title;

    private String mail;

    private String severity;

    private String type;

    private String description;

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(title) && //
            StringUtils.isEmpty(mail) && //
            StringUtils.isEmpty(severity) && //
            StringUtils.isEmpty(type) && //
            StringUtils.isEmpty(description);
    }

    @Override public String toString() {
        return "MailDetails{" +
                "title='" + title + '\'' +
                ", mail='" + mail + '\'' +
                ", severity='" + severity + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}