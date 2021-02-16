# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Friends Requests in the System

## Steps to Reproduce
1. Register as the Victim `A` with a non existing Username
2. Logout
3. Register as the Victim `B` with a non existing Username
4. Sends a Friend Request to Victim `A`
5. Logout
6. Register as the Attacker with a non existing Username
7. Go to `My Friends` Tab
8. Search = `' AND 1 <> 1 UNION SELECT USERNAME1, 1, USERNAME2, ID, 1 FROM FriendsRequests -- `, verifying that you are presented with Victim's `B` Friend Request to Victim `A`

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 8`
