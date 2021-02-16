# Vulnerability: SQL Injection in My Friends (Search) Field

- Vulnerability: SQL Injection
- Where: `Search` Field in `My Friends` Tab
- Impact: Allows Any User to List all Users in the System

## Steps to Reproduce
1. Register with a non existing Username
2. Go to `My Friends` Tab
3. Search = `' -- `, verifying that you are presented with all Users in the System (Their `USERNAME`, `NAME` and `ABOUT`)

## POC
- [Script](./Exploit.py)
- To Run: `../runVulnN.sh 5`
