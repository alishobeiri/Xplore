import React from 'react';
import { StyleSheet, Text, View, Button } from 'react-native';
import { Expo, Permissions, ImagePicker } from 'expo';

// import CameraScreen from './components/Camera';

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      welcomeScreen: true,
      hasCameraPermission: false,
      hasMicrophonePermission: false,
      startRecording: false,
      camDisplay: null,
    }
  }

  //  ask user permission for both camera and microphone
  componentDidMount = async () => {
    const cameraResult = await Permissions.askAsync(Permissions.CAMERA);
    const cameraRollResult = await Permissions.askAsync(Permissions.CAMERA_ROLL);
    this.setState({ hasCameraPermission: cameraResult.status === 'granted' && cameraRollResult.status === 'granted' });

    const microphonePermissions = await Permissions.askAsync(Permissions.AUDIO_RECORDING);
    this.setState({ hasMicrophonePermission: microphonePermissions.status === 'granted' });
  }

  _askForPermissions = async () => {
    const response = await Permissions.askAsync(Permissions.AUDIO_RECORDING);
    this.setState({
      haveRecordingPermissions: response.status === 'granted',
    });
  };

  // launch camera
  launchCam = async () => {
    const camView = await Expo.ImagePicker.launchCameraAsync();
    if(!camView.cancelled) {
      this.setState({camDisplay: camView, welcomeScreen: welcomeScreen});
    }
  };




  render() {
    const { welcomeScreen, hasCameraPermission, hasMicrophonePermission } = this.state;
    if (!hasCameraPermission || !hasMicrophonePermission) {
      return (
        <Text>Please restart app and give camera or microphone access</Text>
      );
    }

    if (welcomeScreen) {
      return (
        <View style={styles.container}>
          <Text>Click to Enter the camera</Text>
          <Button
            onPress={this.launchCam}
            title={"Click to Enter the App"}
            />
          
        </View>
      );
    } else {
      return <View camDisplay={this.state.camDisplay}/>
    }
  }
}




const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
