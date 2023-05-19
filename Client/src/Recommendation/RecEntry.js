import React from 'react';
import './RecEntry.css';
import { Link } from 'react-router-dom';

function RecEntry({ entry, entryType, handleShowUser }) {

    return (
        <div>
            { entryType === 'cv' ? (
            <div key={entry.db_id} className='rec-entry'>
                <Link to={`/cv/${entry._id}`}><button className='rec-entry-button'><h3>{entry.filename}</h3></button></Link>
                <p className='user-name-text'>Name: <button onClick={() => handleShowUser(entry.user_id)}>{entry.candidate_name}</button></p>
                <p>Filetype: {entry.filetype}</p>
            </div>
            ) : (
            <div key={entry.db_id} className='rec-entry'>
                <Link to={`/job/${entry._id}`}><button className='rec-entry-button'><h3>{entry.title}</h3></button></Link>
                <p className='user-name-text'>Company Name: <button onClick={() => handleShowUser(entry.user_id)}>{entry.company_name}</button></p>
                <p>Location: {entry.location}</p>
            </div>
            )}
        </div>
    );
}

export default RecEntry;
