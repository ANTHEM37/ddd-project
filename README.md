# DDD Project

本项目是一个基于Spring Boot的领域驱动设计(DDD)框架及示例实现，包含以下两大部分：

1. **ddd-framework**: 提供完整的DDD架构支持和最佳实践实现的框架
2. **ddd-example**: 基于该框架实现的用户管理示例应用

## 框架概述

`ddd-framework`是一个严格遵循DDD分层架构的框架，包含以下核心模块：
- **ddd-common**: 通用工具和基础类
- **ddd-interfaces**: 接口层（用户界面层）
- **ddd-application**: 应用层
- **ddd-domain**: 领域层
- **ddd-infrastructure**: 基础设施层

## 详细文档

请查看 [ddd-framework/README.md](ddd-framework/README.md) 以获取框架的详细架构设计、核心组件和使用方法。

## 示例项目

`ddd-example`提供了一个基于框架实现的用户管理系统，展示了如何在实际项目中应用DDD架构。

## 技术栈
- Java 8
- Spring Boot 2.7.x
- Maven

## 快速开始
1. 构建项目: `mvn clean install`
2. 查看框架文档: 阅读 ddd-framework/README.md
3. 运行示例: 参考 ddd-example 中的说明

## 许可证
本项目采用MIT许可证 - 详见LICENSE文件