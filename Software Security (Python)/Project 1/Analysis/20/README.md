# Vulnerability: Stored XSS in the About Field of Edit Profile that can Insert Alerts

- Vulnerability: Stored XSS
- Where: `About` Field in Edit Profile Form
- Impact: Allows a prior Stored Script in Name Field to Display Alerts to the Victim

## Steps to Reproduce
1. Register as the Victim `V` with a non existing Username
2. Logout
3. Register as the Attacker `A` with a non existing Username
4. Go to `Update Profile` and update it with `About` = [DisplaysAlert](../Common/Scripts/XSS/DisplaysAlert.html)
5. Go to `Add a Friend` and send a Friend Request to the Victim `V`
6. Logout
7. Login as the Victim
8. Go to `Pending Requests`, verifying that an Alert was Displayed

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 20`
