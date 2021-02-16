# Vulnerability: SQL Injection & Stored XSS Attack in About Field in Edit Profile (Store Script in Photo Field)

- Vulnerability: SQL Injection & Stored XSS Attack
- Where: `About` Field in Profile Form
- Impact: Allows a prior Stored Script in Photo Field to be executed

## Steps to Reproduce
1. Register with a non existing Username `U`
2. Go to `Profile` Tab
3. In the `About` Field, insert `', PHOTO = '">`[DisplaysAlert](../Common/Scripts/XSS/DisplaysAlert.html)`' WHERE USERNAME = 'U' -- `
4. Click in `Update Profile` Button
5. Go to `New Post` Tab, and create a Random Post
6. Go to `/`, verifying that an Alert is displayed

## POC
- [Script](./Exploit.py)
- To Run: In `../runVulnN.sh 31`
