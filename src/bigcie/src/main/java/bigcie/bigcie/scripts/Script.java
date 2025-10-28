package bigcie.bigcie.scripts;

import bigcie.bigcie.entities.Rider;
import bigcie.bigcie.services.interfaces.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class Script implements CommandLineRunner {
    private final IUserService userService;

    public Script(IUserService userService) {
        this.userService = userService;
    }
    @Override
    public void run(String... args) throws Exception {
//        clearBikeFromUser(UUID.fromString("93e21f50-5bf8-4891-9d3a-30a9676f3b36"));
    }

    private void assignBikeToStation() {

    }

    private void clearBikeFromUser(UUID userId) {
        Rider rider = (Rider) userService.getUserByUUID(userId);
        rider.setCurrentBikes(new ArrayList<>());
        userService.updateUser(rider);
    }
}
