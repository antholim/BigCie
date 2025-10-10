package bigcie.bigcie.entities;

import bigcie.bigcie.models.enums.UserType;

public class Rider extends User {
    public Rider() {
        this.type = UserType.RIDER;
    }
}
