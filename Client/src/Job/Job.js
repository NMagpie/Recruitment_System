import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux'
import { apiHost, apiPort } from "../App";
import axios from "axios";
import './Job.css';
import { Link, useParams, useNavigate } from 'react-router-dom';

function Job() {
    const [isLoading, setIsLoading] = useState(false)
    const [cv, setCV] = useState({})

    const navigate = useNavigate()

    const userContext = useSelector((state) => state.userContext)

    const { jobId } = useParams()

    const isLoggedIn = (userContext) => {
        return userContext.jwtToken
        && userContext.userId
        && userContext.userType
        && userContext.username;
    }

    const getJob = async () => {

        setIsLoading(true);

        const url = `http://${apiHost}:${apiPort}/job/${jobId}`;

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

    const goBack = () => { navigate(-1) }

    useEffect(() => {getJob();}, []);

    return (
        <div>
            {isLoggedIn(userContext) ? (
            <div>
                <div>
                    {isLoading && <h2>Loading...</h2>}
                </div>
                <button onClick={goBack} className="download-btn">Back</button>
                <div className="job-item">
                    <h3 className="job-title">Title: {cv.title}</h3>
                    <p className="job-location">Location: {cv.location}</p>
                    <p className="job-description">Description: {cv.description}</p>
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

export default Job;
