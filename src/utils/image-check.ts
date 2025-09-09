export const isBase64 = (str: string): boolean => {
  const base64Regex = /^[A-Za-z0-9+/=]+$/;
  const hasValidLength = str.length % 4 === 0; 

  return base64Regex.test(str) && hasValidLength;
}

export const isFile = (obj: unknown) : boolean => {
  return (
    obj instanceof File ||
    (
      obj !== null &&
      typeof obj === 'object' &&
      typeof (obj as File).name === 'string' &&
      typeof (obj as File).size === 'number' &&
      typeof (obj as File).lastModified === 'number'
    )
  );
}