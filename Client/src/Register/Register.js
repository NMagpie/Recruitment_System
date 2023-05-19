import React, { useState } from "react";
import axios from "axios";
import { apiHost, apiPort } from "../App";
import "./Register.css";
import { useDispatch } from 'react-redux'
import { set_user_session } from "../Redux/userSlice";

const RegistrationPage = ({ handleCloseRegister }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [userType, setUserType] = useState("user");
  const [location, setLocation] = useState("");
  const [error, setError] = useState('');

  const dispatch = useDispatch();

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await axios.post(
        `http://${apiHost}:${apiPort}/register`,
        {
          username: username,
          name: name,
          password: password,
          userType: userType,
          roles: ["USER"],
          location: location,
        }
      );

      dispatch(set_user_session({
        jwtToken: response.data.jwt,
        userId: response.data.user_id,
        name: name,
        userType: userType,
        username: username
      }));

      handleCloseRegister();
    } catch (error) {
      setError(error.message);
    }
  };

return (
  <form onSubmit={handleSubmit} className="registration-form">

    <h2>Registration</h2>

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

    <div className="form-group">
      <label htmlFor="username">Name / Company Name:</label>
      <input
        type="text"
        id="name"
        value={name}
        onChange={(event) => setName(event.target.value)}
      />
    </div>

    <div className="form-group">
      <label htmlFor="user-type">User type:</label>
      <select
        id="user-type"
        value={userType}
        onChange={(event) => setUserType(event.target.value)}
      >
        <option value="user">Client</option>
        <option value="company">Company</option>
      </select>
    </div>
    <div className="form-group">
      <label htmlFor="location">Location:</label>
      <input
        type="text"
        id="location"
        value={location}
        onChange={(event) => setLocation(event.target.value)}
      />
    </div>
    <button type="submit" className="submit-button">Register</button>
    {error && <div>{error}</div>}
  </form>
);

};

export default RegistrationPage;