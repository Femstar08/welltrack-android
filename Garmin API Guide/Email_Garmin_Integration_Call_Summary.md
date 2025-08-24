
# Garmin Integration Call Summary

**Dear Femi,**

Thank you very much for taking the time to participate in our Integration Call just a few minutes ago.  
I have put together a few of the slides and the most important findings for you.

---

## Key Findings

1. **The data flow is triggered by the synchronization behavior of the customer.**  
2. **Every time the customer synchronizes his device to his Garmin Connect account,**  
   we push the newly received data to your server.  
3. **Please respond asynchronously with HTTP 200 within 30 seconds to everything we send to you.**  
4. **PULL-ONLY requests and direct Mobile App Connections are not permitted.**  

> ⚠️ The Evaluation environment you have been given access to is restricted for sandbox use.  
> Any deviating use of this key such as onboarding end users or exposing it to public GitHub, etc.  
> will result in it being invalidated and/or deleted.

---

## Requirements for Production Level Access

To move to the Production Level for commercial and use in studies,  
please find the requirements below. The review process is initialized via **[Partner Verification]** once ready.

### General Requirements

- Server-to-server communication only (no direct mobile phone API connections)
- No freemail accounts or non-company domain email accounts as admin/developer
- NDA copy between your company and subcontractor (if applicable)
- Deregistration Endpoint enabled
- User Permission Change Endpoint enabled
- USER-ID processing implemented (See Chapter 3, Start Guide)
- At least two Garmin Connect accounts connected to your review key
- No PULL-ONLY request mechanisms installed
- At least one successful data transfer for each enabled endpoint
- HTTP 200 sent within 30 seconds for all data received
- File size limit set to **100 MB minimum**

### Communication Models

- **PUSH Model:** Asynchronous HTTP 200 response mechanism in place  
- **PING-PULL Model:** Use callback URL of each PING to PULL data within 24 hours

---

## Training/Courses API Production Request

Please provide a screenshot of at least one successfully sent training/course  
from your server to your Garmin Connect account.

---

## UX and Brand Compliance Review

To ensure compliance with Garmin’s branding guidelines, submit:  
- Screenshots and/or video showing:  
  - All uses of Garmin trademarks, logos, and brand elements in the app  
  - All instances of Garmin products and imagery  
  - All required attribution statements (see API brand guidelines)  
  - A complete UX flow to ensure Garmin is not misrepresented  

> All branding, marks, or attribution use in the app must be included in the submission.

---

During the production review, your company account will also be checked  
for any unsupported settings/invitations made during development.

---

**Best Regards / Freundliche Grüße / Cordialement**  
**Elena Kononova**  
_Garmin Connect Partner Services_
