import React, { useState } from 'react';
import axios from 'axios';
import "./Login.css";
import { useDispatch, } from 'react-redux'
import { set_user_session } from "../Redux/userSlice";
import { apiHost, apiPort } from "../App";

function Login({ handleCloseLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const dispatch = useDispatch()

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await axios.post(`http://${apiHost}:${apiPort}/login`, {
        username: username,
        password: password,
      });

      dispatch(set_user_session({
        jwtToken: response.data.token,
        userId: response.data.userId,
        userType: response.data.userType,
        username: response.data.username
      }));

      handleCloseLogin();
    } catch (error) {
      setError(error.message);
    }
  };

  return (
    <div>
      <form onSubmit={handleSubmit} className="login-form">

      <h2>Login</h2>

      <div className="form-group">
        <label htmlFor="username">Username:</label>
        <input
          type="text"
          id="username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
        />
      </div>

      <div className="form-group">
        <label htmlFor="password">Password:</label>
        <input
          type="password"
          id="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
      </div>

        <button type="submit" className="submit-button">Log in</button>
        {error && <div>{error}</div>}
      </form>
    </div>
  );

}

export default Login;
