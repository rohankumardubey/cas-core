const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const service = `https://localhost:9859/anything/sample1`;
    let token = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiYWxnIjoiZGlyIn0..zTe4qLwuyvVi6gHmSL4VcQ.NAmUxtxfEc-xJ0BO1DIUtxRN05-XTiB4bFVqN4YFvBPtVUrppTR5oXVKszWYQD_jWuhHnUBCVcvOBri9n-q6rKXQBkMnW9TLXQ4d9waJUOKpxORvgX3T56qoVfYTbUkVxe-5VchX000JWy8GdhpyWawldG0au03GhU7jhVnQeMlb7WWaNFOGXQwx6wvF0B30UL-6wgZO1nWD7InaQXJiFZazE0HK0DX61DUbP6PFYJKOBkbSWg9vSSzCeTxVFx9uJXMSAg9_vacpAYmq02ixV8e73CU9_hHhiCqEOYGunzwO4mEm6mn3fhPz6Q5azzPhZTc-lROKc66bmo_Y7jtMRmwgwBTq4diY9bIhw0x_qMsLLEdk1qk-dH9_FhBcunW2PkM8rCyGyDQ2slL-Axs_zg.HgP2g-UNsljcDhO74OMFWKxkYiSo4mbK";

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/logout");
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?token=${token}&response_type=code&client_id=client1&scope=openid%20profile%20ssotoken%20email&redirect_uri=${service}`;

    await cas.goto(page, url);
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }
    let code = await cas.assertParameter(page, "code");
    console.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code`
        + `&client_id=client1&client_secret=secret1&redirect_uri=${service}&code=${code}`;
    await cas.goto(page, accessTokenUrl);
    await page.waitForTimeout(1000);
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    console.log(payload);
    assert(payload.access_token != null);
    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded["family_name"] !== undefined);
    assert(decoded["given_name"] !== undefined);
    assert(decoded["name"] !== undefined);
    assert(decoded["token"] !== undefined);

    let profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token }`;
    console.log(`Calling user profile ${profileUrl}`);

    let ssoToken = await cas.doPost(profileUrl, "", {
        'Content-Type': "application/json"
    }, res => {
        assert(res.data.sub === "casuser");
        assert(res.data.attributes["email"] === "cas@example.org");
        assert(res.data.attributes["given_name"] === "CAS");
        return res.data.attributes["token"];
    }, error => {
        throw `Operation failed: ${error}`;
    });

    const response = await cas.doRequest(`https://localhost:8443/cas/actuator/tokenAuth/${ssoToken}?service=1`,
        "GET", {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        });
    let body = JSON.parse(response);
    console.dir(body, {depth: null, colors: true});
    assert(body.registeredService !== undefined);
    assert(body.principal !== undefined);

    await browser.close();
})();
