import React from "react";
import { Provider } from 'react-redux'
import store from "./Redux/store";
import JwtRefresher from "./JwtRefresher";
import Home from "./Home/Home";
import UploadJob from "./Upload/Upload_Job";
import UploadCV from "./Upload/Upload_CV";
import NavBar from "./NavBar/NavBar";
import Search from "./Search/Search";
import CV from "./CV/CV";
import Job from "./Job/Job";
import './App.css';

import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

export const apiHost = process.env.REACT_APP_API_HOST;
export const apiPort = process.env.REACT_APP_API_PORT;

export default function App() {

  return (
    <Provider store={store}>
      <Router>
        <NavBar/>
          <Routes>
            <Route exact path="/" element={<Home/>}/>
            <Route exact path="/upload_job" element={<UploadJob/>}/>
            <Route exact path="/upload_cv" element={<UploadCV/>}/>
            <Route path="/search/:query" element={<Search/>}/>
            <Route path="/cv/:cvId" element={<CV/>}/>
            <Route path="/job/:jobId" element={<Job/>}/>
          </Routes>
      </Router>
      <JwtRefresher/>
    </Provider>
  );
}