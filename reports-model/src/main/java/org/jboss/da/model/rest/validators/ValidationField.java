package org.jboss.da.model.rest.validators;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Stanislav Knot <sknot@redhat.com>
 */
@JsonRootName("validationField")
@EqualsAndHashCode
@ToString
@JsonInclude(Include.NON_NULL)
public class ValidationField {

    @Getter
    private String attribute;

    @Getter
    private List<String> messages = new ArrayList<String>();

    @Setter
    @Getter
    private String value;

    public ValidationField(String attribute) {
        this.attribute = attribute;
    }

    public ValidationField(String attribute, String message) {
        this.attribute = attribute;
        this.messages.add(message);
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }
}
