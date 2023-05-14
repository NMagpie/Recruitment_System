import axios from "axios";
import { useSelector, useDispatch } from 'react-redux'
import { refresh_jwt_token } from "./Redux/userSlice";
import { apiHost, apiPort } from "./App";

export default function JwtRefresher() {
    const userContext = useSelector((state) => state.userContext)
  
    const dispatch = useDispatch()
  
    if (userContext.jwtToken !== "")
      setInterval(() => {

        axios.get(`http://${apiHost}:${apiPort}/refresh_token`, {
          headers: {
            Authorization: `Bearer ${userContext.jwtToken}`,
          },
        })
          .then((response) => {
            if (response.ok) {
              dispatch(refresh_jwt_token({
                jwtToken: response.text,
              }));
            } else {
              console.log(JSON.stringify(response))
              throw new Error("Failed to refresh JWT token");
            }
          })
          .catch((error) => {
            console.error(error);
          });
      }, 840000);
  }