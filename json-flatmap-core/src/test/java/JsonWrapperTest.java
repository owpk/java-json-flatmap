import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.owpk.jsondataextruder.DefinitionConfig;
import org.owpk.jsondataextruder.JsonObjectWrapperImpl;

import java.util.Arrays;
import java.util.List;

public class JsonWrapperTest {
    public static Multimap<String, String> expected;

    @BeforeAll
    public static void init() {
        expected = LinkedListMultimap.create();
        expected.put("id", "0");
        expected.put("id", "2");
        expected.put("name", "ETH wallet");
        expected.put("name", "BTC wallet");
        expected.put("coin", "ETH");
        expected.put("coin", "BTC");
        expected.put("wal", "0x123123123123123123");
        expected.put("wal", "0x123123123123123123");
        expected.put("pool", "string");
        expected.put("value", "0.0");
        expected.put("pool", "hive");
        expected.put("value", "0.0");
        expected.put("pool", "string");
        expected.put("value", "0.0");
        expected.put("pool", "hive");
        expected.put("value", "0.0");
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
        DefinitionConfig balanceConfig = new DefinitionConfig("balance");
        balanceConfig.addFieldsToShow("value");

        DefinitionConfig poolBalancesConfig = new DefinitionConfig("pool_balances");
        poolBalancesConfig.addFilterByFields("pool", "hive", "string");
        poolBalancesConfig.addFieldsToShow("pool");
        poolBalancesConfig.addEntitiesToShow(balanceConfig);

        DefinitionConfig walletConfig = new DefinitionConfig(TestClasses.Wallet.class);
        walletConfig.setObjects(List.of(poolBalancesConfig));
        walletConfig.setFields(List.of("coin", "name", "id", "wal"));
        walletConfig.addFilterByFields("id", "0", "2");


        Multimap<String, String> simpleDataCollector = LinkedListMultimap.create();

        ObjectMapper objectMapper = new ObjectMapper();

        TestClasses.Wallet[] wallets = objectMapper.readValue(json, TestClasses.Wallet[].class);

        Arrays.stream(wallets).forEach(x -> {
            JsonObjectWrapperImpl<TestClasses.Wallet> jsonObjectWrapper = new JsonObjectWrapperImpl<>(x, simpleDataCollector);
            jsonObjectWrapper.executeNext(walletConfig);
        });

        Assertions.assertEquals(expected, simpleDataCollector);
        System.out.println("Expected: " + expected);
        System.out.println("Result : " + simpleDataCollector);
    }
}
