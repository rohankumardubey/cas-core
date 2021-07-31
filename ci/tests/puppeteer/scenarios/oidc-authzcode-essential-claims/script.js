const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const https = require('https');
const assert = require('assert');
const axios = require('axios');
const jwt = require('jsonwebtoken');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://apereo.github.io";
    let url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client"
        + "&redirect_uri=" + redirectUrl + "&scope=openid&state=gKK1AT6qfk"
        + "&nonce=gzpjHPGJpu&response_type=code"
        + "&claims=%7B%22userinfo%22:%7B%22name%22:%7B%22essential%22:true%7D%7D%7D";

    console.log("Navigating to " + url);
    await page.goto(url);
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.click(page, "#allow");
    await page.waitForNavigation();

    let code = await cas.assertParameter(page, "code");
    console.log("OAuth code " + code);

    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += "redirect_uri=" + redirectUrl;

    let accessTokenUrl = 'https://localhost:8443/cas/oidc/token?' + accessTokenParams + "&code=" + code;
    console.log("Calling " + accessTokenUrl);

    let accessToken = null;
    await instance
        .post(accessTokenUrl, new URLSearchParams(), {
            headers: {
                'Content-Type': "application/json"
            }
        })
        .then(res => {
            console.log(res.data);
            assert(res.data.access_token !== null);

            accessToken = res.data.access_token;
            console.log("Received access token " + accessToken);

            console.log("Decoding ID token...");
            let decoded = jwt.decode(res.data.id_token);
            console.log(decoded);
            assert(decoded.sub !== null)
        })
        .catch(error => {
            throw 'Operation failed to obtain access token: ' + error;
        })

    assert(accessToken != null, "Access Token cannot be null")

    let profileUrl = "https://localhost:8443/cas/oidc/profile?access_token=" + accessToken;
    console.log("Calling user profile " + profileUrl);
    instance
        .post(profileUrl, new URLSearchParams(), {
            headers: {
                'Content-Type': "application/json"
            }
        })
        .then(res => {
            console.log(res.data);
            assert(res.data.name != null)
            assert(res.data.sub != null)
        })
        .catch(error => {
            throw 'Operation failed: ' + error;
        })

    await browser.close();
})();