package bigcie.bigcie.entities;

import bigcie.bigcie.models.enums.UserType;

public class Operator extends User {
    public Operator() {
        this.type = UserType.OPERATOR;
    }
}
