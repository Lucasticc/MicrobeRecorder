# 微生物实验记录 Android APP

一款专为实验室设计的微生物实验记录 Android 原生应用程序。

## ✨ 功能特性

### 📝 实验记录
- 完整的实验记录表单：实验编号、样品名称、培养时间、观察结果、备注
- 自动生成实验编号
- 支持语音输入实验描述

### 📸 相机拍照
- 拍摄实验样本照片
- 照片预览和管理
- 支持多张照片记录

### 🎙️ 录音功能
- 实验过程录音
- 录音文件管理
- 录音时长显示

### 🗣️ 语音转文字
- 语音输入自动转为文字
- 支持中文语音识别
- 实时显示识别结果

### 💾 数据存储
- 本地 SQLite 数据库存储
- 所有数据完全本地化
- 无需联网即可使用

### 📊 数据导出
- **一键导出 Excel 表格**：完整的实验记录表格
- **一键导出 Word 文档**：详细的实验报告
- 支持分享导出文件

### 📋 历史记录
- 历史记录列表查看
- 关键词搜索功能
- 记录详情查看

## 🛠️ 技术栈

- **开发语言**: Kotlin
- **最低支持版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **架构**: MVVM + Repository
- **数据库**: Room (SQLite)
- **UI框架**: Material Design 3
- **异步处理**: Kotlin Coroutines
- **图片加载**: Glide
- **Excel导出**: Apache POI
- **Word导出**: Apache POI

## 📁 项目结构

```
MicrobeRecorder/
├── app/src/main/
│   ├── java/com/microbe/recorder/
│   │   ├── MainActivity.kt              # 主界面
│   │   ├── AddRecordActivity.kt         # 添加记录界面
│   │   ├── RecordDetailActivity.kt      # 记录详情界面
│   │   ├── HistoryActivity.kt           # 历史记录界面
│   │   ├── adapter/
│   │   │   ├── RecordAdapter.kt         # 记录列表适配器
│   │   │   └── PhotoAdapter.kt          # 照片适配器
│   │   ├── database/
│   │   │   ├── AppDatabase.kt           # 数据库配置
│   │   │   ├── RecordDao.kt             # 数据访问对象
│   │   │   └── RecordEntity.kt          # 数据实体
│   │   ├── model/
│   │   │   └── MicrobeRecord.kt         # 数据模型
│   │   └── util/
│   │       ├── CameraHelper.kt          # 相机工具类
│   │       ├── AudioRecorderHelper.kt   # 录音工具类
│   │       ├── SpeechToTextHelper.kt    # 语音转文字工具类
│   │       ├── ExcelExporter.kt         # Excel导出工具类
│   │       ├── WordExporter.kt          # Word导出工具类
│   │       └── FileHelper.kt            # 文件工具类
│   ├── res/
│   │   ├── layout/                      # 布局文件
│   │   ├── values/                      # 资源文件
│   │   ├── drawable/                    # 图片资源
│   │   └── xml/                         # XML配置
│   └── AndroidManifest.xml              # 应用清单
├── build.gradle.kts                     # 项目构建配置
└── README.md                            # 项目说明文档
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 安装步骤

1. **克隆或下载项目**
   ```bash
   git clone <repository-url>
   ```

2. **使用 Android Studio 打开项目**
   - 打开 Android Studio
   - 选择 "Open an existing project"
   - 导航到 MicrobeRecorder 目录并打开

3. **等待 Gradle 同步**
   - 首次打开时，Android Studio 会自动同步 Gradle 依赖
   - 这可能需要几分钟时间

4. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击运行按钮

## 📱 使用说明

### 新建实验记录
1. 在主界面点击"新建记录"
2. 填写实验编号、样品名称、培养时间等基本信息
3. 描述观察结果
4. 可选：拍照、录音、语音输入
5. 点击"保存记录"

### 查看历史记录
1. 在主界面点击"历史记录"
2. 使用搜索功能查找特定记录
3. 点击记录查看详情

### 导出数据
1. 在主界面点击"导出 Excel"或"导出 Word"
2. 系统会自动生成文件并弹出分享选项
3. 选择保存位置或分享给他人

## 🔑 权限说明

应用需要以下权限：
- **相机权限**: 用于拍摄实验样本照片
- **录音权限**: 用于录制实验过程音频
- **存储权限**: 用于保存照片、录音和导出文件
- **麦克风权限**: 用于语音识别功能

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

如有问题或建议，请提交 Issue。
