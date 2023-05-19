import React, { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import './UserInfo.css';
import { useSelector } from 'react-redux'
import { apiHost, apiPort } from "../App";
import axios from "axios";

function UserInfo({ handleCloseUser, userId }) {
    const [isLoading, setIsLoading] = useState(false)
    const [isDocumentsLoading, setIsDocumentsLoading] = useState(false)
    const [user, setUser] = useState({})
    const [documents, setDocuments] = useState([])

    const userContext = useSelector((state) => state.userContext)

    const getUser = async () => {

        setIsLoading(true);

        setIsDocumentsLoading(true);

        const userUrl = `http://${apiHost}:${apiPort}/user/${userId}`;

        try {
            const response = await axios.get(userUrl, {
                headers: { Authorization: `Bearer ${userContext.jwtToken}` }
                });

            setUser(response.data);

            const documentType = response.data.userType === 'company' ? 'job' : 'cv'

            const documentsUrl = `http://${apiHost}:${apiPort}/user/${documentType}/${userId}`;

            const documentResponse = await axios.get(documentsUrl, {
                headers: { Authorization: `Bearer ${userContext.jwtToken}` }
                });

            setDocuments(documentResponse.data.documents);
        } catch (error) {
            console.error(error);
        }

        setIsDocumentsLoading(false);

        setIsLoading(false);
        };

    useEffect(() => {
        getUser();
    }, []);

    return (
    <div>
        <div className="user-item">

            <div>
                {isLoading && <h2>Loading...</h2>}
            </div>

            <h3 className="user-title">Name: {user.name}</h3>
            <p className="user-description">Username: {user.username}</p>
            <p className="user-location">User Type: {user.userType === 'company' ? 'Company' : 'Client'}</p>
            <p className="user-description">Location: {user.location}</p>

            <div className='document-list'>

                <div>
                    {isDocumentsLoading && <h2>Loading...</h2>}
                </div>

                {!isDocumentsLoading && documents.length == 0 && <h2>User has no entries!</h2>}

                {user.userType === 'user' && 
                    documents.map(document => {
                        return(
                            <div key={document._id} className='entry'>
                                <Link to={`/cv/${document._id}`}><button onClick={handleCloseUser} className='entry-button'><h3>{document.filename}</h3></button></Link>
                                {/* <p>Name: {document.candidate_name}</p> */}
                                <p>Filetype: {document.filetype}</p>
                            </div>
                        )}
                    )}

                {user.userType === 'company' && 
                    documents.map(document => {
                        return(
                            <div key={document._id} className='entry'>
                            <Link to={`/job/${document._id}`}><button onClick={handleCloseUser} className='entry-button'><h3>{document.title}</h3></button></Link>
                            {/* <p>Name: {document.company_name}</p> */}
                            <p>Location: {document.location}</p>
                            </div>
                        )}
                    )}

            </div>
        </div>
    </div>
    );
}

export default UserInfo;