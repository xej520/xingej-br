create database broker;

use broker;

show tables;

CREATE TABLE `broker_opt_log` (
  `id` varchar(255) NOT NULL,
  `created_time` datetime DEFAULT NULL,
  `instance_id` varchar(255) DEFAULT NULL,
  `opt_type` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  `error_msg` varchar(3000) DEFAULT NULL,
  `plan_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `service_instance` (
  `id` varchar(255) NOT NULL,
  `catalog` varchar(255) DEFAULT NULL,
  `dashboard_url` varchar(255) DEFAULT NULL,
  `parameters` varchar(255) DEFAULT NULL,
  `plan_id` varchar(255) DEFAULT NULL,
  `project_id` varchar(255) DEFAULT NULL,
  `service_id` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `tenant_id` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `service_instance_binding` (
  `id` varchar(255) NOT NULL,
  `created_time` datetime DEFAULT NULL,
  `credentials` varchar(3000) DEFAULT NULL,
  `instance_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `unit_version` (
  `id` varchar(255) NOT NULL,
  `app_type` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `extended_field` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

show tables;

INSERT INTO `unit_version` VALUES ('1', 'mysql', '172.16.3.50:5000/bonc-mysql57:latest', '5.7', 'default');
INSERT INTO `unit_version` VALUES ('2', 'mysql', '172.16.3.50:5000/bonc-mysql56:latest', '5.6', 'default');
INSERT INTO `unit_version` VALUES ('3', 'mysql', '172.16.3.50:5000/bonc-mysql80:latest', '8.0', 'default');
INSERT INTO `unit_version` VALUES ('4', 'redis', '172.16.3.50:5000/redis:3.2.11', '3.2.11', 'default');
INSERT INTO `unit_version` VALUES ('5', 'mysql', '172.16.3.50:5000/mysqld-exporter:latest', '5.6', 'exporter');
INSERT INTO `unit_version` VALUES ('6', 'mysql', '172.16.3.50:5000/mysqld-exporter:latest', '5.7', 'exporter');
INSERT INTO `unit_version` VALUES ('7', 'mysql', '172.16.3.50:5000/mysqld-exporter:latest', '8.0', 'exporter');
INSERT INTO `unit_version` VALUES ('8', 'redis', '172.16.3.50:5000/redis-exporter:latest', '3.2.11', 'exporter');
INSERT INTO `unit_version` VALUES ('9', 'mysql', '172.16.3.50:5000/backup:0.0.1', '5.6', 'backup');
INSERT INTO `unit_version` VALUES ('10', 'mysql', '172.16.3.50:5000/backup:0.0.1', '5.7', 'backup');
INSERT INTO `unit_version` VALUES ('11', 'mysql', '172.16.3.50:5000/backup:0.0.1', '8.0', 'backup');
