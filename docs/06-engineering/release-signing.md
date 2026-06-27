# Release Signing

本文档记录 LifeLab 稳定升级链路的 release 签名、GitHub Actions secrets 和发版步骤。不要把 keystore 文件、密码或 base64 后的 keystore 内容提交到仓库。

## 1. 安装与覆盖升级规则

Android 判断是否能覆盖安装主要看三件事：

- `applicationId` 必须一致。
- APK 签名证书必须一致。
- 新 APK 的 `versionCode` 必须大于设备上已安装版本。

因此，第一次从早期默认包名 `com.example.lifelab` 切换到正式包名 `com.study.lifelab` 时，Android 会把它当作另一个应用，不能覆盖旧包。需要先卸载旧包，或允许两个包并存。

切换到 `com.study.lifelab` 之后，后续 release APK 只要继续使用同一个 `applicationId`、同一套 release keystore，并且每次发版递增 `versionCode`，就可以对上一版执行覆盖升级。

## 2. Gradle 属性合同

Gradle release 配置由另一个 worker 实现，CI 和本地发版文档只依赖以下属性名：

```text
LIFELAB_APPLICATION_ID
LIFELAB_VERSION_CODE
LIFELAB_VERSION_NAME
LIFELAB_RELEASE_STORE_FILE
LIFELAB_RELEASE_STORE_PASSWORD
LIFELAB_RELEASE_KEY_ALIAS
LIFELAB_RELEASE_KEY_PASSWORD
```

release APK 构建时至少需要提供 `LIFELAB_RELEASE_STORE_FILE`、`LIFELAB_RELEASE_STORE_PASSWORD`、`LIFELAB_RELEASE_KEY_ALIAS` 和 `LIFELAB_RELEASE_KEY_PASSWORD`。版本和包名属性由 Gradle 配置决定是否从 `gradle.properties`、CI 参数或其他发布流程传入。

## 3. GitHub Secrets

仓库 Settings -> Secrets and variables -> Actions 中需要配置：

```text
ANDROID_RELEASE_KEYSTORE_BASE64
ANDROID_RELEASE_KEY_ALIAS
ANDROID_RELEASE_KEY_PASSWORD
ANDROID_RELEASE_STORE_PASSWORD
```

其中 `ANDROID_RELEASE_KEYSTORE_BASE64` 是 release keystore 文件的 base64 文本。其他三个值分别对应 keystore key alias、key password 和 store password。

生成新的 release keystore 时，在受控机器上执行类似命令，并把生成的 `.jks` 存入团队密钥库或密码管理器：

```bash
keytool -genkeypair \
  -v \
  -keystore lifelab-release.jks \
  -storetype JKS \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias lifelab-release
```

把 keystore 转成 GitHub Secret 可用的 base64：

```bash
base64 -w 0 lifelab-release.jks
```

macOS 默认 `base64` 没有 `-w` 参数，可使用：

```bash
base64 -i lifelab-release.jks | tr -d '\n'
```

Windows PowerShell 可使用：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("lifelab-release.jks"))
```

## 4. GitHub Actions 发版流程

`.github/workflows/android-ci.yml` 的 PR 路径只运行：

```bash
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:lintDebug --no-daemon
```

手动运行 `Android CI` workflow，或推送 `v*` tag 时，workflow 会：

1. 检查四个 `ANDROID_RELEASE_*` secrets 是否存在，缺失时失败并输出缺失项。
2. 将 `ANDROID_RELEASE_KEYSTORE_BASE64` 解码到 runner 临时目录。
3. 执行 `./gradlew :app:assembleRelease --no-daemon`，并用 `-P` 传入 `LIFELAB_RELEASE_*` 签名属性。
4. 上传 `app/build/outputs/apk/release/app-release.apk`，artifact 名称为 `lifelab-release-apk`。

推荐发版步骤：

```bash
git switch main
git pull --ff-only
git tag vX.Y.Z
git push origin vX.Y.Z
```

也可以在 GitHub Actions 页面选择 `Android CI` 后手动运行 workflow，用于生成一次 release APK artifact。

## 5. 发版前检查

每次发布前确认：

- `applicationId` 仍为正式包名，例如 `com.study.lifelab`。
- `versionCode` 大于上一个已分发 release APK。
- 使用同一套 release keystore 和 alias。
- GitHub Secrets 与 keystore 实际密码匹配。
- PR CI 中没有增加 APK 构建步骤，避免在普通 PR 上暴露发布路径。
