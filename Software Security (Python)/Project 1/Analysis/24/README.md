# Vulnerability: Stored XSS in the Content Field in the Create Posts Form that Accepts Friend Requests

- Vulnerability: Stored XSS
- Where: `Content` Field in Create Posts Form
- Impact: Allows a prior Stored Script in a Form Content to Accepts Friend Requests impersonating the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `New Post` and create it with `Content` = [AcceptsFriendRequest](../Common/Scripts/XSS/AcceptsFriendRequest.html)
5. Go to `Add a Friend` and send a Friend Request to the Victim `V`
6. Logout
7. Login as the Victim
8. Go to `Friends`, verifying that the Attacker `A` is in it

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 24`
