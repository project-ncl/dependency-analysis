package org.jboss.da.model.rest.validators;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Stanislav Knot &lt;sknot@redhat.com&gt;
 */
@JsonRootName("validation")
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Validations {

    @Getter
    private List<ValidationField> validation = new ArrayList<ValidationField>();

    public void addValidationField(ValidationField vf) {
        this.validation.add(vf);
    }

}
