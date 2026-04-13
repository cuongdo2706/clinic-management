package cd.beapi.enumerate;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

import static cd.beapi.enumerate.ActionType.*;

@Getter
public enum PageType {
    PATIENT     (EnumSet.of(VIEW, CREATE, UPDATE, DELETE, EXPORT)),
    STAFF       (EnumSet.of(VIEW, CREATE, UPDATE, DELETE)),
    APPOINTMENT (EnumSet.of(VIEW, CREATE, UPDATE, DELETE)),
    MEDICINE    (EnumSet.of(VIEW, CREATE, UPDATE, DELETE, EXPORT)),
    INVOICE     (EnumSet.of(VIEW, EXPORT)),
    PRESCRIPTION(EnumSet.of(VIEW, CREATE, UPDATE)),
    TREATMENT   (EnumSet.of(VIEW, CREATE, UPDATE, DELETE)),
    USER        (EnumSet.of(VIEW, CREATE, UPDATE, DELETE));

    private final Set<ActionType> allowedActions;

    PageType(Set<ActionType> allowedActions) {
        this.allowedActions = allowedActions;
    }

}
