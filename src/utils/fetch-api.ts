import { getLocalStorage } from "./local-storage";

export const fetchAPI = async (url : string, method = 'GET', headers = {}, body = {}) => {
  try {
    const JWTAuthenticationValue = getLocalStorage("token") ? `Bearer ${getLocalStorage("token")}`: "";
    const options = {
      method: method, 
      headers: {
        'Content-Type': 'application/json',
        'Authorization': JWTAuthenticationValue,
        ...headers 
      },
      body: body && method !== "GET" ? JSON.stringify(body) : null,
    };
    const response = await fetch(url, options);


    if (!response.ok) {
     const errorJson = await response.json();
      throw new Error(errorJson.message);
    }
    const responseJson = await response.json();
    if(responseJson?.data){
    return responseJson.data;
    }
    if(responseJson?.token){
      return responseJson.token;
    }
    return responseJson.message;

  } catch (error) {
    console.error('Error fetching data:', error);
    throw error; 
  }
};

export const toErrorMessage = (e: unknown): string => {
  if (e instanceof Error) return e.message;
  if (typeof e === 'string') return e;
  try { return JSON.stringify(e); } catch { return 'Unknown error'; }
}
