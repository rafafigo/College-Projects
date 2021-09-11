from os import system

zipFile = input("ZIP File: ")
pdfFile = input("PDF File: ")

system(f"zip2john {zipFile} > {zipFile}.hash")
system(f"pdftotext {pdfFile} {pdfFile}.txt")

with open(f"{pdfFile}.txt", 'r') as file:
    wl = [f"{word}\n" for line in file for word in line.split()]

with open(f"{pdfFile}.txt", 'w') as file:
    file.writelines(wl)

system(f"john --wordlist={pdfFile}.txt {zipFile}.hash")
system(f"john --show {zipFile}.hash")
