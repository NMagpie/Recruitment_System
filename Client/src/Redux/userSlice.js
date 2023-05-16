import { createSlice } from '@reduxjs/toolkit'

export const userSlice = createSlice({
  name: 'userContext',

  initialState: {
    jwtToken: "",
    userId: "",
    name: "",
    userType: "",
    username: ""
  },

  reducers: {

    refresh_jwt_token: (state, action) => {
      state.jwtToken = action.payload.jwtToken;
    },

    set_user_session: (state, action) => {
      state.jwtToken = action.payload.jwtToken;
      state.userId = action.payload.userId;
      state.name = action.payload.name;
      state.userType = action.payload.userType;
      state.username = action.payload.username;
    },

    clear_user_session: (state) => {
      state.jwtToken = "";
      state.userId = "";
      state.name = "";
      state.userType = "";
      state.username = "";
    }
  },
})

export const { set_user_session, clear_user_session, refresh_jwt_token } = userSlice.actions

export default userSlice.reducer