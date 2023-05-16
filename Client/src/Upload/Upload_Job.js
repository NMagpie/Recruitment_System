import React, { useState } from 'react';
import { apiHost, apiPort } from "../App";
import { useSelector,} from 'react-redux';
import "./Upload_Job.css";
import { useNavigate } from 'react-router-dom';
import axios from "axios";
import { Link } from 'react-router-dom';

const UploadJob = () => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const userContext = useSelector((state) => state.userContext)

  const isLoggedIn = (userContext) => {
    return userContext.jwtToken 
    && userContext.userId 
    && userContext.name
    && userContext.userType 
    && userContext.username;
  }

  const navigate = useNavigate()

  const goBack = () => { navigate(-1) }

  const handleTitleChange = (event) => {
    setTitle(event.target.value);
  };

  const handleDescriptionChange = (event) => {
    setDescription(event.target.value);
  };

  const handleLocationChange = (event) => {
    setLocation(event.target.value);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
        const response = await axios.post(
          `http://${apiHost}:${apiPort}/upload_job`,
          JSON.stringify({
            user_id: userContext.userId,
            company_name: userContext.name,
            title,
            description,
            location
        }),
          {
            headers: {
              Authorization: `Bearer ${userContext.jwtToken}`,
              'Content-Type': 'application/json',
            },
          }
        );

        setMessage(response.data.status);

        navigate('/');
    } catch (error) {
        setError(error.message);
      }
  };

  return (
    <div>
    {isLoggedIn(userContext) ? (
    <div>
        <button onClick={goBack} className="download-btn">Back</button>
        <form onSubmit={handleSubmit} className="upload-form">

        <h2>Job Upload</h2>

        <div className="form-group">
        <label>
            Job Title:
            <input type="text" value={title} onChange={handleTitleChange} />
        </label>
        </div>

        <div className="form-group">
        <label>
            Job Description:
            <textarea rows="20" cols="65" maxLength="4000" value={description} onChange={handleDescriptionChange} />
        </label>
        </div>

        <div className="form-group">
        <label>
            Location:
            <input type="text" value={location} onChange={handleLocationChange} />
        </label>
        </div>

        <button type="submit" className="submit-button">Upload</button>
        {error && <div>{error}</div>}
        {message && <div>{message}</div>}
        </form>
    </div>
  ) : (
    <div>
        <h2>Please, Log In before Uploading Job!</h2>
        <Link to="/">
              <button>Home</button>
            </Link>
    </div>
  )}
  </div>
  );
};

export default UploadJob;
