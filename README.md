
# react-native-telpos

## Getting started

`$ yarn add vuongductuanktmt/react-native-telpos`

### Mostly automatic installation

`$ react-native link react-native-telpos`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-telpos` and add `RNTelpos.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNTelpos.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNTelposPackage;` to the imports at the top of the file
  - Add `new RNTelposPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-telpos'
  	project(':react-native-telpos').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-telpos/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-telpos')
  	```

## Usage
```javascript
import {qrReaderEmit, nfcReaderEmit, closeNfcEmit, closeQrEmit} from 'react-native-telpos';
componentDidMount() {
        this.qrReaderEmit = qrReaderEmit({timeout: 300}, qrData => {
            this.setState({qrData})
            ToastAndroid.show(qrData, ToastAndroid.SHORT)
        });
        this.nfcReaderEmit = nfcReaderEmit({timeout: 500}, nfcData => {
            this.setState({nfcData})
            ToastAndroid.show(JSON.stringify(nfcData), ToastAndroid.SHORT)

        });
    }

    componentWillUnmount() {
        this.nfcReaderEmit.remove()
        this.qrReaderEmit.remove()
    }
```
  
