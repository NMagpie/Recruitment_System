import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux'
import { apiHost, apiPort } from "../App";
import axios from "axios";
import './CV.css';
import { Link, useParams, useNavigate } from 'react-router-dom';

function CV() {
    const [isLoading, setIsLoading] = useState(false)
    const [cv, setCV] = useState({})

    const navigate = useNavigate()

    const userContext = useSelector((state) => state.userContext)

    const { cvId } = useParams()

    const isLoggedIn = (userContext) => {
        return userContext.jwtToken
        && userContext.userId
        && userContext.userType
        && userContext.username;
    }

    const getCV = async () => {

        setIsLoading(true);
    
        const url = `http://${apiHost}:${apiPort}/cv/info/${cvId}`;
    
            try {
                const response = await axios.get(url, {
                headers: { Authorization: `Bearer ${userContext.jwtToken}` }
                });
                setCV(response.data);
            } catch (error) {
                console.error(error);
            }
    
        setIsLoading(false);
        };

    const downloadCV = async (event) => {
        event.preventDefault();

        const url = `http://${apiHost}:${apiPort}/cv/download/${cv._id}`;

        try {
            const response = await axios.get(url, {
            responseType: 'blob',
            headers: { Authorization: `Bearer ${userContext.jwtToken}` }
            });
            const href = URL.createObjectURL(response.data);

            const pdfWindow = window.open();
            pdfWindow.document.title = cv.filename;
            pdfWindow.location.href = href;

            // const link = document.createElement('a');
            // link.href = href;
            // link.setAttribute('download', `${cv.filename}`);
            // document.body.appendChild(link);
            // link.click();
        
            // // clean up "a" element & remove ObjectURL
            // document.body.removeChild(link);
            // URL.revokeObjectURL(href);
        } catch (error) {
            console.error(error);
        }
    }

    const goBack = () => { navigate(-1) }

    useEffect(() => {getCV();}, []);

  return (
    <div>
        {isLoggedIn(userContext) ? (
        <div>
            <div>
                {isLoading && <h2>Loading...</h2>}
            </div>
            <button onClick={goBack} className="download-btn">Back</button>
            <div className="cv-item">
                <h3 className="cv-filename">Title: {cv.filename}</h3>
                <p className="cv-filetype">File type: {cv.filetype}</p>
                <p className="candidate-name">Candidate name: {cv.candidate_name}</p>
                <button onClick={downloadCV} className="download-btn">Download</button>
            </div>
        </div>) : (
        <div>
            <h2>Please, Log In before Fetching!</h2>
            <Link to="/">
                <button>Home</button>
            </Link>
        </div>
        )}
    </div>
  );
}

export default CV;
