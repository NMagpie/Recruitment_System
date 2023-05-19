import React, { useState, useEffect } from "react";
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux'
import axios from "axios";
import { useDispatch } from 'react-redux'
import { apiHost, apiPort } from "../App";
import "./Home.css";
import Modal from 'react-bootstrap/Modal';
import Login from "../Login/Login";
import RegistrationPage from "../Register/Register";
import RecEntry from "../Recommendation/RecEntry";
import UserInfo from '../UserInfo/UserInfo';

export default function Home() {

    const userContext = useSelector((state) => state.userContext)
  
    const isLoggedIn = (userContext) => {
      return userContext.jwtToken 
      && userContext.userId
      && userContext.name
      && userContext.userType 
      && userContext.username;
    }

    const [showLogin, setShowLogin] = useState(false);

    const handleShowLogin = () => setShowLogin(true);
    const handleCloseLogin = () => setShowLogin(false);

    const [showRegister, setShowRegister] = useState(false);

    const handleShowRegister = () => setShowRegister(true);
    const handleCloseRegister = () => setShowRegister(false);
  
    return (
      <div className="home">
        {isLoggedIn(userContext) ? (
            <LoggedInHome/>
        ) : (
          <div className="column">
            <h1>Welcome to Recruitment System!</h1>

            <button onClick={handleShowLogin}>Sign in</button>
            <Modal show={showLogin} onHide={handleCloseLogin} backdrop="static">
              <Modal.Body>
                <div className="overlay" onClick={handleCloseLogin}></div>
                <div className="modal-content">
                  <Login handleCloseLogin={handleCloseLogin}/>
                </div>
              </Modal.Body>
            </Modal>

            <button onClick={handleShowRegister}>Sign up</button>
            <Modal show={showRegister} onHide={handleCloseRegister} backdrop="static">
            <Modal.Body>
              <div className="overlay" onClick={handleCloseRegister}></div>
              <div className="modal-content">
                <RegistrationPage handleCloseRegister={handleCloseRegister}/>
              </div>
            </Modal.Body>
          </Modal>

          </div>
        )}
      </div>
    )
  }

function LoggedInHome() {

  const userContext = useSelector((state) => state.userContext)

  const [isLoading, setIsLoading] = useState(false)
  const [recommendations, setRecommendations] = useState([])

  const [showUser, setShowUser] = useState(false);
  const [userId, setUserId] = useState('');

  const handleShowUser = (userId) => {
      setUserId(userId)
      setShowUser(true)
  };
  const handleCloseUser = () => setShowUser(false);

  const getRecommendations = async () => {

    setRecommendations([]);

    setIsLoading(true);

    const url = `http://${apiHost}:${apiPort}/recommendation/${userContext.userId}`;

        try {
            const response = await axios.get(url, {
            headers: { Authorization: `Bearer ${userContext.jwtToken}` }
            });
            setRecommendations(response.data.recommendations);
        } catch (error) {
            console.error(error);
            setRecommendations([]);
        }

    setIsLoading(false);
  }

  useEffect(() => {getRecommendations();}, []);

  const entryType = userContext.userType === 'company' ? 'cv' : 'job'

  const recommendList = recommendations.map( 
    entry => { return(
      <RecEntry entry={entry} entryType={entryType} handleShowUser={handleShowUser} />
  ) } )

  return(
      <div className="logged-home">
        <h1>Welcome, {userContext.userType === 'company' ? userContext.name : userContext.name.split(' ')[1] ? userContext.name.split(' ')[1] : userContext.name}!</h1>

        {userContext.userType === 'company' && <Link to="/upload_job"><button>Upload Job</button></Link>}
        {userContext.userType === 'user' && <Link to="/upload_cv"><button>Upload CV</button></Link>}

        {isLoading ? (
          <></>
        ) : (
          <div>
            {recommendations.length > 0 ? (
            <div className="recommendations">
              <h2>Some recommendations for you:</h2>
              <div className="recommend-list">
                {recommendList}
              </div>
            </div>
            ) : (
              <></>
            )}

            <Modal show={showUser} onHide={handleCloseUser} backdrop="static">
                <Modal.Body>
                <div className="overlay" onClick={handleCloseUser}/>
                <div className="modal-content">
                    <UserInfo handleCloseUser={handleCloseUser} userId={userId}/>
                </div>
                </Modal.Body>
            </Modal>
          </div>
        )}
      </div>
  )
}