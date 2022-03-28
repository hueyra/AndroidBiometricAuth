# BiometricAuth

一个简单的生物识别API库，结合了BiometricManager和SoterAPI

## 1. 依赖方式

#### Step 1. Add the JitPack repository to your build file

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

#### Step 2. Add the dependency

```
dependencies {
	 implementation 'com.github.hueyra:BiometricAuth:1.0.0'
}
```

## 2. 在项目中使用

#### Step 1. 在application初始化中初始化 BiometricAuth.getInstance().init(this)

#### Step 2. 在项目中使用BiometricAuth

```
 for kt.
 
 // 1.设置回调
 BiometricAuth.getInstance().setBiometricAuthCallback(this)
 
 // 2.查看是否支持
 val check = BiometricAuth.getInstance().canAuthenticate(this)
 
 if(check.isSuccess){
 
    // 3.判断是否打开
    if (!BiometricAuth.getInstance().isOpenSoterFingerprintAuth) {
        BiometricAuth.getInstance().openSoterFingerprintAuth(this)
    } else {
        BiometricAuth.getInstance().authenticateWithSoterFingerprint(this)
    }
 }
 
 // 此外，还可以使用Soter判断具体的硬件支持情况
 BiometricAuth.getInstance().isSupportFingerprintAuth(this)


```

#### Step 3. 使用自带的SimpleAuthDialog和AuthErrorDialog完善交互

具体参考示例
