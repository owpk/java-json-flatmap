import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IntegrationTest {

    @BeforeAll
    static void init() {
        new JsonWrapperTest().init();
    }

    @Test
    public void test() throws JsonProcessingException {
        DefinitionConfig config = new DefinitionConfigBuilder(TestClasses.Wallet.class)
                .addFieldsToShow("coin", "name", "id", "wal")
                .addFilterBy("id", "0","2")
                .addNewDefinitionConfig(TestClasses.PoolBalances.class)
                .addFilterBy("pool", "hive", "string")
                .addFieldsToShow("pool")
                .addNewDefinitionConfig(TestClasses.Balance.class)
                .addFieldsToShow( "value")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        TestClasses.Wallet[] wallets = objectMapper.readValue(JsonWrapperTest.json, TestClasses.Wallet[].class);

        Multimap<String, String> collector = JsonFlatmap.flatmap(wallets, config);
        Assertions.assertEquals(JsonWrapperTest.result, collector);
    }
}
