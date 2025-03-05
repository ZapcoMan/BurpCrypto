# BurpCrypto

[![Releases](https://img.shields.io/github/v/release/whwlsfb/BurpCrypto.svg?include_prereleases&style=square)](https://github.com/whwlsfb/BurpCrypto/releases)
[![Downloads](https://img.shields.io/github/downloads/whwlsfb/BurpCrypto/total?label=Release%20Download)](https://github.com/whwlsfb/BurpCrypto/releases/latest)

BurpCrypto是一款支持多种加密算法以及直接执行浏览器JS代码的BurpSuite插件。
[English](./README.md) | [简体中文](./README_zh.md)

## 功能描述

BurpCrypto插件主要提供了以下功能：

1. **多种加密算法支持**：支持AES、DES、RSA等多种对称和非对称加密算法。
2. **执行JS代码**：可以执行自定义的JS脚本，以应对复杂的加密逻辑。
3. **密文解密**：支持通过密文查询原始明文的功能。

## 使用方法

### 加密操作

通过在插件的相应选项卡中配置加密算法和密钥，然后点击`Add processor`按钮，成功添加加密处理器后，即可在BurpSuite的各个功能模块中调用。

### 解密操作

通过在插件中完整选中密文内容，右单击后找到BurpCrypto菜单中的`Get PlainText`功能，即可获取原始明文。

## 安装步骤

1. 从[BurpCrypto的Github页面](https://github.com/whwlsfb/BurpCrypto/releases)下载已编译好的版本。
2. 将下载的插件文件在BurpSuite的扩展列表中添加。
3. 插件加载成功后，即可在BurpSuite中使用BurpCrypto进行加密和解密操作。

