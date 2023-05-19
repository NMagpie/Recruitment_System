import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux'
import { apiHost, apiPort } from "../App";
import axios from "axios";
import { Link, useParams, useNavigate } from 'react-router-dom';
import './Search.css';
import UserInfo from '../UserInfo/UserInfo';
import Modal from 'react-bootstrap/Modal';


function Search() {
    const [isLoading, setIsLoading] = useState(false)
    const [searchResults, setSearchResults] = useState([])
    const [zeroResults, setZeroResults] = useState(false)
    const [offset, setOffset] = useState(0)
    const { query } = useParams()

    const [showUser, setShowUser] = useState(false);
    const [userId, setUserId] = useState('');

    const handleShowUser = (userId) => {
        setUserId(userId)
        setShowUser(true)
    };
    const handleCloseUser = () => setShowUser(false);

    // const handleScroll = async (e) => {

    //     if (e.target.scrollTop + e.target.clientHeight === e.target.scrollHeight) {
    //         setIsLoading(true);

    //         console.log('scrolled to bottom');

    //         const new_offset = offset + 1;

    //         setOffset(new_offset);

    //         const documentType = userContext.userType === 'company' ? 'cvs' : 'jobs';

    //         const url = `http://${apiHost}:${apiPort}/search/${documentType}/?${query}offset=${new_offset}`;

    //             try {
    //                 const response = await axios.get(url, {
    //                 headers: { Authorization: `Bearer ${userContext.jwtToken}` }
    //                 });

    //                 if (response.data.results.length > 0) {

    //                     const updatedResults = searchResults.concat(response.data.results);

    //                     setSearchResults(updatedResults);
    //                 }

    //             } catch (error) {
    //                 console.error(error);
    //             }

    //         setIsLoading(false);
    //     }
    // };

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

    const handleSearch = async () => {

        setOffset(0);

        setSearchResults([]);

        setZeroResults(false);

        setIsLoading(true);

        const documentType = userContext.userType === 'company' ? 'cvs' : 'jobs';

        const url = `http://${apiHost}:${apiPort}/search/${documentType}/?${query}offset=${offset}`;

            try {
                const response = await axios.get(url, {
                headers: { Authorization: `Bearer ${userContext.jwtToken}` }
                });
                setSearchResults(response.data.results);

                if (response.data.results.length == 0)
                    setZeroResults(true);
            } catch (error) {
                console.error(error);
                setSearchResults([]);
            }

        setIsLoading(false);
    };

    useEffect(() => {handleSearch();}, [query]);

    return(
        <div>
        {isLoggedIn(userContext) ? (
            <div className='search-page' 
            // onScroll={handleScroll}
            >
            <div className='messages'>
                {isLoading && <h2>Loading...</h2>}

                {zeroResults && <h2>Sorry! No results...</h2>}
            </div>
            <div className="results">

                {!isLoading && <button onClick={goBack} className="download-btn">Back</button>}

                {userContext.userType === 'company' && 
                searchResults.map(result => {
                    return(
                        <div key={result.db_id} className='entry'>
                        <Link to={`/cv/${result.db_id}`}><button className='entry-button'><h3>{result.filename}</h3></button></Link>
                        <p className='user-name-text'>Name: <button onClick={() => handleShowUser(result.user_id)}>{result.candidate_name}</button></p>
                        <p>Filetype: {result.filetype}</p>
                        </div>
                    )}
                )}

                {userContext.userType === 'user' && 
                    searchResults.map(result => {
                        return(
                            <div key={result.db_id} className='entry'>
                            <Link to={`/job/${result.db_id}`}><button className='entry-button'><h3>{result.title}</h3></button></Link>
                            <p className='user-name-text'>Company Name: <button onClick={() => handleShowUser(result.user_id)}>{result.company_name}</button></p>
                            <p>Location: {result.location}</p>
                            </div>
                        )}
                    )}

                {/* {offset > 0 && isLoading && <h2>Loading...</h2>} */}

                <Modal show={showUser} onHide={handleCloseUser} backdrop="static">
                    <Modal.Body>
                    <div className="overlay" onClick={handleCloseUser}></div>
                    <div className="modal-content">
                        <UserInfo handleCloseUser={handleCloseUser} userId={userId}/>
                    </div>
                    </Modal.Body>
                </Modal>

            </div>
        </div>) : (
        <div>
            <h2>Please, Log In before Searching!</h2>
            <Link to="/">
                <button>Home</button>
            </Link>
        </div>
    )}
    </div>
        )
}

export default Search;