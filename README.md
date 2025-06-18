# 密码管理器 (Password Manager)

一个基于Android的本地密码管理应用，使用Kotlin和Jetpack Compose开发。

## 功能特性

- 📱 **现代化UI**: 使用Jetpack Compose构建的Material Design 3界面
- 🔒 **本地存储**: 所有密码数据存储在本地SQLite数据库中，确保隐私安全
- 🔍 **智能搜索**: 支持实时搜索密码条目
- 🔤 **拼音排序**: 支持中文拼音首字母排序，方便查找
- ➕ **密码管理**: 添加、编辑、删除密码条目
- 📝 **详细信息**: 支持存储账号名称、用户名、密码和备注信息

## 技术栈

- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM (Model-View-ViewModel)
- **数据库**: Room (SQLite)
- **异步处理**: Kotlin Coroutines + Flow
- **依赖注入**: 手动依赖注入
- **拼音处理**: Pinyin4j

## 项目结构

```
app/src/main/java/com/ycw/passwordmanager/
├── data/                    # 数据层
│   ├── PasswordEntity.kt    # 密码实体类
│   └── PasswordDatabase.kt  # Room数据库配置
├── repository/              # 仓库层
│   └── PasswordRepository.kt
├── viewmodel/              # ViewModel层
│   ├── PasswordViewModel.kt
│   └── PasswordViewModelFactory.kt
├── ui/                     # UI层
│   ├── components/         # UI组件
│   ├── screens/           # 屏幕页面
│   └── theme/             # 主题配置
├── utils/                  # 工具类
│   └── PinyinUtils.kt     # 拼音处理工具
└── MainActivity.kt         # 主Activity
```

## 安装要求

- Android 7.0 (API level 24) 或更高版本
- 约 10MB 存储空间

## 构建项目

1. 克隆项目到本地:
```bash
git clone https://github.com/yourusername/PasswordManager.git
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖

4. 运行项目:
```bash
./gradlew assembleDebug
```

## 主要依赖

- `androidx.compose.bom:2024.12.01` - Compose BOM
- `androidx.room:room-runtime:2.6.1` - Room数据库
- `androidx.room:room-ktx:2.6.1` - Room Kotlin扩展
- `com.belerweb:pinyin4j:2.5.1` - 拼音处理库
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel Compose集成

## 安全说明

⚠️ **重要提醒**: 
- 本应用将密码以明文形式存储在本地数据库中
- 建议在设备上启用屏幕锁定以增加安全性
- 定期备份重要密码数据
- 不建议在共享设备上使用

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
