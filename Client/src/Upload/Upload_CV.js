import React, { useState } from "react";
import axios from "axios";
import { apiHost, apiPort } from "../App";
import { useSelector,} from 'react-redux';
import "./Upload_CV.css";
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';

const UploadCV = () => {
  const [file, setFile] = useState(null);
  const [candidateName, setCandidateName] = useState("");

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const userContext = useSelector((state) => state.userContext)

  const isLoggedIn = (userContext) => {
    return userContext.jwtToken && userContext.userId && userContext.userType && userContext.username;
  }

  const navigate = useNavigate()

  const goBack = () => { navigate(-1) }

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleCandidateNameChange = (e) => {
    setCandidateName(e.target.value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!file || !candidateName) {
      setError("Please fill out all fields.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("candidate_name", candidateName);
    formData.append("user_id", userContext.userId);

    try {
      const response = await axios.post(
        `http://${apiHost}:${apiPort}/upload_cv`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${userContext.jwtToken}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );
      setMessage(response.data.status);
      setFile(null);
      setCandidateName("");
      navigate("/");
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

          <h2>CV Upload</h2>

          <div className="form-group">
            <label htmlFor="file">CV File</label>
            <input type="file" id="file" onChange={handleFileChange} />
          </div>

          <div className="form-group">
            <label htmlFor="candidateName">Candidate Name</label>
            <input
              type="text"
              id="candidateName"
              value={candidateName}
              onChange={handleCandidateNameChange}
            />
          </div>

          <button type="submit" className="submit-button">Upload</button>
          {error && <div>{error}</div>}
          {message && <p>{message}</p>}
        </form>
      </div>) : (
    <div>
        <h2>Please, Log In before Uploading CV!</h2>
        <Link to="/">
              <button>Home</button>
            </Link>
    </div>
    )}
    </div>
  );
};

export default UploadCV;
