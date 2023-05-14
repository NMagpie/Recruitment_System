import React from 'react';
import './RecEntry.css';
import { Link } from 'react-router-dom';

function RecEntry({ entry, entryType }) {

    return (
        <div>
            { entryType === 'cv' ? (
            <div key={entry.db_id} className='rec-entry'>
                <Link to={`/cv/${entry._id}`}><button className='rec-entry-button'><h3>{entry.filename}</h3></button></Link>
                <p>Name: {entry.candidate_name}</p>
                <p>Filetype: {entry.filetype}</p>
            </div>
            ) : (
            <div key={entry.db_id} className='rec-entry'>
                <Link to={`/job/${entry._id}`}><button className='rec-entry-button'><h3>{entry.title}</h3></button></Link>
                <p>Location: {entry.location}</p>
            </div>
            )}
        </div>
    );
}

export default RecEntry;
