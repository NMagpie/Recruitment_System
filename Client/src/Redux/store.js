import { configureStore, applyMiddleware } from '@reduxjs/toolkit'
import userReducer from './userSlice.js';

// const saveToSessionStorage = store => next => action => {
//     const result = next(action);
//     sessionStorage.setItem('userContext', JSON.stringify(store.getState().userContext));
//     return result;
// };

export default configureStore({
    reducer: { 
        userContext: userReducer,
    },
    //middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(saveToSessionStorage),
})