import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './NavBar.css';
import { useSelector, useDispatch } from 'react-redux'
import { clear_user_session } from "../Redux/userSlice";

function NavBar() {

  const [searchQuery, setSearchQuery] = useState('')

  const dispatch = useDispatch()

  const navigate = useNavigate()

  const logout = () => { 
    dispatch(clear_user_session())
    navigate('/')
  }

  const userContext = useSelector((state) => state.userContext)

  const isLoggedIn = (userContext) => {
    return userContext.jwtToken && userContext.userId && userContext.userType && userContext.username;
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
    <nav className="nav-bar">
      <div className="nav-left">
        <Link to="/">
          <img src="/logo.png" alt="Company Logo" className="nav-logo"/>
        </Link>
        <form className="nav-search" onSubmit={handleSearch}>
        <input type="text" placeholder='Search...' value={searchQuery} onChange={e => setSearchQuery(e.target.value)} className="nav-search-input"/>
          <button type="submit" className="nav-search-button">
            Search
          </button>
        </form>
      </div>
      <div className="nav-right">
        <button className="nav-user-button">{userContext.username}</button>
        <button className="nav-logout-button" onClick={logout}>
          Logout
        </button>
      </div>
    </nav>
    ) : (<></>)
  );
}

export default NavBar;
