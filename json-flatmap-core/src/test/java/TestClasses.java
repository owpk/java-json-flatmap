import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.owpk.objectname.ObjectName;

import java.util.List;

public class TestClasses {
    @JsonAutoDetect
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    @ObjectName(name = "wallet")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wallet {

        private Long id;

        @JsonProperty(value = "user_id")
        private Long userId;

        private String name;
        private String source;
        private String coin;
        private String wal;

        @JsonProperty(value = "fetch_balance")
        private boolean fetchBalance;

        @JsonProperty(value = "api_key_id")
        private Integer apiKeyId;

        @ObjectName
        private Balance balance;

        @JsonProperty(value = "pool_balances")
        @ObjectName(name = "pool_balances")
        private List<PoolBalances> poolBalances;

        @JsonProperty(value = "fs_count")
        private Integer fsCount;

        @JsonProperty(value = "workers_count")
        private Integer workersCount;

        @JsonProperty(value = "farm_id")
        private Integer farmId;

    }

    @Data
    @ObjectName(name = "pool_balances")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PoolBalances {

        private String pool;

        @ObjectName(name = "balance")
        private Balance balance;
    }

    @JsonAutoDetect
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ObjectName(name = "balance")
    public static class Balance {

        @JsonProperty
        private String status;
        private Double value;

        @JsonProperty("value_fiat")
        private Double valueFiat;

    }
}
