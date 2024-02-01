insert into sys_dict_type values(11, '节点类型', 'cnsl_node_type',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '安装包类型');
insert into sys_dict_data values(30, 1,  '工作节点',     'worker',       'cnsl_node_type',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '工作节点');
insert into sys_dict_data values(31, 2,  '中心节点',     'center',       'cnsl_node_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点');
insert into sys_dict_data values(32, 3,  '哨兵节点',     'sentinel',     'cnsl_node_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '哨兵节点');
insert into sys_dict_data values(33, 3,  '代理节点',     'proxy',        'cnsl_node_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '代理节点');

insert into sys_dict_type values(12, '指令执行结果状态', 'cnsl_command_result',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '指令执行结果状态');
insert into sys_dict_data values(41, 1,  '成功',     'done',       'cnsl_command_result',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '执行成功');
insert into sys_dict_data values(42, 2,  '失败',     'failed',       'cnsl_command_result',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '执行失败');

insert into sys_dict_type values(13, '部署模式', 'cnsl_deploy_mode',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '部署模式');
insert into sys_dict_data values(50, 1,  '单点模式',     'single',       'cnsl_deploy_mode',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '单点模式');
insert into sys_dict_data values(51, 2,  '哨兵主从模式',     'sentinel_worker',       'cnsl_deploy_mode',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '哨兵主从模式');
insert into sys_dict_data values(52, 3,  '集群模式',     'cluster',       'cnsl_deploy_mode',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '集群模式');
insert into sys_dict_data values(53, 4,  '可伸缩集群模式',     'scalable',       'cnsl_deploy_mode',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '可伸缩集群模式');

insert into sys_dict_type values(15, '远程登录方式', 'cnsl_ssh_login_mode',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '远程登录方式');
insert into sys_dict_data values(60, 1,  '用户名/密码',     'passwd',       'cnsl_ssh_login_mode',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '用户名/密码');
insert into sys_dict_data values(61, 2,  '秘钥模式',     'key',       'cnsl_ssh_login_mode',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '秘钥模式');

insert into sys_dict_type values(16, 'RDS认证方式',    'cnsl_rds_secure_mode',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, 'RDS认证方式');
insert into sys_dict_data values(65, 1,  '不校验',     '0',       'cnsl_rds_secure_mode',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '不校验');
insert into sys_dict_data values(66, 2,  'ssh',       '1',       'cnsl_rds_secure_mode',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, 'ssh');
insert into sys_dict_data values(67, 3,  '密码',       '2',       'cnsl_rds_secure_mode',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '密码');
insert into sys_dict_data values(68, 4,  'ssl+密码',   '3',       'cnsl_rds_secure_mode',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, 'ssl+密码');

insert into sys_dict_type values(18, 'RDS节点状态',    'cnsl_node_status',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, 'RDS节点状态');
insert into sys_dict_data values(80, 1,  '未安装',     'none',       'cnsl_node_status',   '',   'default', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '未安装');
insert into sys_dict_data values(81, 2,  '启动',       'start',       'cnsl_node_status',   '',   'success',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '启动');
insert into sys_dict_data values(82, 3,  '启动中',      'starting',       'cnsl_node_status',   '',   'info', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '启动中');
insert into sys_dict_data values(83, 5,  '停止',       'stop',       'cnsl_node_status',   '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '停止');
insert into sys_dict_data values(84, 6,  '停止中',      'stopping',       'cnsl_node_status',   '',   'warning', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '停止中');
insert into sys_dict_data values(85, 7,  '部分启动',    'start-part',       'cnsl_node_status',   '',   'warning',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '部分启动');

insert into sys_dict_type values(19, '安装包类型', 'cnsl_package_type',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '安装包类型');
insert into sys_dict_data values(75, 1,  '工作哨兵节点',     'worker',       'cnsl_package_type',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '工作哨兵节点');
insert into sys_dict_data values(76, 2,  '中心节点',     'center',       'cnsl_package_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点');
insert into sys_dict_data values(77, 3,  '代理节点',     'proxy',        'cnsl_package_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '代理节点');

insert into sys_dict_type values(20, '模版配置类型', 'cnsl_template_type',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '模版管理');
insert into sys_dict_data values(90, 1,  '工作节点-配置',     'worker-cfg',       'cnsl_template_type',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '工作节点-配置');
insert into sys_dict_data values(91, 2,  '工作节点-动态配置',     'worker-dynamic',       'cnsl_template_type',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '工作节点-动态配置');
insert into sys_dict_data values(92, 3,  '哨兵节点-配置',     'sentinel-cfg',       'cnsl_template_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '哨兵节点-配置');
insert into sys_dict_data values(93, 4,  '代理节点-配置',     'proxy-cfg',        'cnsl_template_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '代理节点-配置');
insert into sys_dict_data values(94, 5,  '中心节点-配置',     'center-cfg',       'cnsl_template_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点-配置');
insert into sys_dict_data values(95, 6,  '中心节点-部署管理配置',     'center-cluster',       'cnsl_template_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点-部署管理配置');
insert into sys_dict_data values(96, 7,  '中心节点-同步配置',     'center-sync',       'cnsl_template_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点-同步配置');
insert into sys_dict_data values(97, 8,  '中心节点-用户访问控制',     'center-alc',       'cnsl_template_type',   '',   '',  'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点-用户访问控制');

insert into sys_dict_type values(21, '部署环境', 'cnsl_deploy_env',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '部署环境');
insert into sys_dict_data values(110, 1,  '主机/虚机',     'host',       'cnsl_deploy_env',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '主机/虚拟机环境');
insert into sys_dict_data values(111, 2,  'K8S容器云',    'k8s',       'cnsl_deploy_env',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, 'K8S容器云');

insert into sys_dict_type values(22, '指令类型', 'cnsl_command_type',   '0', 'admin', CURRENT_TIMESTAMP(), '', null, '指令类型');
insert into sys_dict_data values(115, 1,  '节点管理器操作',     'manager',       'cnsl_command_type',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '节点管理器操作');
insert into sys_dict_data values(116, 2,  '节点操作',    'node',       'cnsl_command_type',   '',   '', 'N', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '节点操作');


drop table if exists cnsl_rds_service;
create table cnsl_rds_service (
  service_id              bigint(20)      not null auto_increment    comment '服务ID',
  sentinel_service_id     bigint(20)      default null               comment '哨兵服务ID',
  service_name            varchar(1000)   not null                   comment '服务名称',
  host_address            varchar(512)    default null               comment '服务地址(K8S的中心服务)',
  admin_port              int(9)          default null               comment 'admin端口(K8S的中心服务)',
  manual_admin            tinyint(1)      default '1'                comment '是否手工维护，默认是 0-否 1-是',
  deploy_mode             varchar(20)     not null                   comment '部署模式， 字典类型 cnsl_deploy_mode, 隐藏类型center',
  secure_mode             tinyint(4)      not null default '0'       comment '认证方式, cnsl_rds_secure_mode',
  version_id              bigint(20)      default null               comment '版本',
  group_id                bigint(20)      default null               comment '模版组ID',
  password                varchar(100)    default ''                 comment '认证密码',
  create_by               varchar(64)     default ''                 comment '创建者',
  create_time             datetime                                   comment '创建时间',
  update_by               varchar(64)     default ''                 comment '更新者',
  update_time             datetime                                   comment '更新时间',
  remark                  varchar(500)    default null               comment '备注',
  primary key (service_id),
  unique (service_name)
) ENGINE=InnoDB auto_increment=10 DEFAULT CHARSET=utf8 COMMENT='RDS服务';

insert into cnsl_rds_service values(1, null, 'rds-center-service', null, null, 1, 'center', '2', 1, 1, '454d51192b1704c60e19734ce6b38203', 'admin', CURRENT_TIMESTAMP(), '', null, '中心节点服务');


drop table if exists cnsl_rds_node;
create table cnsl_rds_node (
  node_id              bigint(20)      not null auto_increment    comment '节点ID',
  manager_id           bigint(20)      default null               comment '节点管理器ID',
  service_id           bigint(20)      not null                   comment '服务ID',
  node_name            varchar(200)    not null                   comment '节点名称',
  instance             varchar(300)    not null                   comment '节点实例名',
  node_type            varchar(20)     not null                   comment '节点类型',
  host_address         varchar(512)    not null                   comment '节点地址',
  service_port         int(9)          not null                   comment 'RDS端口',
  redis_port           int(9)          default null               comment 'redis端口',
  admin_port           int(9)          default null               comment 'admin端口(Center节点用)',
  master_node          tinyint(1)      default 0                  comment '是否为主节点 0-否 1-是',
  hot_spares           tinyint(1)      default 0                  comment '是否为热备节点 0-否 1-是',
  slot                 varchar(50)     default null               comment '分片的插槽范围',
  shard                int(8)          default null               comment '分片的编号',
  node_status          varchar(30)     not null                   comment '节点状态，cnsl_rds_node_status',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (node_id),
  unique (instance),
  unique (manager_id, node_name),
  unique (host_address, service_port),
  KEY (host_address, redis_port)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='RDS节点信息';

drop table if exists cnsl_node_config;
CREATE TABLE cnsl_node_config (
    node_id             bigint(20)      not null                  comment '节点ID',
    temp_type           varchar(30)     not null                  comment '对应模版类型: cnsl_template_type',
    template_id         bigint(20)      default null              comment '使用的模版ID',
    conf_content        varchar(15000)  default null              comment '配置内容',
    update_time          datetime                                 comment '更新时间',
    primary key (node_id, temp_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='节点配置信息';


drop table if exists cnsl_service_config;
CREATE TABLE cnsl_service_config (
    service_id          bigint(20)      not null                  comment '服务ID',
    conf_type           varchar(30)     not null                  comment '对应模版类型: cnsl_template_type',
    template_id         bigint(20)      default null              comment '使用的模版ID',
    conf_content        varchar(15000)  default null              comment '配置内容',
    update_time          datetime                                 comment '更新时间',
    primary key (service_id, conf_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务配置信息';

drop table if exists cnsl_rds_version;
create table cnsl_rds_version (
  version_id           bigint(20)      not null auto_increment    comment '版本ID',
  software_name        varchar(200)    not null                   comment '软件名称',
  version_no           varchar(100)    not null                   comment '版本',
  default_group_id     bigint(20)      default null               comment '默认模版组ID',
  default_version      tinyint(1)      default 1                  comment '是否为默认版本',
  status               char(1)         not null                   comment '状态（1正常 0停用）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (version_id),
  unique (software_name,version_no)
) engine=innodb auto_increment=100 DEFAULT CHARSET=utf8 comment = '版本信息';

insert into cnsl_rds_version values(1, 'TongRDS-CN', '2.2.C.1', 1, 1,  1, 'admin', CURRENT_TIMESTAMP(), '', null, '东方通分布式数据缓存中间件软件（云原生版）');

drop table if exists cnsl_rds_version_pkg;
create table cnsl_rds_version_pkg (
  package_id           bigint(20)      not null auto_increment    comment '安装包ID',
  version_id           bigint(20)      not null                   comment '版本ID',
  pkg_name             varchar(200)    not null                   comment '包名称',
  pkg_type             varchar(30)     not null                   comment '包类类型: cnsl_package_type',
  file_name            varchar(200)    not null                   comment '文件名称（下载时使用不显示给用户）',
  file_size            int(11)         not null                   comment '文件大小',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (package_id)
) engine=innodb auto_increment=100 comment = '安装包信息';

drop table if exists cnsl_center_stat_src;
CREATE TABLE cnsl_center_stat_src (
  src_id                 bigint(20)       not null auto_increment      comment '源ID',
  centers_src            varchar(3000)    not null                     comment '中心节点列表内容',
  services_src           text             not null                     comment '服务列表内容',
  sentinels_src          varchar(5000)    not null                     comment '哨兵节点列表内容',
  license_src            varchar(1000)    not null                     comment 'license使用情况内容',
  duration               bigint(20)                                     comment '执行时长(毫秒)',
  create_time            datetime                                       comment '创建时间',
  primary key (src_id),
  KEY center_stat_src_idx1 (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='中心节点统计信息的原始报文';


drop table if exists cnsl_service_stat;
CREATE TABLE cnsl_service_stat (
    stat_id              bigint(20)      not null auto_increment     comment '监控信息ID',
    src_id               bigint(20)      not null                    comment '源ID',
    service_id           bigint(20)      not null                    comment '节点ID',
    deploy_mode          varchar(20)     not null                    comment '部署模式， 字典类型 cnsl_deploy_mode',
    name                 varchar(200)    not null                    comment '服务名称',
    current_connections  bigint(20)      default 0                   comment '当前连接数',
    total_connections    bigint(20)      default 0                   comment '启动以来一共连接次数',
    current_keys         bigint(20)      default null                comment '当前key总数',
    memory_used          bigint(20)      default 0                   comment '当前内存使用量',
    memory_free          bigint(20)      default 0                   comment '当前内存剩余量',
    memory_total         bigint(20)      default 0                   comment '实际占用内存总量，JVM当前使用的堆内存总量',
    memory_available     bigint(20)      default 0                   comment '最大可用内存量，JVM -Xmx 的参数值',
    command_result       bigint(20)      default null                comment '启动以来返回结果的命令数累计',
    network_input_bytes  bigint(20)      default null                comment '网络IO入流量总和',
    input_per_second     double          default null                comment '网络IO每秒入流量',
    network_output_bytes bigint(20)      default null                comment '网络IO出流量总和',
    output_per_second    double          default null                comment '网络IO每秒出流量',
    cpu_process_load     double          default null                comment '当前程序CPU使用率，百分比值',
    cpu_system_load      double          default null                comment '当前系统CPU使用率, 百分比值',
    create_time          datetime                                    comment '创建时间',
    create_second        bigint(20)                                  comment '创建时间秒',
    primary key (stat_id),
    KEY service_stat_idx1 (create_time),
    KEY service_stat_idx2 (service_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务监控信息';


drop table if exists cnsl_node_stat;
CREATE TABLE cnsl_node_stat (
  stat_id              bigint(20)      not null auto_increment     comment '监控信息ID',
  src_id               bigint(20)      not null                    comment '源ID',
  node_id              bigint(20)      not null                    comment '节点ID',
  service_id           bigint(20)      not null                    comment '节点ID',
  name                 varchar(200)    not null                    comment '节点名称',
  node_type            varchar(20)     not null                    comment '节点类型',
  instance             varchar(300)    not null                    comment '节点实例名',
  expired              tinyint(1)      not null                    comment '是否过期， 0-否 1-是（大于3秒钟没有收到心跳就会变成是）',
  running              bigint(20)      not null                    comment '启动以来运行了多少秒',
  current_connections  bigint(20)      default 0                   comment '当前连接数',
  total_connections    bigint(20)      default 0                   comment '启动以来一共连接次数',
  current_keys         bigint(20)      default null                comment '当前key总数',
  memory_used          bigint(20)      default 0                   comment '当前内存使用量',
  memory_free          bigint(20)      default 0                   comment '当前内存剩余量',
  memory_total         bigint(20)      default 0                   comment '实际占用内存总量，JVM当前使用的堆内存总量',
  memory_available     bigint(20)      default 0                   comment '最大可用内存量，JVM -Xmx 的参数值',
  command_result       bigint(20)      default null                comment '启动以来返回结果的命令数累计',
  network_input_bytes  bigint(20)      default null                comment '网络IO入流量总和',
  input_per_second     double          default null                comment '网络IO每秒入流量',
  network_output_bytes bigint(20)      default null                comment '网络IO出流量总和',
  output_per_second    double          default null                comment '网络IO每秒出流量',
  cpu_process_load     double          default null                comment '当前程序CPU使用率，百分比值',
  cpu_system_load      double          default null                comment '当前系统CPU使用率, 百分比值',
  throughput_average10 double          default null                comment '前10秒内，每秒平均流量',
  throughput_average60 double          default null                comment '前60秒内，每秒平均流量',
  create_time          datetime                                    comment '创建时间',
  create_second        bigint(20)                                  comment '创建时间秒',
  primary key (stat_id),
  KEY node_stat_idx1 (create_time),
  KEY node_stat_idx2 (node_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='节点监控信息';


-- 命令执行历史表中 --
drop table if exists cnsl_command_history;
CREATE TABLE cnsl_command_history (
  history_id          bigint(20)      not null auto_increment    comment '历史ID',
  command_type        varchar(30)     not null                   comment '操作类型 cnsl_command_type',
  node_id             bigint(20)      default null               comment '节点ID',
  manager_id          bigint(20)      default null               comment '节点管理器ID',
  cmd                 varchar(30)     not null                   comment '操作指令',
  cmd_msg             varchar(15000)  default ''                 comment '操作指令内容',
  cmd_file            varchar(1000)   default ''                 comment '操作指令文件名',
  res_status          varchar(30)     not null                   comment '指令执行结果状态 cnsl_command_result',
  res_msg             varchar(5000)  default ''                  comment '指令执行结果消息内容',
  create_time         datetime                                   comment '创建时间',
  duration            bigint(20)                                 comment '执行时长(毫秒)',
  primary key (history_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='执行命令历史表';
