

export const setLocalStorage = (key : string, value : object | string)=> {
    if(value && typeof value === "object"){
        localStorage.setItem(key, JSON.stringify(value))
    } else {
        localStorage.setItem(key, value);
    }
}

export const getLocalStorage = (key: string) => {
    const storedValue = localStorage.getItem(key);
    if(storedValue){
        try{
            return JSON.parse(storedValue);
        } 
        catch (_){
            return storedValue
        }
    }
}

export const removeLocalStorage = (key: string) => {
  localStorage.removeItem(key);
}