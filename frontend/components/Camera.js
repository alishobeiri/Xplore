import React from 'react';
import { View, Text } from 'react-native';
import Camera from 'react-native-camera';

export default class CameraScreen extends React.Component {
    state = {

    }

    render() {
        <View>
            <Text>HELLO</Text>
            <Camera
                ref={(cam) => {
                    this.camera = cam;
                }}>

            </Camera>
        </View>
    }
} 