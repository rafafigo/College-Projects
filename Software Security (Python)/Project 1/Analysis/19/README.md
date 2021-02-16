# Vulnerability: Stored XSS in the About Field of Edit Profile that can Create a Post

- Vulnerability: Stored XSS
- Where: `About` Field in Edit Profile Form
- Impact: Allows a prior Stored Script in Name Field to Create a Post impersonating the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `Update Profile` and update it with `About` = [CreatesPost](../Common/Scripts/XSS/CreatesPost.html)
5. Go to `Add a Friend` and send a Friend Request to the Victim `V`
6. Logout
7. Login as the Victim
8. Go to `Pending Requests`
9. Go to `/`, verifying that a Post was created impersonating the Victim `V`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 19`
