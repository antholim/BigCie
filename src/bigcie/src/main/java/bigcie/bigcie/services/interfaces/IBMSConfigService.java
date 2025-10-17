package bigcie.bigcie.services.interfaces;

import bigcie.bigcie.entities.BMSConfig;

public interface IBMSConfigService {
    // Populate DB
    void populateDB();

    // return BMSConfig for frontend
    BMSConfig getBMSConfig();

    BMSConfig createConfig(String JSONFilePath);
}
