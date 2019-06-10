import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">React DOOM</h1>
        </header>
        <p className="App-intro">
          Click on Screen, Press Enter to start. Use Arrows to move, Space to use and Ctrl to Fire.
        </p>
      </div>
    );
  }
}

export default App;
