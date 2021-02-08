import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class JsonWrapperTest {
    public static Multimap<String, String> result;

    @Test
    public void init() {
        result = LinkedListMultimap.create();
        result.put("id", "0");
        result.put("id", "2");
        result.put("name", "ETH wallet");
        result.put("name", "BTC wallet");
        result.put("coin", "ETH");
        result.put("coin", "BTC");
        result.put("wal", "0x123123123123123123");
        result.put("wal", "0x123123123123123123");
        result.put("pool", "string");
        result.put("value", "0.0");
        result.put("pool", "hive");
        result.put("value", "0.0");
        result.put("pool", "string");
        result.put("value", "0.0");
        result.put("pool", "hive");
        result.put("value", "0.0");
    }

    public static final String json = "[" +
            "{" +
            "\"id\": 0," +
            "\"coin\": \"ETH\"," +
            "\"name\": \"ETH wallet\"," +
            "\"wal\": \"0x123123123123123123\"," +
            "\"source\": \"string\"," +
            "\"fetch_balance\": false," +
            "\"api_key_id\": 0," +
            "\"balance\": {" +
            "\"value\": 0," +
            "\"value_fiat\": 0," +
            "\"status\": \"pending\"" +
            "}," +
            "\"pool_balances\": [" +
            "{" +
            "\"pool\": \"string\"," +
            "\"balance\": {" +
            "\"value\": 0," +
            "\"value_fiat\": 0," +
            "\"status\": \"pending\"" +
            "}" +
            "}," +
            "{" +
            "\"pool\": \"hive\"," +
            "\"balance\": {" +
            "\"value\": 0," +
            "\"value_fiat\": 0," +
            "\"status\": \"pending\"" +
            "}" +
            "}" +
            "]," +
            "\"fs_count\": 0," +
            "\"workers_count\": 0," +
            "\"farm_id\": 0," +
            "\"user_id\": 0" +
            "}," +
            "{" +
            "\"id\": 2," +
            "\"coin\": \"BTC\"," +
            "\"name\": \"BTC wallet\"," +
            "\"wal\": \"0x123123123123123123\"," +
            "\"source\": \"string\"," +
            "\"fetch_balance\": false," +
            "\"api_key_id\": 0," +
            "\"balance\": {" +
            "\"value\": 0," +
            "\"value_fiat\": 0," +
            "\"status\": \"pending\"" +
            "}," +
            "\"pool_balances\": [" +
            "{" +
            "\"pool\": \"string\"," +
            "\"balance\": {" +
            "\"value\": 0," +
            "\"value_fiat\": 0," +
            "\"status\": \"pending\"" +
            "}" +
            "}," +
            "{" +
            "\"pool\": \"hive\"," +
            "\"balance\": {" +
            "\"value\": 0," +
            "\"value_fiat\": 0," +
            "\"status\": \"pending\"" +
            "}" +
            "}" +
            "]," +
            "\"fs_count\": 0," +
            "\"workers_count\": 0," +
            "\"farm_id\": 0," +
            "\"user_id\": 0" +
            "}" +
            "]";

    @Test
    public void test() throws JsonProcessingException {
        DefinitionConfig balanceConfig = new DefinitionConfig(TestClasses.Balance.class);
        balanceConfig.addFieldsToShow("value");

        DefinitionConfig poolBalancesConfig = new DefinitionConfig(TestClasses.PoolBalances.class);
        poolBalancesConfig.addFilterByFields("pool", "hive", "string");
        poolBalancesConfig.addFieldsToShow("pool");
        poolBalancesConfig.addEntitiesToShow(balanceConfig);

        DefinitionConfig walletConfig = new DefinitionConfig(TestClasses.Wallet.class);
        walletConfig.setEntitiesToShow(List.of(poolBalancesConfig));
        walletConfig.setFieldsToShow(List.of("coin", "name", "id", "wal"));
        walletConfig.addFilterByFields("id", "0", "2");


        Multimap<String, String> simpleDataCollector = LinkedListMultimap.create();

        ObjectMapper objectMapper = new ObjectMapper();

        TestClasses.Wallet[] wallets = objectMapper.readValue(json, TestClasses.Wallet[].class);

        Arrays.stream(wallets).forEach(x -> {
            JsonObjectWrapperImpl<TestClasses.Wallet> jsonObjectWrapper = new JsonObjectWrapperImpl<>(x, simpleDataCollector);
            jsonObjectWrapper.executeNext(walletConfig);
        });


        Assertions.assertEquals(result, simpleDataCollector);
    }
}
