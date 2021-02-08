import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.owpk.jsondataextruder.DefinitionConfig;
import org.owpk.jsondataextruder.DefinitionConfigBuilder;

import java.util.List;

public class DefinitionConfigBuilderTest {
    private static DefinitionConfig config;

    @BeforeAll
    public static void init() {
        var balanceConfig = new DefinitionConfig(TestClasses.Balance.class);
        balanceConfig.addFieldsToShow("value");

        var poolBalancesConfig = new DefinitionConfig(TestClasses.PoolBalances.class);
        poolBalancesConfig.addFilterByFields("pool", "hive", "string");
        poolBalancesConfig.addFieldsToShow("pool");
        poolBalancesConfig.addEntitiesToShow(balanceConfig);

        DefinitionConfig walletConfig = new DefinitionConfig(TestClasses.Wallet.class);
        walletConfig.setEntitiesToShow(List.of(poolBalancesConfig));
        walletConfig.setFieldsToShow(List.of("coin", "name", "id", "wal"));
        walletConfig.addFilterByFields("id", "0", "2");

        config = walletConfig;
    }

    @Test
    public void test() {
        DefinitionConfig config = new DefinitionConfigBuilder(TestClasses.Wallet.class)
                .addFieldsToShow("coin", "name", "id", "wal")
                .addFilterBy("id", "0","2")
                .addNewDefinitionConfig(TestClasses.PoolBalances.class)
                .addFilterBy("pool", "hive", "string")
                .addFieldsToShow("pool")
                .addNewDefinitionConfig(TestClasses.Balance.class)
                .addFieldsToShow( "value")
                .build();
        Assertions.assertEquals(config, DefinitionConfigBuilderTest.config);
    }
}
