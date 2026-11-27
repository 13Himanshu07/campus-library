package com.library;

import com.library.util.DBConnection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductionConfigurationTest {
    @Test void productionRejectsRootAndUnverifiedTls() {
        assertThrows(IllegalStateException.class,()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?sslMode=VERIFY_CA","root"));
        assertThrows(IllegalStateException.class,()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?sslMode=REQUIRED","library_app"));
        assertThrows(IllegalStateException.class,()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?sslMode=DISABLED","library_app"));
        assertThrows(IllegalStateException.class,()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?sslMode=VERIFY_CA_INSECURE","library_app"));
        assertThrows(IllegalStateException.class,()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?note=sslMode=VERIFY_CA","library_app"));
        assertThrows(IllegalStateException.class,()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?sslMode=VERIFY_CA&useSSL=false","library_app"));
    }
    @Test void productionAcceptsVerifiedTlsAndDevelopmentRemainsCompatible() {
        assertDoesNotThrow(()->DBConnection.validateProductionConfiguration(true,"jdbc:mysql://db/library?sslMode=VERIFY_CA","library_app"));
        assertDoesNotThrow(()->DBConnection.validateProductionConfiguration(false,"jdbc:mysql://localhost/library?sslMode=DISABLED","root"));
    }
}
