package cd.beapi.enumerate;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

import static cd.beapi.enumerate.ActionType.*;

@Getter
public enum PageType {
    DASHBOARD   (EnumSet.of(VIEW)),
    PATIENT     (EnumSet.of(VIEW, CREATE, UPDATE, DELETE, EXPORT)),
    STAFF       (EnumSet.of(VIEW, CREATE, UPDATE, DELETE)),
    APPOINTMENT (EnumSet.of(VIEW, CREATE, UPDATE, DELETE)),
    EXAMINATION (EnumSet.of(VIEW, UPDATE)),
    MEDICINE    (EnumSet.of(VIEW, CREATE, UPDATE, DELETE, EXPORT)),
    TREATMENT   (EnumSet.of(VIEW, CREATE, UPDATE)),
    PROCEDURE   (EnumSet.of(VIEW, CREATE, UPDATE, DELETE));

    private final Set<ActionType> allowedActions;

    PageType(Set<ActionType> allowedActions) {
        this.allowedActions = allowedActions;
    }

}
