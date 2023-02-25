export const oktaConfig = {
  clientId: "0oa8h1aixr98Dcarj5d7",
  issuer: "https://dev-21848764.okta.com/oauth2/default",
  redirectUri: "http://localhost:3000/login/callback",
  scopes: ["openid", "profile", "email"],
  pkce: true,
  disableHttpsCheck: true,
};
