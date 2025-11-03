package bigcie.bigcie.services.read.interfaces;

import java.util.UUID;

public interface IBikeStationLookup {
    String getStationNameById(UUID bikeStationId);
}
