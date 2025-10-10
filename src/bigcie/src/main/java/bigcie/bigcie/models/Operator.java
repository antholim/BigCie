package bigcie.bigcie.models;

public class Operator extends User {
    public Operator() {
        this.type = UserType.OPERATOR;
    }

    public static class Builder implements bigcie.bigcie.models.factory.UserBuilder<Operator> {
        private java.util.UUID id;
        private String username;
        private String email;
        private String password;

        @Override
        public Builder id(java.util.UUID id) {
            this.id = id;
            return this;
        }
        @Override
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        @Override
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        @Override
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        @Override
        public Operator build() {
            Operator operator = new Operator();
            operator.setId(this.id);
            operator.setUsername(this.username);
            operator.setEmail(this.email);
            operator.setPassword(this.password);
            operator.type = UserType.OPERATOR;
            return operator;
        }
    }
}
