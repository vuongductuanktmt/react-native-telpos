
# react-native-telpos

## Getting started

`$ yarn add vuongductuanktmt/react-native-telpos`

#### iOS (not support)

#### Android
react-native < 0.60
`$ react-native link react-native-telpos`
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
react-native >= 0.60
`Auto link`

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
  
