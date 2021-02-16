# Vulnerability: Stored XSS in the Name Field of Edit Profile that Makes Friend Requests

- Vulnerability: Stored XSS
- Where: `Name` Field in Edit Profile Form
- Impact: Allows a prior Stored Script in Name Field to Make a Friend Request impersonating the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `Update Profile` and update it with `Name` = [MakesFriendRequest](../Common/Scripts/XSS/MakesFriendRequest.html)
5. Go to `New Post` Tab and create a Random Post
6. Logout
7. Login as the Victim
8. Logout
9. Login as the Attacker
10. Go to `Pending Requests`, verifying that the Victim `V` is in it

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 18`
