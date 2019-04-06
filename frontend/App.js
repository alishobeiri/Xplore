import React from 'react';
import { StyleSheet, Text, View, Button } from 'react-native';
import { Expo, Permissions, ImagePicker } from 'expo';

import CameraScreen from './components/Camera';

export default class App extends React.Component {
  constructor() {
    super();
    state = {
      welcomeScreen: true,
      hasCameraPermission: null,
    }
  }

  componentWillMount = async () => {
    const cameraResult = await Permissions.askAsync(Permissions.CAMERA);
    const cameraRollResult = await Permissions.askAsync(Permissions.CAMERA_ROLL);
    this.setState({ hasCameraPermission: cameraResult.status === 'granted' && cameraRollResult.status === 'granted' });
  }

  render() {
    const { welcomeScreen } = this.state;
    if (!hasCameraPermission){
      return(
        <Text>Please give the camera access</Text>
      );
    }

    if (welcomeScreen) {
      return (
        <View style={styles.container}>
          <Text>Click to Enter the camera</Text>
          <Button
            title="Click to Enter the App"
            onPress={this.setState({ welcomeScreen: !welcomeScreen })} />
        </View>
      );
    } else {
      return (
        <View>
          <CameraScreen />
        </View>
      );
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
