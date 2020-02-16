/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/16
 */
package com.enkanrec.twitkitFridge.monitor;

import io.prometheus.client.Counter;
import org.springframework.stereotype.Component;

/**
 * Class : BulkMonitor
 * Usage : 批量入库接口的定制监控
 */
@Component
public class BulkMonitor extends BaseMonitor {
    public final Counter totalCounter = Counter
            .build()
            .name("twitkit_fridge_bulk_raw_count")
            .help("Twitkit fridge bulk service without bloom cache total requests counter")
            .register();

    public final Counter bloomQueryCounter = Counter
            .build()
            .name("twitkit_fridge_bulk_bloom_total_count")
            .help("Twitkit fridge bulk service bloom cache total query counter")
            .register();

    public final Counter bloomHitCounter = Counter
            .build()
            .name("twitkit_fridge_bulk_bloom_hit_count")
            .help("Twitkit fridge bulk service bloom cache hit counter")
            .register();

    public final Counter bloomFalsePositiveCounter = Counter
            .build()
            .name("twitkit_fridge_bulk_bloom_false_positive_count")
            .help("Twitkit fridge bulk service bloom cache hit but not exist counter")
            .register();
}
