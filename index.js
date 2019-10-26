
import {DeviceEventEmitter, NativeModules} from 'react-native';

const { qrReader, nfcReader, closeQr, closeNfc } = NativeModules.RNTelpos;

export const qrReaderEmit = (options, cb) =>{
    qrReader(options);
    return DeviceEventEmitter.addListener('onQrDetected', nfcData => {
        cb(nfcData)
    })
}
export const nfcReaderEmit = (options, cb) =>{
    nfcReader(options)
    return DeviceEventEmitter.addListener('onNfcDetected', qrData => {
        cb(qrData)
    })
};

export const closeQrEmit = () => closeQr();

export const closeNfcEmit = () => closeNfc();
