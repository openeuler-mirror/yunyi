DROP TABLE IF EXISTS `cnsl_template`;
CREATE TABLE `cnsl_template` (
    `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模版ID',
    `group_id` bigint(20) NOT NULL COMMENT '模版组ID',
    `temp_name` varchar(100) DEFAULT NULL COMMENT '模版名称',
    `temp_content` varchar(15000) NOT NULL COMMENT '模版内容',
    `temp_type` varchar(30) NOT NULL COMMENT '对应模版类型: cnsl_template_type',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`template_id`),
    UNIQUE KEY `group_id` (`group_id`,`temp_type`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='配置模版';

INSERT INTO `cnsl_template`
VALUES (3, 1, NULL,
        STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Server>\n    <Common>\n        <RuntimeModel>debug</RuntimeModel>\n        <DataDump>10</DataDump>\n        <Service>${service.serviceName}</Service>\n        <MaxValueLength>10m</MaxValueLength>\n        <MaxItemsInCompress>256</MaxItemsInCompress>\n        <SlowOperationThreshold>150</SlowOperationThreshold>\n        <!--<JmxUrl>localhost:29094</JmxUrl>-->\n        <Instance>${node.instance}</Instance>\n    </Common>\n    <Log>\n        <Level>error</Level>\n        <BackDates>30</BackDates>\n    </Log>\n    <Listen>\n        <Port>${node.servicePort}</Port>\n        <Threads>8</Threads>\n        <MaxConnections>1000</MaxConnections>\n        <Secure>${service.secureMode}</Secure>\n        <Password>${service.encryptPassword}</Password>\n        <RedisPort>${node.redisPort}</RedisPort>\n        <RedisPlainPassword>true</RedisPlainPassword>\n        #if(${service.secureMode} == 2)\n        <RedisPassword>${service.password}</RedisPassword>\n        #end\n    </Listen>\n    <Tables>1</Tables>\n    <Table1>\n        <Rows>1000000</Rows>\n        <Key>bytes2, 100</Key>\n        <Value>variable, 0</Value>\n        <Indexes>0</Indexes>\n        <Sync>\n            <ListNumbers>1</ListNumbers>\n            <ListLength>10000</ListLength>\n        </Sync>\n    </Table1>\n</Server>\n'),
        'worker-cfg', '', '2023-02-26 14:46:56', '', '2023-04-24 22:32:02', NULL),
       (4, 1, NULL,
        STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Server>\n  <Center>\n    <Password>454d51192b1704c60e19734ce6b38203</Password>\n    #foreach($node in ${nodes})\n    <EndPoint>\n      <Host>${node.hostAddress}</Host>\n      <Port>${node.servicePort}</Port>\n    </EndPoint>\n    #end\n  </Center>\n</Server>'),
        'worker-dynamic', '', '2023-02-26 14:47:17', '', '2023-04-24 22:32:27', NULL),
       (5, 1, NULL,
        STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Server>\n    <Common>\n        <SslCiphers>\n            TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,\n            TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA,\n            TLS_KRB5_WITH_DES_CBC_MD5, TLS_KRB5_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,\n            TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,\n            TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA\n        </SslCiphers>\n        <MasterPolicy>node</MasterPolicy>\n        <Instance>${sentinelNode.instance}</Instance>\n        <Group>${sentinelService.serviceName}</Group>\n    </Common>\n    <Log>\n        <Level>error</Level>\n        <BackDates>30</BackDates>\n    </Log>\n    <Listen>\n        <Port>${sentinelNode.servicePort}</Port>\n        <Threads>4</Threads>\n        #if(${sentinelService.secureMode} == 2)\n            <Secure>2</Secure>\n            <IsPlainPassword>true</IsPlainPassword>\n            <Password>${sentinelService.password}</Password>\n        #else\n            <Secure>0</Secure>\n        #end\n    </Listen>\n    <Center>\n        <Password>454d51192b1704c60e19734ce6b38203</Password>\n        #foreach($cnode in ${centerNodes})\n            <EndPoint>\n                <Host>${cnode.hostAddress}</Host>\n                <Port>${cnode.servicePort}</Port>\n            </EndPoint>\n        #end\n    </Center>\n    <Sentinels>\n        #foreach($snode in ${sentinelNodes})\n            <Sentinel>\n                <Host>${snode.hostAddress}</Host>\n                <Port>${snode.servicePort}</Port>\n            </Sentinel>\n        #end\n    </Sentinels>\n    <Services>\n        #foreach($serv in ${services})\n        <${serv.serviceName}>\n            #if(${serv.secureMode} == 2)\n                <Secure>2</Secure>\n                <IsPlainPassword>true</IsPlainPassword>\n                <Password>${serv.password}</Password>\n            #else\n                <Secure>0</Secure>\n            #end\n            <EndPoints>${serv.endpoints}</EndPoints>\n        </${serv.serviceName}>\n        #end\n    </Services>\n</Server>\n'),
        'sentinel-cfg', '', '2023-02-26 14:48:40', '', '2023-04-24 22:31:33', NULL),
       (6, 1, NULL,
        STRINGDECODE('server.instance=${node.instance}\nserver.log.file = center.log\nserver.log.level = error\nserver.password=454d51192b1704c60e19734ce6b38203\nserver.service.port=${node.servicePort}\nserver.admin.port=${node.adminPort}\n# server.sentinel.port=26379\n# server.sentinel.ssl=true\nserver.undo_cfg_file=false\n# server.jmxurl=localhost:29054\nserver.platform=${RESOURCE_PLATFORM}\nserver.platform_service=${D}{SERVICE_NAME:rds-center}\n'),
        'center-cfg', '', '2023-02-26 15:00:35', '', '2023-04-24 22:31:09', NULL),
       (7, 1, NULL,
        STRINGDECODE('sync.servers=${nodes.size()}\n#foreach($node in ${nodes})\nsync.server${foreach.index}.host=${node.hostAddress}\nsync.server${foreach.index}.port=${node.servicePort}\n#end\n'),
        'center-sync', '', '2023-02-26 15:00:58', '', '2023-04-24 22:30:50', NULL),
       (8, 1, NULL,
        STRINGDECODE('#foreach($serv in ${services})\n  ${serv.serviceName}.type=${serv.deployMode}\n  #if(${serv.deployMode} == \"cluster\")\n    ${serv.serviceName}.shards=${serv.shards.size()}\n    #foreach($shard in ${serv.shards})\n      ${serv.serviceName}.shard${shard.index}.nodes=${shard.endpoints}\n      ${serv.serviceName}.shard${shard.index}.slots=${shard.slot}\n    #end\n  #else\n    ${serv.serviceName}.nodes=${serv.nodes.size()}\n    #foreach($node in ${serv.nodes})\n      ${serv.serviceName}.node${foreach.index}=${node.serviceEndpoint}\n    #end\n  #end\n#end'),
        'center-cluster', '', '2023-02-26 15:01:48', '', '2023-04-24 22:30:03', NULL);
