import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './NavBar.css';
import { useSelector, useDispatch } from 'react-redux'
import { clear_user_session } from "../Redux/userSlice";
import UserInfo from '../UserInfo/UserInfo';
import Modal from 'react-bootstrap/Modal';

function NavBar() {

  const [searchQuery, setSearchQuery] = useState('')

  const [showUser, setShowUser] = useState(false);

  const handleShowUser = () => setShowUser(true);
  const handleCloseUser = () => setShowUser(false);

  const dispatch = useDispatch()

  const navigate = useNavigate()

  const logout = () => { 
    dispatch(clear_user_session())
    navigate('/')
  }

  const userContext = useSelector((state) => state.userContext)

  const isLoggedIn = (userContext) => {
    return userContext.jwtToken 
    && userContext.userId 
    && userContext.name
    && userContext.userType 
    && userContext.username;
  }

  const handleSearch = async (event) => {
    event.preventDefault();

    if (searchQuery.length > 0) {

      const queryArray = searchQuery.split(/[ ,]+/);

      const queryParams = queryArray.map(query => `q=${query}&`);

      navigate("/search/"+queryParams);
    };
  }

  return (isLoggedIn(userContext) ? (
    <div>
      <nav className="nav-bar">
        <div className="nav-left">
          <Link to="/">
            <img src="/logo.png" alt="Cringe Inc." className="nav-logo"/>
          </Link>
          <form className="nav-search" onSubmit={handleSearch}>
          <input type="text" placeholder='Search...' value={searchQuery} onChange={e => setSearchQuery(e.target.value)} className="nav-search-input"/>
            <button type="submit" className="nav-search-button">
              Search
            </button>
          </form>
        </div>
        <div className="nav-right">
          <button className="nav-user-button" onClick={handleShowUser}>{userContext.userType === 'company' ? userContext.name : userContext.name.split(' ')[1] ? userContext.name.split(' ')[1] : userContext.name}</button>
          <button className="nav-logout-button" onClick={logout}>
            Logout
          </button>
        </div>
      </nav>
      <Modal show={showUser} onHide={handleCloseUser} backdrop="static">
        <Modal.Body>
          <div className="overlay" onClick={handleCloseUser}></div>
          <div className="modal-content">
            <UserInfo handleCloseUser={handleCloseUser} userId={userContext.userId}/>
          </div>
        </Modal.Body>
      </Modal>
    </div>
    ) : (<></>)
  );
}

export default NavBar;
