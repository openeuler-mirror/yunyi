-- 开启管理系统【参数设置】配置
UPDATE sys_menu SET status = 0 WHERE menu_name = '参数设置';

-- 执行以下SQL 或者 在管理界面【参数设置】配置一条参数键名为：monitorChartAutoRefreshSeconds的数据
DELETE FROM sys_config WHERE config_key = 'monitorChartAutoRefreshSeconds';
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, remark) VALUES ('监控图表刷新间隔', 'monitorChartAutoRefreshSeconds', '300', 'N', 'admin', '监控图表刷新间隔，单位：秒');